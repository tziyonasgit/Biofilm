package backEnd;

import java.util.Scanner;

// class for seperating out checks for simulation parameters and conditions
public class Checks {
    public Checks() {

    }

    // need to write a check that is an integer for initial conditions and boundary
    // conditions //

    // do we want below to give an option of changing grid or changing bacteria //
    // do we want to give options for the other checks as well //

    // method checks that initial bacteria is not greater than the number of blocks
    // in the environment
    public int checkIBacteria(int iBacteria, int yBlocks, int xBlocks) {
        Scanner input = new Scanner(System.in);
        while (iBacteria > yBlocks * xBlocks) {
            System.out.println("Your environment (in number of blocks) is not large enough for that many bacteria.");
            System.out.println("Please reenter starting bacteria:");
            iBacteria = input.nextInt();
        }
        return iBacteria;
    }

    // method checks that maximum bacteria is greater than the initial bacteria
    public int checkMBacteria(int mBacteria, int iBacteria) {
        Scanner input = new Scanner(System.in);
        while (iBacteria > mBacteria) {
            System.out.println("You cannot start with more bacteria that the maximum.");
            System.out.println("Please reenter maximum bacteria:");
            mBacteria = input.nextInt();
        }
        return mBacteria;
    }

    // method checks that maximum bacterial monomers is greater than the initial
    // bacterial monomers
    public int checkMBMonomers(int mBMonomers, int iBMonomers) {
        Scanner input = new Scanner(System.in);
        while (iBMonomers > mBMonomers) {
            System.out.println("You cannot start with more bacterial monomers than the maximum.");
            System.out.println("Please reenter maximum bacterial monomers:");
            mBMonomers = input.nextInt();
        }
        return mBMonomers;
    }

    // method checks that maximum EPS monomers is greater than the initial EPS
    // monomers
    public int checkMEPSMonomers(int mEPSMonomers, int iEPSMonomers) {
        Scanner input = new Scanner(System.in);
        while (iEPSMonomers > mEPSMonomers) {
            System.out.println("You cannot start with more EPS monomers than the maximum.");
            System.out.println("Please reenter maximum EPS monomers:");
            mEPSMonomers = input.nextInt();
        }
        return mEPSMonomers;
    }

    // method checks that maximum nutrients is greater than the initial nutrients
    public int checkMNutrients(int mNutrients, int iNutrients) {
        Scanner input = new Scanner(System.in);
        while (iNutrients > mNutrients) {
            System.out.println("You cannot start with more nutrients than the maximum.");
            System.out.println("Please reenter maximum nutrients:");
            mNutrients = input.nextInt();
        }
        return mNutrients;
    }

}