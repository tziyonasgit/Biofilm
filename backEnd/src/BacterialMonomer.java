package backEnd.src;

// class for managing bacterial monomers, inherits from monomer class
public class BacterialMonomer extends Monomer
{
    public String bacteriaType;

    // paramaterised constructor for bacterial monomer
    public BacterialMonomer(Block position, int MonomerID, String type, char colour, String bacteriaType)
    {
        super(position, MonomerID, type, colour);
        this.bacteriaType = bacteriaType;
    }

    // method for returning ID of bacterial monomer
    public int getBMID()
    {
        return this.MonomerID;
    }
}