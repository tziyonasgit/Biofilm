package backEnd.src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

// class for taking in conditions and parameters, creating and writing to an activity file 
// and running the simulation
public class Simulation {
    private static File actFile;
    private static String fileName; 
    private static ArrayList<String> activities = new ArrayList<String>();
    private static int totBacterialMonomers;
    private static Checks checks = new Checks();

    // method for taking in initial and boundary conditions
    private static int[] setConditions(String[] args) {
        int[] conds = new int[6];

        // Use command-line arguments instead of Scanner input
        conds[0] = checks.checkInt(args[0]); // Free EPS monomers
        conds[1] = checks.checkInt(args[1]); // Nutrients
        conds[2] = checks.checkInt(args[2]); // Starting bacteria
        conds[3] = checks.checkInt(args[3]); // Simulation width
        conds[4] = checks.checkInt(args[4]); // Simulation height

        // Check block size and adjust
        int[] blocks = checks.checkBlocks(conds[3], conds[4]);
        conds[3] = blocks[0];
        conds[4] = blocks[1];
        // Checks that the number of initial bacteria is valid
        conds[2] = checks.checkIBacteria(conds[2], conds[3], conds[4]);

        // Calculate total bacterial monomers from bacteria
        totBacterialMonomers = conds[2] * 7;

        // Simulation duration
        conds[5] = checks.checkInt(args[5]); // Simulation duration

        return conds;
    }

    // method for creating activity file
    private static void createFile(int[] conds, String[] args) {
        Scanner input = new Scanner(System.in);
        // System.out.println("Enter desired name for activity file:");
        // fileName = input.nextLine() + ".txt";
        fileName = args[6] + ".txt";

        // attempts to create and save file
        try {
            actFile = new File(fileName);
            if (actFile.createNewFile()) {
                System.out.println("Your file has been created.");
            } else {
                System.out.println("File already exists.");
                try {
                    FileWriter actWFile = new FileWriter(fileName);
                    actWFile.write("");
                    actWFile.close();
                } catch (IOException e) {
                    System.out.println("Something went wrong.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong.");
            e.printStackTrace();
        }

        try {
            FileWriter actWFile = new FileWriter(fileName);
            actWFile.write("Starting EPS monomers: " + conds[0] + "\n");
            actWFile.write("Starting nutrients: " + conds[1] + "\n");
            actWFile.write("Starting bacteria: " + conds[2] + "\n");
            actWFile.write("Simulation width: " + conds[3] + "\n");
            actWFile.write("Simulation height: " + conds[4] + "\n");
            actWFile.write("Simulation duration: " + conds[5] + "\n\n");
            actWFile.close();
        } catch (IOException e) {
            System.out.println("Something went wrong.");
            e.printStackTrace();
        }

        input.close();
    }

    // method for writing activities ArrayList for simulation to the activity file
    public static void writeToFile() {
        try {
            FileWriter actWFile = new FileWriter(fileName, true);
            for (int i = 0; i < activities.size(); i++) {
                actWFile.write(activities.get(i) + "\n");
            }
            actWFile.write("*\n");
            actWFile.close();
        } catch (IOException e) {
            System.out.println("Something went wrong.");
            e.printStackTrace();
        }
    }

    // Method for returning the arraylist of activities
    public static ArrayList<String> getActivities()
    {
        return activities;
    }

    // method for adding an simulation acitivity to ArrayList of activities
    public static void recActivities(String activity) {
        // synchronizes on ArrayList of activities so that no action is lost if the
        // contents of the ArrayList is busy being written to the activity file and reset in each
        // timestep in SimulationModel.java
        synchronized (activities) {
            activities.add(activity);
        }
    }

    // method for running simulation
    public static void main(String[] args) {
        int[] conds = setConditions(args);
        // below doesn't do anything yet //
        // float[] params = setParams();

        // creates file if new and resets if already exists and writes simulation
        // conditions to the file
        createFile(conds, args);

        System.out.println();
        // creates simulation model
        SimulationModel sim = new SimulationModel(conds[0], conds[1], conds[2], totBacterialMonomers,
                conds[3], conds[4], conds[5]);
        // simulation runs to completion //

    }
}