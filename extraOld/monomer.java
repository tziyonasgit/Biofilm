package extraOld;

public class monomer {

    block position;
    String type;
    char colour;
    // linked list of monomers it is bonded to?

    public monomer(block position, String type, char colour) {
        this.position = position;
        this.type = type;
        this.colour = colour;

    }

    private void bond(monomer other) {
        // add other to linked list of bonds

    }

    private void setPositon(block pos) {
        this.position = pos;
    }

    private void setType(String type) {
        this.type = type;
    }

    private void setColour(char colour) {
        this.colour = colour;
    }

    private block getPosition() {
        return this.position;
    }

    private String getType() {
        return this.type;
    }

    private char getColour() {
        return this.colour;
    }

}