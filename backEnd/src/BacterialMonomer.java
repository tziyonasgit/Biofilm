package backEnd;

// class for managing bacterial monomers, inherits from monomer class
public class BacterialMonomer extends Monomer
{
    public String bacteriaType;

    // paramaterised constructor for bacterial monomer
    public BacterialMonomer(Block position, int bMID, String type, char colour, String bacteriaType)
    {
        super(position, bMID, type, colour);
        this.bacteriaType = bacteriaType;
    }

    public void bond(Monomer other){
        // add other to linked list of bonds //
        Simulation.recActivities("BMonomer:" + this.MonomerID + ":Bond:BMonomer:" + other.MonomerID);
    }
}