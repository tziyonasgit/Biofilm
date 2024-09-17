package backEnd.src;

// class for managing EPS monomers, inherits from monomer class
public class EPS extends Monomer {
    // paramaterised constructor for EPS monomer
    public EPS(Block position, int epsID) {
        super(position, epsID, "eps");
    }
}