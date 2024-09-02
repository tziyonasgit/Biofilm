package backEnd.src;

// class for managing bacterium with methods to manipulate them (activities)
public class Bacterium implements Runnable {
    Block position;
    int bacteriumID;
    String strain;
    int age;
    int father;
    int numMonomers;
    BacterialMonomer[] monomers;

    // paramaterised constructor for bacterium
    public Bacterium(Block position, int ID, int age, int father, 
                     int numMonomers, BacterialMonomer[] monomers, String strain)
    {
        this.position = position;
        this.bacteriumID = ID; // count of IDs
        this.age = age;
        this.father = father;
        this.numMonomers = numMonomers;
        this.monomers = new BacterialMonomer[20];
        this.strain = strain;
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

    // need to figure out what the below methods do exactly //
    // for demo just adds activity to ArrayList of activities so can be written to
    // activity file //
    public void tumble(Block iBlock, Block fBlock) {
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Tumble:("
                + iBlock.getXPos() + "," + iBlock.getYPos() + ")"
                + "(" + fBlock.getXPos() + "," + fBlock.getYPos() + ")");
    }

    public void otherMove(Block iBlock, Block fBlock) {
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":otherMove:("
                + iBlock.getXPos() + "," + iBlock.getYPos() + ")"
                + "(" + fBlock.getXPos() + "," + fBlock.getYPos() + ")");
    }

    public void reproduce(Bacterium child) {
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Reproduce:Bacterium:" + child.getBID());
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
    public void secrete(EPS eps)
    {
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

    public void consume(BacterialMonomer bMonomer)
    {
        bMonomer.position.removeElement(bMonomer);
        Simulation.recActivities("Bacterium:" + this.bacteriumID + ":Consume:BacterialMonomer:" + bMonomer.getID());
    }

    // method for running bacterium thread, doesn't do anything but print message showing running
    // will change still
    public void run()
    {
        System.out.println(Thread.currentThread().getName() + ", executing run() method!");
    }

}