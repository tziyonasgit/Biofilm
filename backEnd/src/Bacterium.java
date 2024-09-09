package backEnd.src;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

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
    // public volatile int number = 0;
    int energy;
    boolean killThread;
    LocalDateTime birthTime;
    double doublingTime = 0; // Time in hours for bacterium to double in size
    MersenneTwister mt;
    int maxAge = 100;
    int maxEnergy = 100;
    int probDie = 0;
    int probEat = 0;
    int probGrow = 0;

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
        this.birthTime = LocalDateTime.now();
        this.doublingTime = generateDoublingTime(1.0);
        this.mt = new MersenneTwister(seed);
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Spawn:" + "(" + position.getXPos() + ","
                + position.getYPos() + ")");
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
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":otherMove:("
        // + iBlock.getXPos() + "," + iBlock.getYPos() + ")"
        // + "(" + fBlock.getXPos() + "," + fBlock.getYPos() + ")");
        move(iBlock, fBlock, environ.environBlocks, "Run");
    }

    public void reproduce(Bacterium child, Block position) {
        Bacterium childBac = environ.createBacterium(environ, position); // creates child bacterium
        childBac.setFather(this); // sets child's father to bacterium that is reproducing
        this.resetMonomers(); // resets father bacterium to 7 monomers

        // synchronizes on waiting to ensure that only one bacterium calls recActivities
        // in Simulation.java
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + childBac.getBID());
        }
    }

    // bacterium dies, do all bacterial monomers die as well //
    public void die() {

        synchronized (environ.Bacteria) {
            environ.Bacteria.remove(this);
        }

        position.setOccupied(false); // makes block free
        this.killThread = true;

        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Die");
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
        }
    }

    // increase EPS count, increase Block EPS //
    public void secrete() {
        position.incEPS();
        // not sure if we need the below
        environ.EPSMonomers.add(environ.createEPSMonomer(position));

        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Secrete:EPS");
        }
    }

    // fixed onto block by EPS //
    public void attach(Block block) {
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Attach:("
                    + block.getXPos() + "," + block.getYPos() + ")");
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
        }
    }

    public void consume(BacterialMonomer bMonomer) {
        bMonomer.position.removeBMonomer(bMonomer);
        synchronized (waiting) {
            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Consume:BacterialMonomer:" + bMonomer.getID());
        }
    }

    public void run() {
        // waits on countdownlatch initialise to ensure all bacteria start their main
        // functioning
        // simultaneously
        try {
            environ.initialise.countDown();
            environ.initialise.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + ", executing run() method!");

        this.runMove(this.getBlock(), environ.environBlocks[0][0]); // calls

        // while (!killThread) {
        // grow(SimulationModel.duration);
        // }

        // // will not be in final, just for testing synchronization //
        this.setFather(this);

    }

    public void grow(double totalDuration) {
        Duration timeElapsed = Duration.between(birthTime, LocalDateTime.now());
        double hoursSinceBirth = timeElapsed.toHours() + timeElapsed.toMinutes() / 60.0; // converts the elapsed time
                                                                                         // into hours.

        // Calculate the proportion of simulation duration that has passed.
        double simulationProportion = hoursSinceBirth / totalDuration;

        double growthTime = Math.min(simulationProportion * hoursSinceBirth, 1.2 * doublingTime);

        // Check if the bacterium has reached its doubling time
        if (growthTime >= doublingTime) {
            // Trigger reproduction when doubling time is met or exceeded
            Block newPosition = getRandomAdjacentFreeBlock(this.position); // Get a free block for the new bacterium
            Bacterium child = environ.createBacterium(environ, newPosition); // Create the child bacterium

            // Call the reproduce method to handle child creation and other tasks
            reproduce(child, newPosition);

            // Reset growth
            this.birthTime = LocalDateTime.now(); // Reset the birth time after reproduction

        }

    }

    public void moveLeft(Block[][] environBlocks, String moveType, Block start) {
        position = environBlocks[start.getXPos() - 1][start.getYPos()];
        this.energy -= 1; // decreases energy each movement
        position.setOccupied(true);
        // synchronized (waiting) {
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType +
        // ":("
        // + start.getXPos() + "," + start.getYPos() + "):"
        // + "(" + position.getXPos() + "," + position.getYPos() + ")");
        // }

    }

    public void moveRight(Block[][] environBlocks, String moveType, Block start) {
        position = environBlocks[start.getXPos() + 1][start.getYPos()];
        this.energy -= 1; // decreases energy each movement
        position.setOccupied(true);
        // synchronized (waiting) {
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType +
        // ":("
        // + start.getXPos() + "," + start.getYPos() + "):"
        // + "(" + position.getXPos() + "," + position.getYPos() + ")");
        // }

    }

    public void moveUp(Block[][] environBlocks, String moveType, Block start) {
        position = environBlocks[start.getXPos()][start.getYPos() + 1];
        this.energy -= 1; // decreases energy each movement
        position.setOccupied(true);
        // synchronized (waiting) {
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType +
        // ":("
        // + start.getXPos() + "," + start.getYPos() + "):"
        // + "(" + position.getXPos() + "," + position.getYPos() + ")");
        // }

    }

    public void moveDown(Block[][] environBlocks, String moveType, Block start) {
        position = environBlocks[start.getXPos()][start.getYPos() - 1];
        this.energy -= 1; // decreases energy each movement
        position.setOccupied(true);
        // synchronized (waiting) {
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType +
        // ":("
        // + start.getXPos() + "," + start.getYPos() + "):"
        // + "(" + position.getXPos() + "," + position.getYPos() + ")");
        // }

    }

    // method that moves a bacterium from a start to a goal block
    public void move(Block start, Block end, Block[][] environBlocks, String moveType) {

        Block position = start;

        while (!position.compareTo(end)) {
            start = position;
            start.setOccupied(false);
            if (start.getXPos() > end.getXPos()) {
                this.moveLeft(environBlocks, moveType, start);// move one left
            } else if (start.getXPos() < end.getXPos()) {
                this.moveRight(environBlocks, moveType, start);// move one right
            }

            else if (start.getYPos() < end.getYPos()) {
                this.moveUp(environBlocks, moveType, start);// move one up
            }

            else if (start.getYPos() > end.getYPos()) {
                this.moveDown(environBlocks, moveType, start); // move one down
            }

            // System.out.println("hi");
            synchronized (waiting) { // not sure what this does...put it in comments in the move up, down, left,
                                     // right methods, if it is used to record when a movement happens then uncomment
                                     // it....Thalia
                Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType + ":("
                        + start.getXPos() + "," + start.getYPos() + "):"
                        + "(" + position.getXPos() + "," + position.getYPos() + ")");
            }
        }

    }

    public void doSomething() {
        int event = mt.nextInt(6);
        switch (event) {
            case 0:
                // tumble
                break;

            case 1:
                if (this.probDie == 100) {
                    probDie = 0;
                    this.die();

                } else if (((this.age) / maxAge) * 100 <= 20) {
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
            case 2:
                if (this.probEat == 100) {
                    this.probEat = 0;
                    this.eat(null);// needs to do something

                } else if ((this.energy / this.maxEnergy) * 100 <= 40) {
                    probEat += 20;

                } else if ((this.energy / this.maxEnergy) * 100 <= 60) {
                    probEat += 15;

                } else if ((this.energy / this.maxEnergy) * 100 <= 80) {
                    probEat += 10;

                } else {
                    probEat += 5;
                }
                break;
            case 3:
                if (this.probGrow == 100 && this.energy >= 40) {
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
            case 4:
                //run
                break;
            case 5:
                break; // do nothing

        }

    }

}
