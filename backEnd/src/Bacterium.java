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
    Block position;
    int bacteriumID;
    String strain;
    int age;
    Bacterium father;
    ArrayList<BacterialMonomer> monomers;
    Environment environ;
    public volatile String waiting = "wait";
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

        this.scalingFactor = 5 / (this.doublingTime * 3600); // choose intial number but can change
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

    public void tumbleMove(Block iBlock, Block fBlock) {
        move(iBlock, fBlock, Environment.environBlocks, "Tumble");
    }

    public void runMove(Block iBlock, Block fBlock) {
        move(iBlock, fBlock, Environment.environBlocks, "Run");
    }

    public void reproduce(Block position) {
        System.out.println(Thread.currentThread().getName() + " is reproducing!");
        // System.out.println("Bacteria " + this.bacteriumID + " is reproducing!");
        // this.resetMonomers(); // resets father bacterium to 7 monomers
        this.length = 7;
        this.birthTime = LocalDateTime.now();
        Bacterium childBac;
        try {
            childBac = environ.createBacterium(environ, position, this);

            // synchronized (waiting) {
            // System.out.println(
            // Thread.currentThread().getName() + " is waiting for the child bacterium to
            // start running...");
            // waiting.wait(); // Wait until the child notifies that it has started running
            // }
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + childBac.getBID());
        } catch (InterruptedException | BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // creates child bacterium
        try {
            SimulationModel.barrier.await();
            SimulationModel.resetBarrier();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    // bacterium dies, do all bacterial monomers die as well //
    public void die() {

        synchronized (Environment.Bacteria) {
            Environment.Bacteria.remove(this);
        }

        position.setOccupied(false); // makes block free
        this.killThread = true;

        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Die");
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public Block getRandomAdjacentFreeBlock(Block position) {

        Random random = new Random();
        int randomBlock = random.nextInt(4); // Generates a number from 0 to 3
        int newYcoord, newXcoord = 0;
        Block destinationPosition = this.position;

        switch (randomBlock) { // moves bacterium to a random adjacent block
            case 0:
                newXcoord = this.position.getXPos() - 1;
                destinationPosition.setXPos(newXcoord);

                synchronized (destinationPosition) {
                    while (destinationPosition.occupied()) {
                        try {
                            destinationPosition.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    destinationPosition.setOccupied(true);
                }

                break;
            case 1:
                newXcoord = this.position.getXPos() + 1;
                destinationPosition.setXPos(newXcoord);

                synchronized (destinationPosition) {
                    while (destinationPosition.occupied()) {
                        try {
                            destinationPosition.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    destinationPosition.setOccupied(true);
                }

                break;
            case 2:
                newYcoord = this.position.getYPos() - 1;
                destinationPosition.setYPos(newYcoord);

                synchronized (destinationPosition) {
                    while (destinationPosition.occupied()) {
                        try {
                            destinationPosition.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    destinationPosition.setOccupied(true);
                }

                break;
            case 3:
                newYcoord = this.position.getYPos() + 1;
                destinationPosition.setYPos(newYcoord);

                synchronized (destinationPosition) {
                    while (destinationPosition.occupied()) {
                        try {
                            destinationPosition.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    destinationPosition.setOccupied(true);
                }

                break;
        }
        return destinationPosition;
    }

    // hits another bacterium but doesn't stick together //
    public void collide(Bacterium bac) {
        Block destinationPosition = getRandomAdjacentFreeBlock(this.position);

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

        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Secrete:EPS");
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public void idle() {
        System.out.println(Thread.currentThread().getName() + " is idling");

        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Idle");
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    // fixed onto block by EPS //
    public void attach(Block block) {
        System.out.println(Thread.currentThread().getName() + " is calling attaching");
        synchronized (waiting) {
            stuck = true;
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Attach:("
                    + block.getXPos() + "," + block.getYPos() + ")");
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
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
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Eat");
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
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

    public void run() {
        // synchronized (waiting) {
        // System.out.println(Thread.currentThread().getName() + " child bacterium is
        // notifying parent.");
        // waiting.notify();
        // }

        try {
            environ.initialise.countDown();
            environ.initialise.await();

            System.out.println(Thread.currentThread().getName()
                    + ", executing run() method!");
            this.birthTime = LocalDateTime.now();
            this.length = monomers.size(); // gets current length of bacterium which is 7 monomers long

            // scheduling the task at interval
            timer.schedule(grow, 0, 1000); // growth rate is per second

            while (!killThread) {
                synchronized (this) {

                    this.doSomething(Environment.environBlocks);

                    // Sleep for one second to enforce one action per second
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

            System.out.println(Thread.currentThread().getName() + " size is : " + bacterium.length);

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
        System.out.println(Thread.currentThread().getName() + " is calling move");
        Block position = start;

        if (!position.compareTo(end)) {
            start = position;

            if (start.getXPos() > end.getXPos()) {
                position = environBlocks[start.getXPos() - 1][start.getYPos()]; // move one left
            } else if (start.getXPos() < end.getXPos()) {
                position = environBlocks[start.getXPos() + 1][start.getYPos()]; // move one right
            }

            if (start.getYPos() < end.getYPos()) {
                position = environBlocks[start.getXPos()][start.getYPos() + 1]; // move one up
            }

            else if (start.getYPos() > end.getYPos()) {
                position = environBlocks[start.getXPos()][start.getYPos() - 1]; // move one down
            }
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
            }
            synchronized (start) {
                start.setOccupied(false);
                start.notifyAll();
            }

            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType + ":("
                    + start.getXPos() + "," + start.getYPos() + "):"
                    + "(" + position.getXPos() + "," + position.getYPos() + ")");

            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

        }
    }

    public void callTest() {
        System.out.println(Thread.currentThread().getName() + " is reproducing!");
        // System.out.println("Bacteria " + this.bacteriumID + " is reproducing!");
        // this.resetMonomers(); // resets father bacterium to 7 monomers
        this.length = 7;
        this.birthTime = LocalDateTime.now();
        Bacterium childBac;
        try {
            childBac = environ.createBacterium(environ, position, this);
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + childBac.getBID());
            SimulationModel.barrier.await();
            SimulationModel.resetBarrier();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // method that determines what the bacterium does
    public void doSomething(Block[][] environBlocks) throws InterruptedException, BrokenBarrierException {
        System.out.println(Thread.currentThread().getName() + " has entered");
        int x = 0;
        int y = 0;
        boolean accepted = false;

        int maxDistance = (int) Math
                .sqrt(Math.pow(environ.getxBlocks(), 2) + Math.pow(environ.getyBlocks(), 2));
        // System.out.println(Thread.currentThread().getName() + " is checking the
        // bacterium " + this.bacteriumID
        // + " whose length is " + this.length);
        if (this.length >= 14) {
            System.out.println(Thread.currentThread().getName() + " IS ABOUT TO reproduce!");

            // try {
            // this.reproduce(getRandomAdjacentFreeBlock(this.position));
            // } catch (Exception e) {
            // e.printStackTrace(); // Log exceptions to identify the root cause
            // }
            this.callTest();
            System.out.println("called reproduce!");
        } else if (position.EPSLevel >= 100) {
            System.out.println("attaching!");
            this.attach(position);

        } else if (goal.compareTo(position)) {
            System.out.println("got to goal");
            newGoal = true;
        } else {
            int event = mt.nextInt(6);

            switch (event) {
                case 0: // tumble
                    System.out.println("calling tumble");
                    if (energy == 0 || stuck) {
                        break;
                    }
                    accepted = false;

                    // while (!accepted) {
                    x = mt.nextInt(environ.getxBlocks() / 2);
                    y = mt.nextInt(environ.getyBlocks()) / 2;
                    // int distance = (int) Math
                    // .sqrt(Math.pow(x - position.getXPos(), 2) + Math.pow(y - position.getYPos(),
                    // 2));

                    // if (distance >= (maxDistance) / 4) { // if the distance is too far for a
                    // tumble do another
                    // // tumble
                    // accepted = false;
                    // }
                    // int energyLevel = this.energy / maxEnergy * 100; // checks if co-ordinates
                    // are viable for the
                    // // bacterium
                    // if (energyLevel >= 80) {
                    // accepted = true;
                    // } else if (energyLevel >= 60 && distance <= (maxDistance * 4) / 5) {
                    // accepted = true;
                    // } else if (energyLevel >= 40 && distance <= (maxDistance * 3) / 5) {
                    // accepted = true;
                    // } else if (energyLevel >= 20 && distance <= (maxDistance * 2) / 5) {
                    // accepted = true;
                    // } else if (energyLevel >= 1 && distance <= (maxDistance * 1) / 5) {
                    // accepted = true;
                    // } else {
                    // accepted = false;
                    // }

                    // }
                    moveType = "tumble";
                    if (newGoal == true) {
                        goal = environBlocks[x][y];
                        newGoal = false;
                    }
                    this.move(position, goal, environBlocks, moveType); // make bacterium go to that block

                    break;

                case 1: // die
                    System.out.println("calling die");
                    // if (this.probDie == 100) {
                    // probDie = 0;
                    // this.die();

                    // } else if (((this.age) / maxAge) * 100 <= 20) { // adds to probDie based on
                    // liklihood of bacterium
                    // // dying
                    // probDie += 2;
                    // } else if (((this.age) / maxAge) * 100 <= 40) {
                    // probDie += 4;
                    // } else if (((this.age) / maxAge) * 100 <= 60) {
                    // probDie += 5;
                    // } else if (((this.age) / maxAge) * 100 <= 80) {
                    // probDie += 10;
                    // } else {
                    // probDie += 15;
                    // }
                    try {
                        SimulationModel.barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    System.out.println(" didn't die");
                    break;
                case 2: // eat
                    System.out.println(" is calling eat");
                    if (environ.nutrients.isEmpty()) {
                        break;
                    }
                    if (this.probEat == 100) {
                        this.probEat = 0;

                        this.eat(environ.nutrients.get(-1));// needs to do something
                        break;

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
                    try {
                        SimulationModel.barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    System.out.println(" didn't eat");
                    break;
                case 3:// run
                    System.out.println(" is calling run");
                    if (energy == 0 || stuck) {
                        break;
                    }
                    accepted = false;

                    // while (!accepted) {
                    x = mt.nextInt(environ.getxBlocks() / 2);
                    y = mt.nextInt(environ.getyBlocks()) / 2;
                    // int distance = (int) Math
                    // .sqrt(Math.pow(x - position.getXPos(), 2) + Math.pow(y - position.getYPos(),
                    // 2));

                    // int energyLevel = this.energy / maxEnergy * 100; // checks if co-ordinates
                    // are viable for the
                    // // bacterium
                    // if (energyLevel >= 80) {
                    // accepted = true;
                    // } else if (energyLevel >= 60 && distance <= (maxDistance * 4) / 5) {
                    // accepted = true;
                    // } else if (energyLevel >= 40 && distance <= (maxDistance * 3) / 5) {
                    // accepted = true;
                    // } else if (energyLevel >= 20 && distance <= (maxDistance * 2) / 5) {
                    // accepted = true;
                    // } else if (energyLevel >= 1 && distance <= (maxDistance * 1) / 5) {
                    // accepted = true;
                    // } else {
                    // accepted = false;
                    // }

                    // }
                    moveType = "run";
                    if (goal == null) {
                        goal = environBlocks[x][y];
                    }
                    this.move(position, goal, environBlocks, moveType); // make bacterium go to that block

                    break;
                case 4: // secrete
                    System.out.println(" is calling secrete");
                    this.secrete();
                    break;
                case 5: // do nothing
                    System.out.println("Bacterium " + this.bacteriumID + " is idle.");
                    this.idle();
                    break;
            }

        }
        // if (SimulationModel.barrier.getNumberWaiting() == 0)
        // {
        // SimulationModel.resetBarrier();
        // }

    }

}
