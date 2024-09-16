package backEnd.src;
import java.util.ArrayList;

// class for managing biofilms
public class Biofilm {
    int numBacteria;
    int numEPS;
    int age;
    ArrayList<Bacterium> bioBacs; 

    // paramaterised constructor for biofilm
    public Biofilm(int bacteria, int EPS, int age) {
        this.numBacteria = bacteria;
        this.numEPS = EPS;
        this.age = age;
        this.bioBacs = new ArrayList<Bacterium>();
    }

    public void addBac(Bacterium bac)
    {
        this.bioBacs.add(bac);
    }

    // breaks up biofilm, how and when //
    public void Disperse() {

    }
}