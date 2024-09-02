package backEnd.src;

// class for managing EPS monomers, inherits from monomer class
public class EPS extends Monomer
{
    String EPSType;

    // paramaterised constructor for EPS monomer
    public EPS(Block position, int MonomerID, String type, char colour, String EPSType)
    {
        super(position, MonomerID, type, colour);
        this.EPSType = EPSType;
    }

    // method for returning ID of EPS monomer
    public int getEPSID()
    {
        return this.MonomerID;
    }
}