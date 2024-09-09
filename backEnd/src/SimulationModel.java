package backEnd.src;

import java.util.Timer;
import java.util.TimerTask;

// class for managing simulation and creating the simulation environment and setting up its parts
public class SimulationModel {
        public int iNutrients, iFBMonomers, totBMonomers, iEPSMonomers,
                        iBacteria, xBlocks, yBlocks;
        public static double duration;
        Environment simEnviron;
        public volatile String run = "sync";
        // Simulation paramaters still to be added here //

        // paramaterised constructor for simulation model
        public SimulationModel(int iFBMonomers, int iEPSMonomers, int iNutrients, int iBacteria, int totBMonomers,
                        int xBlocks, int yBlocks, double duration)
        // Simulation paramaters still to be added here //
        {
                this.iNutrients = iNutrients;
                this.iFBMonomers = iFBMonomers;
                this.totBMonomers = totBMonomers;
                this.iEPSMonomers = iEPSMonomers;
                this.iBacteria = iBacteria;
                SimulationModel.duration = duration;
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
                        // synchronizes on ArrayList of activities to ensure actions not lost if a
                        // bacterium
                        // is busy adding an activity using the recActivities method in Simulation.java
                        synchronized (Simulation.activities) {
                                // writes activities in ArrayList of activities to file and empties the
                                // ArrayList
                                Simulation.writeToFile(i);
                                Simulation.activities.clear();
                        }
                        i++;
                        

                        // if task, and hence number of timesteps done, has been completed as many times
                        // as
                        // the given duration, timer is stopped (notified that can cancel)
                        if (i == duration) {
                                synchronized (run) {
                                        run.notifyAll();
                                        System.out.println("Simulation completed.");
                                }
                                System.exit(0);
                                
                        }
                }

        }

        // method for creating environment and its parts (blocks, monomers, nutrients,
        // bacteria)
        // for now also used for hardcoded demoing of methods and functionality //
        public Environment createEnvironment(int nutrients, int FBMonomers, int totBMonomers, int EPSMonomers,
                        int bacteria, int xBlocks, int yBlocks) {
                Environment environ = new Environment(nutrients, totBMonomers, FBMonomers, EPSMonomers,
                                bacteria, xBlocks, yBlocks);

                environ.createBlocks(xBlocks, yBlocks);

                System.out.println("Creating bacteria...");
                environ.createBacteria(bacteria, xBlocks, yBlocks, environ);

                System.out.println("Creating bacterial monomers...");
                environ.createMonomers(FBMonomers, "bacterial", xBlocks, yBlocks);

                System.out.println("Creating EPS monomers...");
                environ.createMonomers(EPSMonomers, "EPS", xBlocks, yBlocks);

                System.out.println("Creating nutrients...");
                environ.createNutrients(nutrients, xBlocks, yBlocks);
                System.out.println(" ");

                System.out.println("Simulation fully set up. Simulation running...");
                
                environ.initialise.countDown();

                return environ;
        }

        public void biofilmSimulation(Environment environ) {

                // creates new timer and a task to give to it
                Timer timer = new Timer();
                TimerTask task = new Helper();

                // schedules timer to run and perform task every time period
                timer.schedule(task, 0, 10);

                // runs timer over and over until notified to cancel and stop
                try {

                        // Bacterium tester = environ.Bacteria.get(0);
                        // tester.runMove(tester.getBlock(), environ.environBlocks[0][0]); // calls
                        // // either
                        // run or tumble motion which
                        // calls move method
                        synchronized (run) {
                                run.wait();
                        }
                        timer.cancel();
                        // System.exit(0); // Exit the program

                } catch (InterruptedException e) {
                        e.printStackTrace();
                }

                // testing adding and removing bacterial monomers and nutrients from block's
                // LinkedList of
                // bacterial monomers and nutrients
                // System.out.println(environ.nutrients.get(0).position.nutrients);
                // System.out.println(environ.BMonomers.get(environ.BMonomers.size() -
                // 1).position.bacMonomers);

                // environ.Bacteria.get(0).eat(environ.nutrients.get(0));
                // environ.Bacteria.get(0).consume(environ.BMonomers.get(environ.BMonomers.size()
                // - 1));
                // System.out.println(environ.nutrients.get(0).position.nutrients);
                // System.out.println(environ.BMonomers.get(environ.BMonomers.size() -
                // 1).position.bacMonomers);

        }

}