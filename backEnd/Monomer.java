package backEnd;

// class for managing monomers
public class Monomer {
    Block position;
    int MonomerID;
    String type;
    char colour;
    // linked list of monomers it is bonded to //

    // paramaterised constructor for nutrient
    public Monomer(Block position, int MonomerID, String type, char colour) {
        this.position = position;
        this.MonomerID = MonomerID;
        this.type = type;
        this.colour = colour;
    }

    // method for bonding 2 monomers together
    public void bond(Monomer other) {
        // add other to linked list of bonds //
        // below will change later, just testing //
        Simulation.recActivities("BMonomer:" + this.MonomerID + ":Bond:BMonomer:" + other.MonomerID);
    }

    // method for setting position in the environment of the monomer
    private void setPositon(Block pos) {
        this.position = pos;
    }

    // method for setting the type (bacterial, EPS) of monomer
    private void setType(String type) {
        this.type = type;
    }

    // method for setting colour of the monomer
    private void setColour(char colour) {
        this.colour = colour;
    }

    // method for returning the position of the monomer in the environment
    private Block getPosition() {
        return this.position;
    }

    // method for returning the type of monomer
    private String getType() {
        return this.type;
    }

    // method for returning the colour of the monomer
    private char getColour() {
        return this.colour;
    }
}