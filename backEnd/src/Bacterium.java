package backEnd.src;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;

import backEnd.src.Environment;

import java.time.Duration;
import java.time.LocalDateTime;

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
    //public volatile int number = 0;
    int energy;
    boolean killThread;
    LocalDateTime birthTime;
    static final double doublingTime = 1.2; // Time in hours for bacterium to double in size

    // paramaterised constructor for bacterium
    public Bacterium(Block position, int ID, int age, Bacterium father,
            BacterialMonomer[] monomers, String strain, Environment environ) {
        this.position = position;
        this.bacteriumID = ID; // count of IDs
        this.age = age;
        this.father = father;
        this.monomers = new BacterialMonomer[20];
        this.strain = strain;
        this.environ = environ;
        this.energy = 0;
        this.killThread = false;
        this.birthTime = LocalDateTime.now();
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
        // below will come out later (including comment), just easier testing synchronization here //
        // synchronizes on waiting to ensure that only one bacterium calls recActivities in Simulation.java
        synchronized (waiting)
        {
            Simulation.recActivities("Synchronization test by " + this.getBID());
        }
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

    public void reproduce(Bacterium child) {
        Bacterium childBac = environ.createBacterium(environ); // creates child bacterium
        childBac.setFather(this); // sets child's father to bacterium that is reproducing
        this.resetMonomers(); // resets father bacterium to 7 monomers
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + childBac.getBID());
    }

    // bacterium dies, do all bacterial monomers die as well //
    public void die() {
        environ.Bacteria.remove(this);
        position.setOccupied(false); // makes block free
        this.killThread = true;
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Die");
    }

    // hits another bacterium but doesn't stick together //
    public void collide(Bacterium bac) {
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

        move(this.position, destinationPosition, environ.environBlocks, "Run");
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Collide:Bacterium:" + bac.getBID());
    }

    // increase EPS count, increase Block EPS //
    public void secrete(EPS eps) {
        position.incEPS();
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Secrete:EPS:" + eps.getID());
    }

    // fixed onto block by EPS //
    public void attach(Block block) {
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Attach:("
                + block.getXPos() + "," + block.getYPos() + ")");
    }

    // decrease nutrient count, what does it do to bacterium //
    public void eat(Nutrient nutrient) {
        nutrient.position.removeElement(nutrient);
        this.energy += 1; // increases energy level
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Eat:Nutrient:" + nutrient.getID());
    }

    public void consume(BacterialMonomer bMonomer) {
        bMonomer.position.removeElement(bMonomer);
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Consume:BacterialMonomer:" + bMonomer.getID());
    }

    public void run() {
        try {

            // waits on CyclicBarrier initialise to ensure all bacteria start their main functioning
            // simultaneously
            environ.initialise.await();
            
            System.out.println(Thread.currentThread().getName() + ", executing run() method!");

            // while (!killThread) {
            //     grow();
            // }

            // will not be in final, just for testing synchronization //
            this.setFather(this);

        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    public void grow() { // NEED TO WORK ON
        Duration timeElapsed = Duration.between(birthTime, LocalDateTime.now());
        double hoursSinceBirth = timeElapsed.toHours() + timeElapsed.toMinutes() / 60.0;
        double growthTime = Math.min(hoursSinceBirth, 1.2 * doublingTime);
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
                    + start.getXPos() + "," + start.getYPos() + ")"
                    + "(" + position.getXPos() + "," + position.getYPos() + ")");
        }

    }

}
