package backEnd.src;

import java.util.Timer;
import java.util.TimerTask;

import backEnd.src.Environment;

// class for managing simulation and creating the simulation environment and setting up its parts
public class SimulationModel {
        int iNutrients, mNutrients, iFBMonomers, totBMonomers, mBMonomers, iEPSMonomers, mEPSMonomers,
                        iBacteria, mBacteria, duration, yBlocks, xBlocks;
        Environment simEnviron;
        public volatile String run = "sync";
        // Simulation paramaters still to be added here //

        // paramaterised constructor for simulation model
        public SimulationModel(int iFBMonomers, int iEPSMonomers, int iNutrients, int iBacteria, int totBMonomers,
                        int xBlocks, int yBlocks, int duration, int mBMonomers,
                        int mEPSMonomers, int mNutrients, int mBacteria)
        // Simulation paramaters still to be added here //
        {
                this.iNutrients = iNutrients;
                this.mNutrients = mNutrients;
                this.iFBMonomers = iFBMonomers;
                this.totBMonomers = totBMonomers;
                this.mBMonomers = mBMonomers;
                this.iEPSMonomers = iEPSMonomers;
                this.mEPSMonomers = mEPSMonomers;
                this.iBacteria = iBacteria;
                this.mBacteria = mBacteria;
                this.duration = duration;
                this.yBlocks = yBlocks;
                this.xBlocks = xBlocks;
                this.simEnviron = createEnvironment(iNutrients, iFBMonomers, totBMonomers, iEPSMonomers, iBacteria,
                                xBlocks, yBlocks);

                biofilmSimulation(simEnviron);
        }
        // Simulation paramaters still to be added here //

        // class for creating and running a task to be completed on each run of a timer
        class Helper extends TimerTask {
                public int i = 0;

                public void run() {
                        i++;
                        // writes activities in ArrayList of activities to file and empties the ArrayList
                        Simulation.writeToFile(i);
                        Simulation.activities.clear();

                        // if task, and hence number of timesteps done, has been completed as many times as 
                        // the given duration, timer is stopped (notified that can cancel)
                        if (i == duration) {
                                synchronized (run) {
                                        run.notifyAll();
                                }
                        }
                }

        }

        // method for creating environment and its parts (blocks, monomers, nutrients,
        // bacteria)
        // for now also used for hardcoded demoing of methods and functionality //
        public Environment createEnvironment(int nutrients, int FBMonomers, int totBMonomers, int EPSMonomers,
                        int bacteria,
                        int xBlocks, int yBlocks) {
                Environment environ = new Environment(nutrients, totBMonomers, FBMonomers, EPSMonomers,
                                bacteria, xBlocks,
                                yBlocks);

                environ.createBlocks(xBlocks, yBlocks);

                System.out.println("");
                System.out.print("Bacteria: ");
                environ.createBacteria(bacteria, xBlocks, yBlocks, environ);

                System.out.print("Free Bacterial Monomers: ");
                environ.createMonomers(FBMonomers, "bacterial", xBlocks, yBlocks);

                System.out.print("EPS Monomers: ");
                environ.createMonomers(EPSMonomers, "EPS", xBlocks, yBlocks);

                System.out.print("Nutrients: ");
                environ.createNutrients(nutrients, xBlocks, yBlocks);

                // creates new timer and a task to give to it
                Timer timer = new Timer();
                TimerTask task = new Helper();

                // schedules timer to run and perform task every time period 
                timer.schedule(task, 0, 5);

                // runs timer over and over until notified to cancel and stop
                try {
                        synchronized (run) {
                                run.wait();
                        }
                        timer.cancel();
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }

                System.out.println("");
                System.out.println("Simulation fully set up. Simulation running...");

                return environ;
        }

        public void biofilmSimulation(Environment environ) {

                Bacterium tester = environ.Bacteria.get(0);
                tester.runMove(tester.getBlock(), environ.environBlocks[0][0]); // calls
                // either
                // run or tumble motion which
                // calls move method

                // testing adding and removing bacterial monomers and nutrients from block's
                // LinkedList of
                // bacterial monomers and nutrients
                System.out.println(environ.nutrients.get(0).position.elements);
                System.out.println(environ.BMonomers.get(environ.BMonomers.size() -
                                1).position.elements);

                environ.Bacteria.get(0).eat(environ.nutrients.get(0));
                environ.Bacteria.get(0).consume(environ.BMonomers.get(environ.BMonomers.size()
                                - 1));
                System.out.println(environ.nutrients.get(0).position.elements);
                System.out.println(environ.BMonomers.get(environ.BMonomers.size() -
                                1).position.elements);

                System.out.println("Simulation completed.");

        }

}