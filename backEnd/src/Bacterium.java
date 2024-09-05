package backEnd.src;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import backEnd.src.Environment;

// class for managing bacterium with methods to manipulate them (activities)
public class Bacterium implements Runnable {
    Block position;
    int bacteriumID;
    String strain;
    int age;
    Bacterium father;
    int numMonomers;
    BacterialMonomer[] monomers;
    Environment environ;
    public volatile String waiting = "hi";
    public volatile int number = 0;

    // paramaterised constructor for bacterium
    public Bacterium(Block position, int ID, int age, Bacterium father,
            int numMonomers, BacterialMonomer[] monomers, String strain, Environment environ) {
        this.position = position;
        this.bacteriumID = ID; // count of IDs
        this.age = age;
        this.father = father;
        this.numMonomers = numMonomers;
        this.monomers = new BacterialMonomer[20];
        this.strain = strain;
        this.environ = environ;
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

    public void tumble(Block iBlock, Block fBlock) {
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Tumble:("
        // + iBlock.getXPos() + "," + iBlock.getYPos() + ")"
        // + "(" + fBlock.getXPos() + "," + fBlock.getYPos() + ")");
        move(iBlock, fBlock, environ.environBlocks, "Tumble");
    }

    public void run(Block iBlock, Block fBlock) {
        // Simulation.recActivities("Bacterium:" + this.bacteriumID + ":otherMove:("
        // + iBlock.getXPos() + "," + iBlock.getYPos() + ")"
        // + "(" + fBlock.getXPos() + "," + fBlock.getYPos() + ")");
        move(iBlock, fBlock, environ.environBlocks, "Run");
    }

    public void reproduce(Bacterium child) {
        Bacterium childBac = environ.createBacterium(environ); // creates child bacterium
        childBac.setFather(this); // sets child's father to bacterium that is reproducing
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + childBac.getBID());
    }

    // bacterium dies, do all bacterial monomers die as well //
    public void die() {
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Die");    
    }

    // hits another bacterium but doesn't stick together //
    public void collide(Bacterium bac) {
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Collide:Bacterium:" + bac.getBID());
    }

    // increase EPS count, increase Block EPS //
    public void secrete(EPS eps) {
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
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Eat:Nutrient:" + nutrient.getID());
    }

    public void consume(BacterialMonomer bMonomer) {
        bMonomer.position.removeElement(bMonomer);
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Consume:BacterialMonomer:" + bMonomer.getID());
    }

    public void run() {
        try {
            synchronized (waiting) {
                environ.increase();
                waiting.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // WHERE THREADS NEEDS TO WAIT ON BARRIER //
        System.out.println(Thread.currentThread().getName() + ", executing run() method!");
        this.die();
    }

    // method that moves a bacterium from a start to a goal block
    public void move(Block start, Block end, Block[][] environBlocks, String moveType) {

        Block position = start;

        while (!position.compareTo(end)) {
            start = position;
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

            Simulation.recActivities("Bacterium:" + this.bacteriumID + ":" + moveType + ":("
                    + start.getXPos() + "," + start.getYPos() + ")"
                    + "(" + position.getXPos() + "," + position.getYPos() + ")");
        }

    }

}
