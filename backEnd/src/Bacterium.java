package backEnd.src;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

import backEnd.src.Bacterium.grow;
import backEnd.src.SimulationModel.*;

// class for managing bacterium with methods to manipulate them (activities)
public class Bacterium implements Runnable {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private Thread thread;
    private Object[] actionChosen;
    Block position;
    int bacteriumID;
    String strain;
    int age;
    int energyLevel;
    Bacterium father;
    ArrayList<BacterialMonomer> monomers;
    Environment environ;
    public volatile String waiting = "wait";
    private final Object fileWriteLock = new Object(); // Lock for file writing
    private volatile String currentAction = "";
    private Block moveDestination;
    int energy;
    boolean killThread;
    LocalDateTime birthTime;
    double doublingTime = 0; // Time in hours for bacterium to double in size
    Timer timer = new Timer();
    TimerTask grow;
    MersenneTwister mt;
    int maxAge = 100;
    int maxEnergy = 100;
    int probDie = 0;
    int probEat = 0;
    int probGrow = 0;
    boolean stuck = false;
    public double length;
    double scalingFactor;
    double scaledGrowthRate;
    double scaledDoublingTime;
    Block goal;
    String moveType = null;
    boolean newGoal = true;
    int x;
    int y;
    boolean inB = false;
    Biofilm biofilm = null;

    // paramaterised constructor for bacterium
    public Bacterium(Block position, int ID, int age, Bacterium father,
            ArrayList<BacterialMonomer> monomers, String strain, Environment environ, long seed) {
        this.position = position;
        this.bacteriumID = ID_GENERATOR.getAndIncrement(); // Thread-safe ID generation
        this.age = age;
        this.father = father;
        this.monomers = new ArrayList<BacterialMonomer>();
        this.strain = strain;
        this.environ = environ;
        this.energy = 100;
        this.killThread = false;
        this.doublingTime = generateDoublingTime(1.0); // time is in hours
        this.mt = new MersenneTwister(seed);
        this.grow = new grow(this);
        this.newGoal = true;
        this.goal = this.position;
        this.actionChosen = new Object[2];
        this.scalingFactor = 30 / (this.doublingTime * 3600); // choose intial number but can change
        this.scaledGrowthRate = (1 / this.scalingFactor) * (7 / (this.doublingTime * 3600)); // units/second
        this.scaledDoublingTime = (1 / this.scalingFactor) * (this.doublingTime * 3600);

    }

    public static double generateDoublingTime(double mean) {
        // Generate a random uniform number between 0 and 1
        double u = ThreadLocalRandom.current().nextDouble();

        // Use the inverse transform method to get an exponentially distributed value
        return -mean * Math.log(1 - u);
    }

    // method for returning ID of bacterium
    public int getBID() {
        return this.bacteriumID;
    }

    // method for returning block position of bacterium (where it is in the
    // environment)
    public Block getBlock() {
        return this.position;
    }

    public void setFather(Bacterium bac) {
        father = bac;
    }

    public void tumbleMove(Block destinationBlock) {
        move(this.position, destinationBlock, Environment.environBlocks, "Tumble");
    }

    public void runMove(Block destinationBlock) {
        move(this.position, destinationBlock, Environment.environBlocks, "Run");
    }

