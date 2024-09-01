package backEnd;

// class for managing bacterial monomers, inherits from monomer class
public class BacterialMonomer extends Monomer
{
    // paramaterised constructor for bacterial monomer
    public BacterialMonomer(Block position, int bMID)
    {
        super(position, bMID, "bacterial");
    }

    public void bond(Monomer other){
        // add other to linked list of bonds //
        Simulation.recActivities("BMonomer:" + this.MonomerID + ":Bond:BMonomer:" + other.MonomerID);
    }
}