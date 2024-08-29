package backEnd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

// class for taking in conditions and parameters, creating and writing to an activity file 
// and running the simulation
public class Simulation
{
    static File actFile;
    static String fileName;
    // ArrayList for storing simulation activities so can be written to file in one go
    static ArrayList<String> activities = new ArrayList<String>();
    static int totBacterialMonomers;
    // creates Checks object so can access its methods
    static Checks checks = new Checks();

    // method for taking in initial and boundary conditions
    public static int[] setConditions()
    {
        // initial and boundary conditions array for passing to main method
        int[] conds = new int[11]; 
        Scanner input = new Scanner(System.in);

        System.out.println("Enter starting free bacterial monomers:");
        conds[0] = checks.checkInt(input.nextLine());

        System.out.println("Enter starting free EPS monomers:");
        conds[1] = checks.checkInt(input.nextLine());

        System.out.println("Enter starting nutrients:");
        conds[2] = checks.checkInt(input.nextLine());

        System.out.println("Enter starting bacteria:");
        conds[3] = checks.checkInt(input.nextLine());

        System.out.println("Enter simulation width (number of blocks):");
        conds[4] = checks.checkInt(input.nextLine());

        System.out.println("Enter simulation height (number of blocks):");
        conds[5] = checks.checkInt(input.nextLine());

        int[] blocks = checks.checkBlocks(conds[5], conds[4]);
        conds[4] = blocks[0];
        conds[5] = blocks[1];
        conds[3] = checks.checkIBacteria(conds[3], conds[5], conds[4]);
        // calculates total bacterial monomers from free bacterial monomers and bacteria 
        // (7 bacterial monomers = 1 bacterium)
        totBacterialMonomers = conds[0] + conds[3]*7;

        System.out.println("Enter desired simulation duration (in seconds):"); 
        conds[6] = checks.checkInt(input.nextLine());

        System.out.println("Enter maximum bacterial monomers:");
        conds[7] = checks.checkMBMonomers(checks.checkInt(input.nextLine()), totBacterialMonomers);

        System.out.println("Enter maximum EPS monomers:");
        conds[8] = checks.checkMEPSMonomers(checks.checkInt(input.nextLine()), conds[1]);

        System.out.println("Enter maximum nutrients:");
        conds[9] = checks.checkMNutrients(checks.checkInt(input.nextLine()), conds[2]);

        System.out.println("Enter maximum bacteria:");
        conds[10] = checks.checkMBacteria(checks.checkInt(input.nextLine()), conds[3]);

        return conds;
    }

    // method for taking in other parameters
    public static float[] setParams()
    {
        Scanner input = new Scanner(System.in);
        float[] params = new float[10];
        // will display recommended parameters, allow to change if want to //
        // repeat below lines for each parameter (need decide on them still) //
        System.out.println("Parameter0 : Recommended Value : Enter desired value:");
        params[0] = checks.checkFloat(input.nextLine());

        input.close();
        return params;
    }

    // method for creating activity file
    public static void createFile()
    {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter desired name for activity file:");
        fileName = input.nextLine();

        // attempts to create and save file
        try
        {
            actFile = new File(fileName);
            if (actFile.createNewFile())
            {
                System.out.println("Your file has been created.");
            }
            else
            {
                System.out.println("File already exists.");
            }
        }
        catch (IOException e)
        {
            System.out.println("Something went wrong.");
            e.printStackTrace();
        }
    }

    // method for writing activities ArrayList for simulation to the activity file
    public static void writeToFile()
    {
        try 
        {
            FileWriter actWFile = new FileWriter(fileName);
            for (int i=0; i < activities.size(); i++)
            {
                actWFile.write(activities.get(i) + "\n"); 

            }
            actWFile.close();      
        }
        catch (IOException e)
        {
            System.out.println("Something went wrong.");
            e.printStackTrace();
        }
    }

    // method for adding an simulation acitivity to ArrayList of activities
    public static void recActivities(String activity)
    {
        activities.add(activity);
    }

    // method for running simulation
    public static void main(String[] args)
    {      
        createFile();

        int[] conds = setConditions();
        // below doesn't do anything yet //
        float[] params = setParams();

        System.out.println();
        // creates simulation model
        SimulationModel sim = new SimulationModel(conds[0], conds[1], conds[2], conds[3], totBacterialMonomers,
                                  conds[4], conds[5], conds[6], conds[7],
                                  conds[8], conds[9], conds[10]);  
                                  
        // simulation runs to completion //

        writeToFile();   
    }
}