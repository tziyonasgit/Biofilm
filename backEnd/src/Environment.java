package backEnd;

import java.util.ArrayList;
import java.util.Random;

// class for managing the environment with methods for adding new parts to the environment
public class Environment
{
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

    // paramaterised constructor for environment
    public Environment(int nutrients, int totBMonomers, int FBMonomers, int EPSMonomers, int bacteria, int xBlocks, int yBlocks)
    {
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
    }

    // method for creating individual blocks to create environment
    public void createBlocks(int xBlocks, int yBlocks)
    {
        // block array that is physical environment
        this.environBlocks = new Block[xBlocks][yBlocks];
        // above may not be an array in the final product //

        for (int i = 0; i < xBlocks; i++)
        {
            for (int j = 0; j < yBlocks; j++)
            {
                environBlocks[i][j] = new Block(i, j, 0, false);
                // below will not be in final //
                System.out.print(i);
                System.out.print(j);
                System.out.print(" ");
            }
            // below will not be in final //
            System.out.println("");
        }
    }

    // method for creating initial bacterial and EPS monomers
    public void createMonomers(int monomers, String type, int xBlocks, int yBlocks)
    {
        // random sequences for randomly assigning monomers a block in the environment
        Random rX = new Random();
        Random rY = new Random();

        for (int i = 0; i < monomers; i++)
        {
            // checks what type of monomer are creating
            if (type.equals("bacterial"))
            {
                // creates bacterial monomer
                BacterialMonomer BMonomer = new BacterialMonomer(environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], this.bMonomerID, "bacteria", 'r', "covid");
                // adds monomer to environment's ArrayList of monomers
                this.BMonomers.add(BMonomer);
                // adds monomer to its block's LinkedList of monomers and nutrients
                BMonomer.position.addElement(BMonomer);
                this.bMonomerID++;
            }
            else
            {
                // creates EPS monomer
                EPS eps = new EPS(environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], this.EPSMonomerID, "bacteria", 'r', "covid");
                // adds eps to environment's ArrayList of eps
                this.EPSMonomers.add(eps);
                this.EPSMonomerID++;
            }
            
            // below will not be in final //
            System.out.print(i);
            System.out.print(" ");
        }
        // below will not be in final //
        System.out.println("");        
    }

    // method for creating initial nutrients
    public void createNutrients(int nutrients, int xBlocks, int yBlocks)
    {
        Random rX = new Random();
        Random rY = new Random();

        for (int i = 0; i < nutrients; i++)
        {
            // creates nutrient and adds to linked list of EPS monomers
            // hardcoded type for now //
            Nutrient nutrient = new Nutrient(environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], nutrientID, "food", 'b' );
            this.nutrients.add(nutrient);
            nutrient.position.addElement(nutrient);
           

            // below will not be in final //
            System.out.print(i);
            System.out.print(" ");
        }
        // below will not be in final //
        System.out.println("");       
    }

    // method for creating initial bacteria
    public void createBacteria(int bacteria, int xBlocks, int yBlocks)
    {
        BacterialMonomer[] monomers = new BacterialMonomer[20];
        BacterialMonomer bMonomer;
        Random rX = new Random();
        Random rY = new Random();
        
        for (int i = 0; i < bacteria; i++)
        {
            // will be random bacteria type and colour will be based on type //
            // hardcoded for demo //
            Bacterium bac = new Bacterium(environBlocks[rX.nextInt(xBlocks)][rY.nextInt(yBlocks)], BacteriumID, 0, -1, 7,
                                          monomers, "covid");

            // creates bacterial monomers making up bacteria
            for (int j=0; j < 7; j++)
            {
                bMonomer = createBMonomer(bac.position, "bacterial", 'g', "covid");
                bac.monomers[j] = bMonomer;
                this.BMonomers.add(bMonomer);
            }

            Thread b = new Thread(bac);
            b.start();

            this.Bacteria.add(bac);

            // below print statements won't be in final, just easier for demo
            System.out.print(i);
            System.out.print(" ");

            this.BacteriumID++;
        }
        // below print statement won't be in final, just easier for demo
        System.out.println("");

        
    }

    // method for creating singular bacterial monomer
    public BacterialMonomer createBMonomer(Block position, String type, char colour, String bType)
    {
        BacterialMonomer bMonomer = new BacterialMonomer(position, this.bMonomerID, type, colour, bType);
        this.BMonomers.add(bMonomer);
        this.bMonomerID++;
        return bMonomer;
    }

    // method for creating singular EPS monomer
    public EPS createEPSMonomer(Block position, String type, char colour, String EPSType)
    {
        EPS EPSMonomer = new EPS(position, this.EPSMonomerID, type, colour, EPSType);
        this.EPSMonomers.add(EPSMonomer);
        this.EPSMonomerID++;
        return EPSMonomer;
    }

    // method for creating singular bacterium
    public Bacterium createBacterium()
    {
        BacterialMonomer[] monomers = new BacterialMonomer[20];
        BacterialMonomer bMonomer;

        // below hardcoded for the demo //
        Bacterium bacterium = new Bacterium(environBlocks[0][0], this.BacteriumID, 0, -1, 7,
                                            monomers, "covid");
        for (int j=0; j < 7; j++)
        {
            // below hardcoded for the demo //
            bMonomer = createBMonomer(environBlocks[0][0], "bacterial", 'g', "covid");
            bacterium.monomers[j] = bMonomer;
            this.BMonomers.add(bMonomer);
        }

        this.Bacteria.add(bacterium);
        this.BacteriumID++;
        return bacterium;
    }
}