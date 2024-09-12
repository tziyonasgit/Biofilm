package backEnd.src;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import backEnd.src.SimulationModel.*;

// class for managing bacterium with methods to manipulate them (activities)
public class Bacterium implements Runnable {
    Block position;
    int bacteriumID;
    String strain;
    int age;
    Bacterium father;
    BacterialMonomer[] monomers;
    Environment environ;
    public volatile String waiting = "hi";
    private volatile String currentAction = "";
    private Block moveDestination;
    int energy;
    boolean killThread;
    LocalDateTime birthTime;
    double doublingTime = 0; // Time in hours for bacterium to double in size
    Timer timer = new Timer();
    TimerTask grow = new grow();
    MersenneTwister mt;
    int maxAge = 100;
    int maxEnergy = 100;
    int probDie = 0;
    int probEat = 0;
    int probGrow = 0;
    boolean stuck = false;
    double length;
    double scalingFactor;
    double scaledGrowthRate;
    double scaledDoublingTime;

    // paramaterised constructor for bacterium
    public Bacterium(Block position, int ID, int age, Bacterium father,
            BacterialMonomer[] monomers, String strain, Environment environ, long seed) {
        this.position = position;
        this.bacteriumID = ID; // count of IDs
        this.age = age;
        this.father = father;
        this.monomers = new BacterialMonomer[14];
        this.strain = strain;
        this.environ = environ;
        this.energy = 0;
        this.killThread = false;
        this.doublingTime = generateDoublingTime(1.0); // time is in hours
        this.mt = new MersenneTwister(seed);

        this.scalingFactor = 60 / (this.doublingTime * 3600); // choose intial number but can change
        this.scaledGrowthRate = (1 / this.scalingFactor) * (7 / (this.doublingTime * 3600)); // units/second
        this.scaledDoublingTime = (1 / this.scalingFactor) * (this.doublingTime * 3600);

    }

    public static double generateDoublingTime(double mean) {
        // Generate a random uniform number between 0 and 1
        double u = ThreadLocalRandom.current().nextDouble();

        // Use the inverse transform method to get an exponentially distributed value
        return -mean * Math.log(1 - u);
    }

    public void resetMonomers() {
        BacterialMonomer[] tempArray = new BacterialMonomer[14];
        System.arraycopy(this.monomers, 0, tempArray, 0, 7);
        this.monomers = tempArray;
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
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Tumble:("
        // + iBlock.getXPos() + "," + iBlock.getYPos() + ")"
        // + "(" + fBlock.getXPos() + "," + fBlock.getYPos() + ")");
        move(iBlock, fBlock, environ.environBlocks, "Tumble");
    }

    public void runMove(Block iBlock, Block fBlock) {
        move(iBlock, fBlock, environ.environBlocks, "Run");
    }

    public void reproduce(Block position) {
        Bacterium childBac = environ.createBacterium(environ, position, this); // creates child bacterium
        this.resetMonomers(); // resets father bacterium to 7 monomers
        this.length = monomers.length;
        // synchronizes on waiting to ensure that only one bacterium calls recActivities
        // in Simulation.java
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + childBac.getBID());
            try {
                SimulationModel.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
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
                break;
            case 1:
                newXcoord = this.position.getXPos() + 1;
                destinationPosition.setXPos(newXcoord);
                break;
            case 2:
                newYcoord = this.position.getYPos() - 1;
                destinationPosition.setYPos(newYcoord);
                break;
            case 3:
                newYcoord = this.position.getYPos() + 1;
                destinationPosition.setYPos(newYcoord);
                break;
        }
        return destinationPosition;
    }

