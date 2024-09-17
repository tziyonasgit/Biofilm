package backEnd.src;

import java.util.LinkedList;

// class for managing blocks
public class Block {
    private int positionX, positionY;
    private int EPSLevel = 0;
    private boolean occupied = false;
    private LinkedList<Nutrient> nutrients;
    private  LinkedList<BacterialMonomer> bacMonomers;
    private LinkedList<EPS> eps;
    private Bacterium occupier;

    // paramaterised constructor for block
    public Block(int positionX, int positionY, int levelEPS, boolean occupied) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.EPSLevel = levelEPS;
        this.occupied = occupied;
        this.nutrients = new LinkedList<Nutrient>();
        this.bacMonomers = new LinkedList<BacterialMonomer>();
        this.eps = new LinkedList<EPS>();
    }

    // increase EPS count when secreted by bacterium
    public void incEPS() {
        this.EPSLevel += 50;
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

    // method for returning bacterium occupying block
    public Bacterium getOccupier() {
        return occupier;
    }

    // method for setting bacterium occupying block
    public void setOccupier(Bacterium bac) {
        this.occupier = bac;
    }

    // method for returning y coordinate of block
    public int getYPos() {
        return positionY;
    }

    // method for returning whether or not a block is occupied by a bacteria or not
    public boolean occupied() {
        return occupied;
    }

    // method for retunring LinkedList of nutrients for block
    public LinkedList<Nutrient> getNutrients() {
        return this.nutrients;
    }

    // method for retunring LinkedList of bacterial monomers for block
    public LinkedList<BacterialMonomer> getBMonomers() {
        return this.bacMonomers;
    }

    // method for retunring LinkedList of EPS for block
    public LinkedList<EPS> getEPS() {
        return this.eps;
    }

    // method for adding nutrient to LinkedList of nutrients for block
    public void addNutrient(Nutrient n) {
        this.nutrients.add(n);
    }

    // method for adding bacterial monomer to LinkedList of nutrients for block
    public void addBMonomer(BacterialMonomer m) {
        this.bacMonomers.add(m);
    }

    // method for adding EPS to LinkedList of EPS for block
    public void addEPSMonomer(EPS e) {
        this.eps.add(e);
    }

    // method for removing nutrient from LinkedList of nutrients for block
    public void removeNutrient(Nutrient n) {
        this.nutrients.remove(n);
    }

    // method for removing nutrient from LinkedList of nutrients for block
    public void removeBMonomer(BacterialMonomer m) {
        this.bacMonomers.remove(m);
    }

    // method for removing nutrient from LinkedList of nutrients for block
    public void removeEPSMonomer(EPS e) {
        this.eps.remove(e);
    }

    // method for comparing two blocks
    public boolean compareTo(Block other) {
        if (this.getXPos() == other.getXPos() & this.getYPos() == other.getYPos()) {
            return true;
        } else {
            return false;
        }
    }

    // method for printing out a block in the form (x,y)
    public String getStringFormat() {
        return "Block(" + this.getXPos() + ", " + this.getYPos() + ")";
    }
}