    public void reproduce(Block newPosition) throws InterruptedException, BrokenBarrierException {

        // System.out.println(Thread.currentThread().getName() + " is in reproducing
        // method");

        this.length = 7;
        this.birthTime = LocalDateTime.now();
        Bacterium childBac;
        System.out.println(Thread.currentThread().getName() + " adding act");

        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + environ.BacteriumID);
            childBac = environ.createBacterium(environ, newPosition, this);
        }

        // System.out.println("child position is " + newPosition.getStringFormat());
        // System.out.println("parent position is " + this.position.getStringFormat());

        // synchronized (waiting) {
        try {

            SimulationModel.barrier.await();
            synchronized (waiting) {
                if (SimulationModel.reset == true) {
                    SimulationModel.resetBarrier();
                    SimulationModel.reset = false;
                }
            }
            SimulationModel.resetting = SimulationModel.resetting - 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    // Method to be called when file writing is complete
    public void onFileWritten() {
        synchronized (fileWriteLock) {
            fileWriteLock.notifyAll(); // Notify all waiting threads that the file is written
        }
    }

    // bacterium dies, do all bacterial monomers die as well //
    public void die() {
        System.out.println(Thread.currentThread().getName() + " is dying");
        synchronized (Environment.Bacteria) {
            Environment.Bacteria.remove(this);
        }
        position.occupier = null;
        position.setOccupied(false); // makes block free
        this.killThread = true;
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Die");
        try {

            SimulationModel.barrier.await();

            synchronized (waiting) {
                if (SimulationModel.reset == true) {
                    SimulationModel.resetBarrier();
                    SimulationModel.reset = false;
                }
            }

            SimulationModel.resetting = SimulationModel.resetting - 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    public Block getRandomAdjacentFreeBlock() {
        // System.out.println(Thread.currentThread().getName() + " is finding block");
        Random random = new Random();
        int randomBlock = random.nextInt(4); // Generates a number from 0 to 3
        int newYcoord = 0;
        int newXcoord = 0;
        while (this.position.getXPos() == 0 & randomBlock == 0) {
            randomBlock = random.nextInt(4);
        }
        while (this.position.getYPos() == 0 & randomBlock == 2) {
            randomBlock = random.nextInt(4);
        }

        switch (randomBlock) { // moves bacterium to a random adjacent block
            case 0:
                newXcoord = this.position.getXPos() - 1;
                newYcoord = this.position.getYPos();
                synchronized (environ.environBlocks[newXcoord][newYcoord]) {
                    while (environ.environBlocks[newXcoord][newYcoord].occupied()) {
                        try {
                            environ.environBlocks[newXcoord][newYcoord].wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case 1:
                newXcoord = this.position.getXPos() + 1;
                newYcoord = this.position.getYPos();
                // synchronized (destinationPosition) {
                // while (destinationPosition.occupied()) {
                // try {
                // destinationPosition.wait();
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
                // }
                // destinationPosition.setOccupied(true);
                // }

                break;
            case 2:
                newYcoord = this.position.getYPos() - 1;
                newXcoord = this.position.getXPos();
                // synchronized (destinationPosition) {
                // while (destinationPosition.occupied()) {
                // try {
                // destinationPosition.wait();
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
                // }
                // destinationPosition.setOccupied(true);
                // }

                break;
            case 3:
                newYcoord = this.position.getYPos() + 1;
                newXcoord = this.position.getXPos();
                // synchronized (destinationPosition) {
                // while (destinationPosition.occupied()) {
                // try {
                // destinationPosition.wait();
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
                // }
                // destinationPosition.setOccupied(true);
                // }

                break;
        }
        // System.out.println("UYEAH parent position is " +
        // this.position.getStringFormat());
        return Environment.environBlocks[newXcoord][newYcoord];
    }

    // hits another bacterium but doesn't stick together //
    public void collide(Bacterium bac) {
        Block destinationPosition = getRandomAdjacentFreeBlock();

        move(this.position, destinationPosition, Environment.environBlocks, "Run");
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Collide:Bacterium:" + bac.getBID());
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    // increase EPS count, increase Block EPS //
    public void secrete() {
        System.out.println(Thread.currentThread().getName() + " is secreting");
        position.incEPS();
        // not sure if we need the below
        environ.EPSMonomers.add(environ.createEPSMonomer(position));
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Secrete:EPS");

        try {

            SimulationModel.barrier.await();

            synchronized (waiting) {
                if (SimulationModel.reset == true) {
                    SimulationModel.resetBarrier();
                    SimulationModel.reset = false;
                }
            }

            SimulationModel.resetting = SimulationModel.resetting - 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    // fixed onto block by EPS //
    public void attach(Block block) {
        System.out.println(Thread.currentThread().getName() + " is calling attaching");
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Attach:("
                + block.getXPos() + "," + block.getYPos() + ")");

        synchronized (waiting) {
            stuck = true;

            if ((position.getXPos() == 0) & (position.getYPos() == 0)) {
                if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() + 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
            } else if ((position.getXPos() == environ.getxBlocks()) & (position.getYPos() == environ.getyBlocks())) {
                if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() - 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
            } else if ((position.getYPos() == 0)) {
                if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() + 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() + 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
            } else if ((position.getXPos() == 0)) {
                if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() - 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() + 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
            } else if ((position.getXPos() == environ.getxBlocks())) {
                if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() - 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() + 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
                if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            + 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                + 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() + 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    + 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
            } else if ((position.getYPos() == environ.getyBlocks())) {
                if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() - 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() - 1][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos()][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos()][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
                if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()].occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                            .getYPos()].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position
                                .getYPos()].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position
                                    .getYPos()].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                } else if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() - 1]
                        .occupied()) {
                    if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                            - 1].occupier.stuck) {
                        if (Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                - 1].occupier.inB == true) {
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    - 1].occupier.biofilm.addBac(this);
                        } else {
                            this.biofilm = new Biofilm(2, this.position.getEPSLevel() +
                                    Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos() - 1]
                                            .getEPSLevel(),
                                    0);
                            Environment.environBlocks[this.position.getXPos() + 1][this.position.getYPos()
                                    - 1].occupier.biofilm = this.biofilm;
                            System.out.println("biofilm formed");
                        }
                    }
                }
            }
        }

        try {

            SimulationModel.barrier.await();

            synchronized (waiting) {
                if (SimulationModel.reset == true) {
                    SimulationModel.resetBarrier();
                    SimulationModel.reset = false;
                }
            }
            SimulationModel.resetting = SimulationModel.resetting - 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    // decrease nutrient count, what does it do to bacterium //
    public void eat(Nutrient nutrient) {
        System.out.println(Thread.currentThread().getName() + " is calling eating");
        // synchronizes on LinkedList of nutrients for block
        synchronized (nutrient.position.nutrients) {
            nutrient.position.removeNutrient(nutrient);
        }

        synchronized (environ.nutrients) {
            environ.nutrients.remove(nutrient);
        }

        this.energy += 1; // increases energy level
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Eat");

        try {

            SimulationModel.barrier.await();

            synchronized (waiting) {
                if (SimulationModel.reset == true) {
                    SimulationModel.resetBarrier();
                    SimulationModel.reset = false;
                }
            }
            SimulationModel.resetting = SimulationModel.resetting - 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void consume(BacterialMonomer bMonomer) {
        bMonomer.position.removeBMonomer(bMonomer);
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Consume:BacterialMonomer:" + bMonomer.getID());
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

    }

    public void idle(Block pos) {

        System.out.println(Thread.currentThread().getName() + " is idling");
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Idle:" + pos.getStringFormat());
        try {
            System.out.println(Thread.currentThread().getName() + " waiting at idle barrier");
            SimulationModel.barrier.await();
            synchronized (waiting) {
                if (SimulationModel.reset == true) {
                    SimulationModel.resetBarrier();
                    SimulationModel.reset = false;
                }
            }
            SimulationModel.resetting = SimulationModel.resetting - 1;

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " is done idling");
    }

    public void run() {
        this.thread = Thread.currentThread();
        synchronized (waiting) {
            if (Environment.Bacteria.size() > SimulationModel.iBacteria) {
                try {
                    waiting.wait();
                    waiting.notifyAll();
                    System.out.println(Thread.currentThread().getName() + "got here");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            environ.initialise.countDown();
            environ.initialise.await();

            Random random = new Random();
            this.newGoal = true;
            System.out.println(Thread.currentThread().getName()
                    + ", executing run() method!");
            this.birthTime = LocalDateTime.now();
            this.length = monomers.size(); // gets current length of bacterium which is 7 monomers long

            // scheduling the task at interval
            timer.schedule(grow, 0, 1000); // growth rate is per second

            while (!killThread) {
                synchronized (waiting) {
                    if (SimulationModel.resetting == 0) {
                        SimulationModel.reset = true;
                        SimulationModel.resetting = SimulationModel.iBacteria;
                        System.out.println(SimulationModel.resetting);
                    }
                }

                synchronized (this) {
                    // Check if the bacterium should reproduce
                    if (this.length >= 14) {
                        this.setAction("reproduce", null);
                    } else if (this.position.EPSLevel >= 100) {
                        this.setAction("attach", this.position);
                    } else if (this.energy == 0 || this.stuck) {
                        this.setAction("idle", null);
                    } else if (currentAction.isEmpty()) {
                        // If no current action, choose one
                        if (newGoal == true) {
                            while (Environment.environBlocks[x][y].occupied() == true) {
                                x = mt.nextInt(environ.xBlocks);
                                y = mt.nextInt(environ.yBlocks);
                            }
                        }
                        // this.setAction("runMove", Environment.environBlocks[x][y]);
                        this.actionChosen = this.doSomething(Environment.environBlocks);
                        // System.out.println((String) this.actionChosen[0]);
                        this.setAction((String) this.actionChosen[0], (Block) this.actionChosen[1]);
                    }
                    // Perform the chosen action
                    if (currentAction.equals("run")) {
                        System.out.println("Starting runMove action");
                        this.runMove(this.moveDestination); // Run move action
                    } else if (currentAction.equals("tumble")) {
                        // System.out.println("Starting tumbleMove action");
                        this.tumbleMove(this.moveDestination); // Tumble move action
                    } else if (currentAction.equals("reproduce")) {
                        Block newPos = this.getRandomAdjacentFreeBlock();
                        this.reproduce(newPos);
                    } else if (currentAction.equals("attach")) {
                        this.attach(this.moveDestination);
                    } else if (currentAction.equals("idle")) {
                        this.idle(this.position);
                    } else if (currentAction.equals("die")) {
                        this.die();
                    } else if (currentAction.equals("secrete")) {
                        this.secrete();
                    } else if (currentAction.equals("eat")) {
                        this.eat(new Nutrient(this.position, 900));
                    }
                    currentAction = "";
                    // System.out.println("action has been reset");
                    // Sleep for one second to enforce one action per second
                    Thread.sleep(1000);
                }

            }

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public Thread getThread() {
        return this.thread;
    }

    class grow extends TimerTask {
        private Bacterium bacterium;

        public grow(Bacterium bacterium) {
            this.bacterium = bacterium;
        }

        @Override
        public void run() {

            // Calculate the time elapsed since birth
            Duration timeElapsed = Duration.between(bacterium.birthTime, LocalDateTime.now());

            // Convert the elapsed time into hours (floating point for better precision)
            double timeElapsedSeconds = timeElapsed.toSeconds();

            // Update the length based on the growth rate, limiting it by doubling time
            bacterium.length = 7 + scaledGrowthRate * Math.min(timeElapsedSeconds,
                    1.2 * scaledDoublingTime); // scaled time bounds as well

            // System.out.println(bacterium.getThread().getName() + " size is : " +
            // bacterium.length);

        }
    }

    public synchronized void setAction(String action, Block destination) {
        this.currentAction = action;
        this.moveDestination = destination;
        notify(); // Notify the thread to wake up and perform the action
    }

    public void spawn() {
        this.birthTime = LocalDateTime.now();
        this.length = monomers.size(); // gets current length of bacterium which is 7 monomers long
        if (father == null) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Spawn:" + "(" + position.getXPos() + ","
                    + position.getYPos() + ")");
        }

    }

    // method that moves a bacterium from a start to a goal block
    public void move(Block start, Block end, Block[][] environBlocks, String moveType) {

        System.out.println(Thread.currentThread().getName() + " is starting move action");
        Block proposedBlock = null;
        boolean blockFound = false;
        Block position = start;

        if (!position.compareTo(end)) {
            newGoal = false;
            start = position;
            System.out.println(Thread.currentThread().getName() + " is prep to move");
            while (!blockFound) {
                System.out.println(Thread.currentThread().getName() + " is looking");
                // First check left
                if (start.getXPos() > end.getXPos()) {
                    proposedBlock = environBlocks[start.getXPos() - 1][start.getYPos()]; // move one left
                    // System.out.println(Thread.currentThread().getName() + " is looking left at "
                    // + proposedBlock.getStringFormat());
                    if (!proposedBlock.occupied()) {
                        blockFound = true; // Exit the loop if block is unoccupied
                        break;
                    }
                }

                // Then check right
                if (start.getXPos() < end.getXPos()) {
                    proposedBlock = environBlocks[start.getXPos() + 1][start.getYPos()]; // move one right
                    // System.out.println(Thread.currentThread().getName() + " is looking right at "
                    // + proposedBlock.getStringFormat());
                    if (!proposedBlock.occupied()) {
                        blockFound = true; // Exit the loop if block is unoccupied
                        break;
                    }
                }

                // Then check up
                if (start.getYPos() < end.getYPos()) {
                    proposedBlock = environBlocks[start.getXPos()][start.getYPos() + 1]; // move one up
                    if (!proposedBlock.occupied()) {
                        blockFound = true; // Exit the loop if block is unoccupied
                        break;
                    }
                }

                // Finally check down
                if (start.getYPos() > end.getYPos()) {
                    proposedBlock = environBlocks[start.getXPos()][start.getYPos() - 1]; // move one down
                    if (!proposedBlock.occupied()) {
                        blockFound = true; // Exit the loop if block is unoccupied
                        break;
                    }
                }

                // If all directions are checked and all are occupied, you may want to handle
                // the case
                // of no available unoccupied blocks here.
                if (!blockFound) {
                    System.out.println(Thread.currentThread().getName() + " could not find an unoccupied block.");
                    break;
                }
            }
            this.position = proposedBlock;

            synchronized (position) {
                while (position.occupied()) {
                    try {
                        position.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.energy -= 1; // decreases energy each movement
                position.setOccupied(true);
                position.occupier = this;
            }

            synchronized (start) {
                start.setOccupied(false);
                start.occupier = null;
                start.notifyAll();
            }
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType + ":("
                    + start.getXPos() + "," + start.getYPos() + "):"
                    + "(" + this.position.getXPos() + "," + this.position.getYPos() + ")");
            try {

                SimulationModel.barrier.await();

                synchronized (waiting) {
                    if (SimulationModel.reset == true) {
                        SimulationModel.resetBarrier();
                        SimulationModel.reset = false;
                    }
                }

                SimulationModel.resetting = SimulationModel.resetting - 1;

            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        if (position.compareTo(end)) {
            newGoal = true;
        }
        System.out.println(Thread.currentThread().getName() + " has moved to (" + this.position.getXPos() + ","
                + this.position.getYPos() + ")");
    }

    // method that determines what the bacterium does
    public Object[] doSomething(Block[][] environBlocks) throws InterruptedException,
            BrokenBarrierException {
        String action = "";
        boolean accepted = false;
        int maxDistance = (int) Math
                .sqrt(Math.pow(environ.getxBlocks(), 2) + Math.pow(environ.getyBlocks(), 2));

        int event = mt.nextInt(6); // Mersenne Twister generates event
        System.out.println(event);
        if ((position.getXPos() == 0) & (position.getYPos() == 0)) {
            if (environBlocks[position.getXPos()][position.getYPos() + 1].occupied() ||
                    environBlocks[position.getXPos() + 1][position.getYPos() + 1].occupied() ||
                    environBlocks[position.getXPos() + 1][position.getYPos()].occupied()) {
                action = "secrete";
                event = 8;
            }
        } else if ((position.getXPos() == environ.getxBlocks()) & (position.getYPos() == environ.getyBlocks())) {
            if (environBlocks[position.getXPos() - 1][position.getYPos()].occupied()
                    || environBlocks[position.getXPos() - 1][position.getYPos() - 1].occupied()
                    || environBlocks[position.getXPos()][position.getYPos() - 1].occupied()) {
                action = "secrete";
                event = 8;
            }
        } else if ((position.getXPos() == 0)) {
            if (environBlocks[position.getXPos()][position.getYPos() + 1].occupied() ||
                    environBlocks[position.getXPos() + 1][position.getYPos() + 1].occupied() ||
                    environBlocks[position.getXPos() + 1][position.getYPos()].occupied() ||
                    environBlocks[position.getXPos()][position.getYPos() - 1].occupied() ||
                    environBlocks[position.getXPos() + 1][position.getYPos() - 1].occupied()) {
                action = "secrete";
                event = 8;
            }
        } else if ((position.getYPos() == 0)) {
            if (environBlocks[position.getXPos() - 1][position.getYPos() + 1].occupied()
                    || environBlocks[position.getXPos()][position.getYPos() + 1].occupied()
                    || environBlocks[position.getXPos() + 1][position.getYPos() + 1].occupied()
                    || environBlocks[position.getXPos() - 1][position.getYPos()].occupied()
                    || environBlocks[position.getXPos() + 1][position.getYPos()].occupied()) {
                action = "secrete";
                event = 8;
            }
        } else if ((position.getXPos() == environ.getxBlocks())) {
            if (environBlocks[position.getXPos() - 1][position.getYPos() + 1].occupied()
                    || environBlocks[position.getXPos()][position.getYPos() + 1].occupied()
                    || environBlocks[position.getXPos() - 1][position.getYPos()].occupied()
                    || environBlocks[position.getXPos() - 1][position.getYPos() - 1].occupied()
                    || environBlocks[position.getXPos()][position.getYPos() - 1].occupied()) {
                action = "secrete";
                event = 8;
            }
        } else if ((position.getYPos() == environ.getyBlocks())) {
            if (environBlocks[position.getXPos() - 1][position.getYPos()].occupied()
                    || environBlocks[position.getXPos() + 1][position.getYPos()].occupied()
                    || environBlocks[position.getXPos() - 1][position.getYPos() - 1].occupied()
                    || environBlocks[position.getXPos()][position.getYPos() - 1].occupied()
                    || environBlocks[position.getXPos() + 1][position.getYPos() - 1].occupied()) {
                action = "secrete";
                event = 8;
            }
        }
        switch (event) {
            case 0: // tumble

                accepted = false;

                while (!accepted && (newGoal == true)) { // while the coorodinate is not valid try another one
                    x = mt.nextInt(environ.xBlocks); // Generating coordinate
                    y = mt.nextInt(environ.getyBlocks());

                    if ((position.getXPos() == 0) & (position.getYPos() == 0)) {
                        if ((environBlocks[position.getXPos()][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos()].getEPSLevel() >= 20)) {
                            accepted = true;
                        }
                    } else if ((position.getXPos() == environ.getxBlocks())
                            & (position.getYPos() == environ.getyBlocks())) {
                        if ((environBlocks[position.getXPos() - 1][position.getYPos()].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() - 1][position.getYPos() - 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos()][position.getYPos() - 1].getEPSLevel() >= 20)) {
                            accepted = true;
                        }
                    } else if ((position.getXPos() == 0)) {
                        if ((environBlocks[position.getXPos()][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos()].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos()][position.getYPos() - 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos() - 1]
                                        .getEPSLevel() >= 20)) {
                            accepted = true;
                        }
                    } else if ((position.getYPos() == 0)) {
                        if ((environBlocks[position.getXPos() - 1][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos()][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() - 1][position.getYPos()].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos()].getEPSLevel() >= 20)) {
                            accepted = true;
                        }
                    } else if ((position.getXPos() == environ.getxBlocks())) {
                        if ((environBlocks[position.getXPos() - 1][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos()][position.getYPos() + 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() - 1][position.getYPos()].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() - 1][position.getYPos() - 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos()][position.getYPos() - 1].getEPSLevel() >= 20)) {
                            accepted = true;
                        }
                    } else if ((position.getYPos() == environ.getyBlocks())) {
                        if ((environBlocks[position.getXPos() - 1][position.getYPos()].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos()].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() - 1][position.getYPos() - 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos()][position.getYPos() - 1].getEPSLevel() >= 20)
                                || (environBlocks[position.getXPos() + 1][position.getYPos() - 1].getEPSLevel() >= 20)
                                || environBlocks[x][y].getEPSLevel() >= 20) {
                            accepted = true;
                        }
                    }

                    int distance = (int) Math // calculating distance, used to validate coordinate
                            .sqrt(Math.pow(x - position.getXPos(), 2) + Math.pow(y - position.getYPos(), 2));

                    if (distance >= (maxDistance) / 4) { // if the distance is too large for a tumble reject the
                                                         // coordinate and try again
                        accepted = false;
                    }
                    int energyLevel = (int) ((double) this.energy / maxEnergy * 100); // calculating energy level to be
                                                                                      // used to validae coordinate

                    if (energyLevel >= 80) { // validating coordinate
                        accepted = true;
                    } else if (energyLevel >= 60 && distance <= (maxDistance * 4) / 5) {
                        accepted = true;
                    } else if (energyLevel >= 40 && distance <= (maxDistance * 3) / 5) {
                        accepted = true;
                    } else if (energyLevel >= 20 && distance <= (maxDistance * 2) / 5) {
                        accepted = true;
                    } else if (energyLevel >= 1 && distance <= (maxDistance * 1) / 5) {
                        accepted = true;
                    } else {
                        accepted = false;

                        System.out.println("found it");
                    }

                }
                action = "tumble";
                goal = environBlocks[x][y];
                break;
            // System.out.println("tumbling");
            // return new Object[] { moveType, goal };
            case 1: // die
                if (this.probDie == 100) {
                    probDie = 0;
                    action = "die";

                } else if (((this.age) / maxAge) * 100 <= 20) { // adds to probDie based on liklihood of bacterium dying
                    probDie += 2;
                } else if (((this.age) / maxAge) * 100 <= 40) {
                    probDie += 4;
                } else if (((this.age) / maxAge) * 100 <= 60) {
                    probDie += 5;
                } else if (((this.age) / maxAge) * 100 <= 80) {
                    probDie += 10;
                } else {
                    probDie += 15;
                }
                // System.out.println("dying");
                // return new Object[] { action, null };
                break;
            case 2: // eat
                if (this.probEat == 100) {
                    this.probEat = 0;
                    action = "eat";

                } else if ((this.energy / this.maxEnergy) * 100 <= 40) { // adds to probEat based on liklihood of
                                                                         // bacterium eating
                    probEat += 20;

                } else if ((this.energy / this.maxEnergy) * 100 <= 60) {
                    probEat += 15;

                } else if ((this.energy / this.maxEnergy) * 100 <= 80) {
                    probEat += 10;

                } else {
                    probEat += 5;
                }
                break;
            // System.out.println("eating");
            // return new Object[] { action, null };
            case 3:// run
                accepted = false;
                while (!accepted && (newGoal == true)) { // while the coorodinate is not valid try another one
                    // x = mt.nextInt(environ.xBlocks); // Generating coordinate
                    // y = mt.nextInt(environ.getyBlocks());

                    int distance = (int) Math // calculating distance (needed to validate coordinate)
                            .sqrt(Math.pow(x - position.getXPos(), 2) + Math.pow(y - position.getYPos(), 2));

                    int energyLevel = (int) ((double) this.energy / maxEnergy * 100);// calculating energy level to be
                                                                                     // used to validae coordinate
                    System.out.println(
                            Thread.currentThread().getName() + " is waiting acceptance with energy "
                                    + energyLevel);
                    if (energyLevel >= 80) { // validating coordinate
                        accepted = true;
                    } else if (energyLevel >= 60 && distance <= (maxDistance * 4) / 5) {
                        accepted = true;
                    } else if (energyLevel >= 40 && distance <= (maxDistance * 3) / 5) {
                        accepted = true;
                    } else if (energyLevel >= 20 && distance <= (maxDistance * 2) / 5) {
                        accepted = true;
                    } else if (energyLevel >= 1 && distance <= (maxDistance * 1) / 5) {
                        accepted = true;
                    } else {
                        accepted = false;
                    }
                }
                action = "run";
                goal = environBlocks[x][y];
                break;
            // return new Object[] { moveType, goal };
            case 4: // secrete
                action = "secrete";
                break;
            // return new Object[] { "secrete", null };
            case 5: // do nothing
                action = "idle";
                break;
            // return new Object[] { "idle", null };

        }
        // System.out.println(event);
        System.out.println(action);
        if (action == "") {
            return new Object[] { "run", goal };
        } else {
            return new Object[] { action, goal };
        }

    }

}
