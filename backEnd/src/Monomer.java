package backEnd.src;

// class for managing monomers
public class Monomer {
    private Block position;
    private int monomerID;
    private String type;

    // paramaterised constructor for monomer
    public Monomer(Block position, int monomerID, String type) {
        this.position = position;
        this.monomerID = monomerID;
        this.type = type;
    }

    // method for returning position of monomer
    public Block getPos() {
        return this.position;
    }
    
    // method for returning ID of monomer
    public int getID() {
        return this.monomerID;
    }
}