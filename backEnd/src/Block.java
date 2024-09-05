package backEnd.src;

import java.util.LinkedList;

// class for managing blocks
public class Block {
    int positionX, positionY;
    int EPSLevel = 0;
    boolean occupied = false;
    LinkedList<Monomer> elements;

    // paramaterised constructor for block
    public Block(int positionX, int positionY, int levelEPS, boolean occupied) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.EPSLevel = levelEPS;
        this.occupied = occupied;
        this.elements = new LinkedList<Monomer>();

    }

    // method for setting EPS level of block
    private void setEPS(int level) {
        this.EPSLevel = level;
    }

    // method for setting whether or a not a block is occupied
    private void setOccupied(boolean value) {
        this.occupied = value;
    }

    // method for returning EPS level of block
    private int getEPSLevel() {
        return EPSLevel;
    }

    // method for returning x coordinate of block
    public int getXPos() {
        return positionX;
    }

    // method for returning y coordinate of block
    public int getYPos() {
        return positionY;
    }

    // method for returning whether or not a block is occupied
    private boolean occupied() {
        return occupied;
    }

    // method for retunring LinkedList of bacterial monomers and nutrients for blocl
    public LinkedList<Monomer> getElements() {
        return this.elements;
    }

    // method for adding monomer to LinkedList of bacterial monomers and nutrients
    // for block
    public void addElement(Monomer m) {
        this.elements.add(m);
    }

    // method for removing monomer from LinkedList of bacterial monomers and
    // nutrients for block
    public void removeElement(Monomer m) {
        this.elements.remove(m);
    }

    // compare to method
    public boolean compareTo(Block other) {
        if (this.getXPos() == other.getXPos() & this.getYPos() == other.getYPos()) {
            return true;
        } else {
            return false;
        }
    }
}
