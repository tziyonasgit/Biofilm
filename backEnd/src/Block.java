package backEnd.src;

import java.util.LinkedList;

// class for managing blocks
public class Block {
    int positionX, positionY;
    int EPSLevel = 0;
    boolean occupied = false;
    LinkedList<Monomer> nutrients;
    LinkedList<Monomer> bacMonomers;
    LinkedList<Monomer> eps;

    // paramaterised constructor for block
    public Block(int positionX, int positionY, int levelEPS, boolean occupied) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.EPSLevel = levelEPS;
        this.occupied = occupied;
        this.nutrients = new LinkedList<Monomer>();
        this.bacMonomers = new LinkedList<Monomer>();
        this.eps = new LinkedList<Monomer>();
    }

    // method for setting EPS level of block
    private void setEPS(int level) {
        this.EPSLevel = level;
    }

    // increase EPS count when secreted by bacterium
    public void incEPS() {
        this.EPSLevel += 1;
    }

    // method for setting whether or a not a block is occupied
    public void setOccupied(boolean value) {
        this.occupied = value;
    }

    // method for returning EPS level of block
    public int getEPSLevel() {
        return EPSLevel;
    }

    // method for returning x coordinate of block
    public int getXPos() {
        return positionX;
    }

    public void setXPos(int x) {
        positionX = x;
    }

    // method for returning y coordinate of block
    public int getYPos() {
        return positionY;
    }

    public void setYPos(int y) {
        positionY = y;
    }

    // method for returning whether or not a block is occupied by a bacteria or not
    public boolean occupied() {
        return occupied;
    }

    // method for retunring LinkedList of nutrients for block
    public LinkedList<Monomer> getNutrients() {
        return this.nutrients;
    }

    // method for retunring LinkedList of bacterial monomers for block
    public LinkedList<Monomer> getBMonomers() {
        return this.bacMonomers;
    }

    // method for retunring LinkedList of EPS for block
    public LinkedList<Monomer> getEPS() {
        return this.eps;
    }

    // method for adding nutrient to LinkedList of nutrients for block
    public void addNutrient(Monomer n) {
        this.nutrients.add(n);
    }

    // method for adding bacterial monomer to LinkedList of nutrients for block
    public void addBMonomer(Monomer m) {
        this.bacMonomers.add(m);
    }

    // method for adding EPS to LinkedList of EPS for block
    public void addEPSMonomer(Monomer e) {
        this.eps.add(e);
    }

    // method for removing nutrient from LinkedList of nutrients for block
    public void removeNutrient(Monomer n) {
        this.nutrients.remove(n);
    }

    // method for removing nutrient from LinkedList of nutrients for block
    public void removeBMonomer(Monomer m) {
        this.bacMonomers.remove(m);
    }

    // method for removing nutrient from LinkedList of nutrients for block
    public void removeEPSMonomer(Monomer e) {
        this.eps.remove(e);
    }

    // compare to method
    public boolean compareTo(Block other) {
        if (this.getXPos() == other.getXPos() & this.getYPos() == other.getYPos()) {
            return true;
        } else {
            return false;
        }
    }

    public String getStringFormat() {
        return "Block(" + this.getXPos() + ", " + this.getYPos() + ")";
    }
}