    // hits another bacterium but doesn't stick together //
    public void collide(Bacterium bac) {
        Block destinationPosition = getRandomAdjacentFreeBlock(this.position);

        move(this.position, destinationPosition, environ.environBlocks, "Run");
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

    // fixed onto block by EPS //
    public void attach(Block block) {
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
        // waits on countdownlatch initialise to ensure all bacteria start their main
        // functioning
        // simultaneously
        try {
            environ.initialise.countDown();
            environ.initialise.await();

            System.out.println(Thread.currentThread().getName() + ", executing run() method!");
            spawn();
            // grow(SimulationModel.duration);

            // scheduling the task at interval
            timer.schedule(grow, 0, 1000); // growth rate is per second

            while (!killThread) {
                synchronized (this) {
                    // Wait for an action to be set
                    while (currentAction.isEmpty()) {
                        wait(); // Wait until an action is set
                    }

                    // Perform the action based on the external command
                    if (currentAction.equals("runMove")) {
                        runMove(this.getBlock(), moveDestination); // Run move action
                    } else if (currentAction.equals("tumbleMove")) {
                        tumbleMove(this.getBlock(), moveDestination); // Tumble move action
                    }

                    // Clear the action after performing
                    currentAction = "";
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    class grow extends TimerTask {
        @Override
        public void run() {
            // Check if the bacterium should reproduce
            if (length >= 14) {
                reproduce(getRandomAdjacentFreeBlock(position));
            }

            // Calculate the time elapsed since birth
            Duration timeElapsed = Duration.between(birthTime, LocalDateTime.now());

            // Convert the elapsed time into hours (floating point for better precision)
            double timeElapsedSeconds = timeElapsed.toSeconds();

            // Update the length based on the growth rate, limiting it by doubling time
            length = 7 + scaledGrowthRate * Math.min(timeElapsedSeconds,
                    1.2 * scaledDoublingTime); // scaled time bounds as well

            // Print information for debugging
            System.out.println("Scaled growth rate is : " + scaledGrowthRate + " units per second");
            System.out.println("Time elapsed since birth : " + timeElapsed);
            System.out.println("My size is : " + length);
        }
    }

    public synchronized void setAction(String action, Block destination) {
        this.currentAction = action;
        this.moveDestination = destination;
        notify(); // Notify the thread to wake up and perform the action
    }

    public void spawn() {
        this.birthTime = LocalDateTime.now();
        this.length = monomers.length; // gets current length of bacterium which is 7 monomers long
        if (father == null) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Spawn:" + "(" + position.getXPos() + ","
                    + position.getYPos() + ")");
        }
        try {
            SimulationModel.barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    // method that moves a bacterium from a start to a goal block
    public void move(Block start, Block end, Block[][] environBlocks, String moveType) {

        Block position = start;

        while (!position.compareTo(end)) {
            start = position;
            start.setOccupied(false);
            if (start.getXPos() > end.getXPos()) {
                position = environBlocks[start.getXPos() - 1][start.getYPos()]; // move one left
            } else if (start.getXPos() < end.getXPos()) {
                position = environBlocks[start.getXPos() + 1][start.getYPos()]; // move one right
            }

            else if (start.getYPos() < end.getYPos()) {
                position = environBlocks[start.getXPos()][start.getYPos() + 1]; // move one up
            }

            else if (start.getYPos() > end.getYPos()) {
                position = environBlocks[start.getXPos()][start.getYPos() - 1]; // move one down
            }
            this.energy -= 1; // decreases energy each movement
            position.setOccupied(true);

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

    // method that determines what the bacterium does
    public void doSomething(Block[][] environBlocks) {
        int event = mt.nextInt(6);
        int x = 0;
        int y = 0;
        boolean accepted = false;
        int maxDistance = (int) Math
                .sqrt(Math.pow(environ.getxBlocks(), 2) + Math.pow(environ.getyBlocks(), 2));
        if (position.EPSLevel >= 100) {
            this.attach(position);
        }

        switch (event) {
            case 0: // tumble
                if (energy == 0 || stuck) {
                    break;
                }
                accepted = false;

                while (!accepted) {
                    x = mt.nextInt(environ.getxBlocks() / 2);
                    y = mt.nextInt(environ.getyBlocks()) / 2;
                    int distance = (int) Math
                            .sqrt(Math.pow(x - position.getXPos(), 2) + Math.pow(y - position.getYPos(), 2));

                    if (distance >= (maxDistance) / 4) { // if the distance is too far for a tubmle do another tumble
                        accepted = false;
                    }
                    int energyLevel = this.energy / maxEnergy * 100; // checks if co-ordinates are viable for the
                                                                     // bacterium
                    if (energyLevel >= 80) {
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
                String type = "tumble";
                this.move(position, environBlocks[x][y], environBlocks, type); // make bacterium go to that block

                break;

            case 1: // die
                if (this.probDie == 100) {
                    probDie = 0;
                    this.die();

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
                break;
            case 2: // eat
                if (this.probEat == 100) {
                    this.probEat = 0;
                    this.eat(null);// needs to do something

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
            case 3: // grow
                if (this.probGrow == 100 && this.energy >= 40) { // adds to probGrow based on liklihood of bacterium
                                                                 // growing
                    this.consume(null); // do something
                } else if (this.energy / this.maxEnergy * 100 <= 20) {
                    probGrow += 2;
                } else if (this.energy / this.maxEnergy * 100 <= 40) {
                    probGrow += 10;
                } else if (this.energy / this.maxEnergy * 100 <= 60) {
                    probGrow += 12;
                } else if (this.energy / this.maxEnergy * 100 <= 80) {
                    probGrow += 15;
                } else {
                    probGrow += 20;
                }
                break;
            case 4:// run
                if (energy == 0 || stuck) {
                    break;
                }
                accepted = false;
                while (!accepted) {
                    x = mt.nextInt(environ.getxBlocks() / 2);
                    y = mt.nextInt(environ.getyBlocks()) / 2;
                    int distance = (int) Math
                            .sqrt(Math.pow(x - position.getXPos(), 2) + Math.pow(y - position.getYPos(), 2));

                    if (distance >= (maxDistance) / 4) {
                        accepted = false;
                    }
                    int energyLevel = this.energy / maxEnergy * 100; // checks if co-ordinates are viable for the
                                                                     // bacterium
                    if (energyLevel >= 80) {
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
                type = "run";
                this.move(position, environBlocks[x][y], environBlocks, type); // make bacterium go to that block
                break;
            case 5:// do nothing
                break;

        }

    }

}
