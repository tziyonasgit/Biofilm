package backEnd.src;
import java.util.ArrayList;

// class for managing biofilms
public class Biofilm {
    private int numBacteria;
    private int numEPS;
    private ArrayList<Bacterium> bioBacs; 

    // paramaterised constructor for biofilm
    public Biofilm(int bacteria, int EPS) {
        this.numBacteria = bacteria;
        this.numEPS = EPS;
        this.bioBacs = new ArrayList<Bacterium>();
    }

    // method for increasing bacteria in biofilm when new bacteria is added
    public void incBacs()
    {
        this.numBacteria ++;
    }

    // method for increasing EPS in biofilm by given amount
    public void incEPSAmount(int level)
    {
        this.numEPS = this.numEPS + level;
    }

    // method for returning number of bacteria in biofilm
    public int getBacs()
    {
        return this.numBacteria;
    }

    // method for returning number of EPS monomers in biofilm
    public int getEPSAmount()
    {
        return this.numEPS;
    }

    // method for adding bacterium to biofilm
    public void addBac(Bacterium bac)
    {
        this.bioBacs.add(bac);
    }
}