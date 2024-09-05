package backEnd.src;

// class for managing biofilms
public class Biofilm {
    int numBacteria;
    int numEPS;
    int age;
    String type;

    // paramaterised constructor for biofilm
    public Biofilm(int bacteria, int EPS, int age, String type) {
        this.numBacteria = bacteria;
        this.numEPS = EPS;
        this.age = age;
        this.type = type;
    }

    // breaks up biofilm, how and when //
    public void Disperse() {

    }
}