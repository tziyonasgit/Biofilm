package backEnd.src;

import java.util.Scanner;

// class for seperating out checks for simulation parameters and conditions
public class Checks {
    public Checks() {

    }

    // do we want to give options for the other checks as well (to change max or
    // initial) //

    // method checks that the number of blocks in the environment is between its
    // bounds
    public int[] checkBlocks(int xBlocks, int yBlocks) {
        int blocks[] = new int[2];
        Scanner input = new Scanner(System.in);
        // Validate xBlocks
        while (xBlocks < 100 || xBlocks > 2500) {
            System.out.println("Width must be between 100 and 2500 blocks.");
            System.out.println("Please reenter simulation width:");
            xBlocks = this.checkInt(input.nextLine());
        }

        // Validate yBlocks
        while (yBlocks < 100 || yBlocks > 2500) {
            System.out.println("Height must be between 100 and 2500 blocks.");
            System.out.println("Please reenter simulation height:");
            yBlocks = this.checkInt(input.nextLine());
        }
        blocks[0] = xBlocks;
        blocks[1] = yBlocks;
        return blocks;
    }

    // method checks that initial bacteria is not greater than the number of blocks
    // in the environment
    public int checkIBacteria(int iBacteria, int xBlocks, int yBlocks) {
        Scanner input = new Scanner(System.in);
        while (iBacteria >= yBlocks * xBlocks) {
            System.out
                    .println("Your environment (in number of blocks) is not large enough for your amount of bacteria.");
            System.out.println("Please reenter starting bacteria:");
            iBacteria = this.checkInt(input.nextLine());
        }
        return iBacteria;
    }

    public int checkInt(String value) {
        Scanner input = new Scanner(System.in);
        boolean checking = true;
        while (checking) {
            try {
                Integer.parseInt(value);
                checking = false;
            } catch (Exception e) {
                System.out.println("Value must be an integer.");
                System.out.println("Please enter your new value:");
                value = input.nextLine();

            }
        }
        return Integer.parseInt(value);
    }

    public float checkFloat(String value) {
        Scanner input = new Scanner(System.in);
        boolean checking = true;
        while (checking) {
            try {
                Float.parseFloat(value);
                checking = false;
            } catch (Exception e) {
                System.out.println("Value must be a float.");
                System.out.println("Please enter your new value:");
                value = input.nextLine();
            }
        }
        return Float.parseFloat(value);
    }
}