package backEnd.src;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;
import java.util.Random;

// class for managing the environment with methods for adding new parts to the environment
public class Environment {
    int totNutrients, totBMonomers, FBMonomers, totEPSMonomers, totBacteria, freeBlocks;
    Block environBlocks[][];
    ArrayList<BacterialMonomer> BMonomers;
    ArrayList<Bacterium> Bacteria;
    ArrayList<EPS> EPSMonomers;
    ArrayList<Nutrient> nutrients;
    // initialises IDs for the different parts of the environment
    int BacteriumID;
    int bMonomerID;
    int EPSMonomerID;
    int nutrientID;
    int xBlocks;
    int yBlocks;
    // barrier for bacteria to wait on
    public volatile CountDownLatch initialise;
    // public int number = 0;

    // paramaterised constructor for environment
    public Environment(int nutrients, int totBMonomers, int FBMonomers, int EPSMonomers, int bacteria, int xBlocks,
            int yBlocks) {
        this.totNutrients = nutrients;
        this.totBMonomers = totBMonomers;
        this.totBMonomers = FBMonomers;
        this.totEPSMonomers = EPSMonomers;
        this.totBacteria = bacteria;
        this.freeBlocks = yBlocks * xBlocks;
        this.BMonomers = new ArrayList<BacterialMonomer>();
        this.EPSMonomers = new ArrayList<EPS>();
        this.Bacteria = new ArrayList<Bacterium>();
        this.nutrients = new ArrayList<Nutrient>();
        this.BacteriumID = 0;
        this.bMonomerID = 0;
        this.EPSMonomerID = 0;
        this.nutrientID = 0;
        this.xBlocks = xBlocks;
        this.yBlocks = yBlocks;
        this.initialise = new CountDownLatch(bacteria + 1);
    }

    public int getxBlocks(){
        return this.xBlocks;

    }

    public int getyBlocks(){
        return this.yBlocks;
    }

    // method for creating individual blocks to create environment
    public void createBlocks(int xBlocks, int yBlocks) {
        // block array that is physical environment
        this.environBlocks = new Block[xBlocks][yBlocks];
        // above may not be an array in the final product //

        for (int i = 0; i < xBlocks; i++) {
            for (int j = 0; j < yBlocks; j++) {
                environBlocks[i][j] = new Block(i, j, 0, false);
            }
        }
    }

    // method for creating initial bacterial and EPS monomers
    public void createMonomers(int monomers, String type, int xBlocks, int yBlocks) {
        Random rX = new Random();
        Random rY = new Random();

        for (int i = 0; i < monomers; i++) {
            // checks what type of monomer are creating
            if (type.equals("bacterial")) {
                // creates bacterial monomer and adds to linked list of bacterial monomers
                BacterialMonomer BMonomer = new BacterialMonomer(
                        environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], this.bMonomerID);
                this.BMonomers.add(BMonomer);
                // adds monomer to its block's LinkedList of monomers and nutrients
                BMonomer.position.addBMonomer(BMonomer);
                this.bMonomerID++;
            } else {
                // creates EPS monomer and adds to linked list of EPS monomers
                EPS eps = new EPS(environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], this.EPSMonomerID);
                this.EPSMonomers.add(eps);
                eps.position.addEPSMonomer(eps);
                this.EPSMonomerID++;
            }
        }
    }

    // method for creating initial nutrients
    public void createNutrients(int nutrients, int xBlocks, int yBlocks) {
        Random rX = new Random();
        Random rY = new Random();

        for (int i = 0; i < nutrients; i++) {
            // creates nutrient and adds to linked list of EPS monomers
            // hardcoded type for now //
            Nutrient nutrient = new Nutrient(environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], nutrientID);
            this.nutrients.add(nutrient);
            nutrient.position.addNutrient(nutrient);
        }
    }

    // method for creating initial bacteria
    public void createBacteria(int bacteria, int xBlocks, int yBlocks, Environment environ) {
        BacterialMonomer[] monomers = new BacterialMonomer[20];
        BacterialMonomer bMonomer;
        Random rX = new Random();
        Random rY = new Random();

        for (int i = 0; i < bacteria; i++) {
            int xBlock = rX.nextInt(xBlocks);
            int yBlock = rY.nextInt(yBlocks);
            while (environBlocks[xBlock][yBlock].occupied()) {
                xBlock = rX.nextInt(xBlocks);
                yBlock = rY.nextInt(yBlocks);
            }
            Bacterium bac = new Bacterium(environBlocks[xBlock][yBlock], BacteriumID, 0, null,
                    monomers, "covid", environ, 5);

            // creates bacterial monomers making up bacteria
            for (int j = 0; j < 7; j++) {
                bMonomer = createBMonomer(bac.position);
                bac.monomers[j] = bMonomer;
                this.BMonomers.add(bMonomer);
            }

            Thread b = new Thread(bac);
            b.start();

            this.Bacteria.add(bac);

            this.BacteriumID++;
        }

    }

    // method for creating singular bacterial monomer
    public BacterialMonomer createBMonomer(Block position) {
        BacterialMonomer bMonomer = new BacterialMonomer(position, this.bMonomerID);

        synchronized (this.BMonomers) {
            this.BMonomers.add(bMonomer);
        }

        this.bMonomerID++;
        return bMonomer;
    }

    // method for creating singular EPS monomer
    public EPS createEPSMonomer(Block position) {
        EPS EPSMonomer = new EPS(position, this.EPSMonomerID);

        synchronized (this.EPSMonomers) {
            this.EPSMonomers.add(EPSMonomer);
        }

        this.EPSMonomerID++;
        return EPSMonomer;
    }

    // method for creating singular nutrient
    public Nutrient createNutrient(Block position) {
        Nutrient nut = new Nutrient(position, this.nutrientID);

        // synchronizes on ArrayList of nutrients for the Environment
        synchronized (this.nutrients) {
            this.nutrients.add(nut);
        }

        // synchronizes on LinkedList of nutrients for block
        synchronized (position.nutrients) {
            position.addNutrient(nut);
        }

        this.nutrientID++;
        return nut;
    }

    // method for creating singular bacterium
    public Bacterium createBacterium(Environment environ, Block position) {
        BacterialMonomer[] monomers = new BacterialMonomer[20];
        BacterialMonomer bMonomer;

        // below hardcoded for the demo //
        Bacterium bacterium = new Bacterium(position, this.BacteriumID, 0, null,
                monomers, "covid", environ, 5);

        for (int j = 0; j < 7; j++) {
            // below hardcoded for the demo //
            bMonomer = createBMonomer(bacterium.position);
            bacterium.monomers[j] = bMonomer;

            synchronized (this.BMonomers) {
                this.BMonomers.add(bMonomer);
            }
        }

        synchronized (this.Bacteria) {
            this.Bacteria.add(bacterium);
        }

        this.BacteriumID++;

        Thread b = new Thread(bacterium);
        b.start();

        return bacterium;
    }
}