package backEnd.src;

// class for managing bacterial monomers, inherits from monomer class
public class BacterialMonomer extends Monomer {
    // paramaterised constructor for bacterial monomer
    public BacterialMonomer(Block position, int bMID) {
        super(position, bMID, "bacterial");
    }
}