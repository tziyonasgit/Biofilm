package backEnd.src;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

// class for managing simulation and creating the simulation environment and setting up its parts
public class SimulationModel {
        public static int iNutrients, iFBMonomers, totBMonomers, iEPSMonomers,
                        iBacteria, xBlocks, yBlocks;
        public static double duration;
        Environment simEnviron;
        public volatile String run = "sync";
        public static CyclicBarrier barrier;
        public static CyclicBarrier barrier2;
        public static Object runLock = new Object();
        public long startTime;
        public Timer timer;
        public static boolean reset = false;
        public static volatile int resetting;
        public static boolean initialBacteriaMade;
        // Simulation paramaters still to be added here //

        // paramaterised constructor for simulation model
        public SimulationModel(int iFBMonomers, int iEPSMonomers, int iNutrients, int iBacteria, int totBMonomers,
                        int xBlocks, int yBlocks, double duration)
        // Simulation paramaters still to be added here //
        {
                SimulationModel.initialBacteriaMade = false;
                SimulationModel.iNutrients = iNutrients;
                SimulationModel.iFBMonomers = iFBMonomers;
                SimulationModel.totBMonomers = totBMonomers;
                SimulationModel.iEPSMonomers = iEPSMonomers;
                SimulationModel.iBacteria = iBacteria;
                SimulationModel.duration = duration;
                SimulationModel.yBlocks = yBlocks;
                SimulationModel.xBlocks = xBlocks;
                SimulationModel.resetting = iBacteria;

                barrier = new CyclicBarrier(iBacteria, new Runnable() {
                        @Override
                        public void run() {
                                // System.out.println("Writing to file with " + iBacteria + " bacteria");
                                // for (int i = 0; i < Simulation.activities.size(); i++) {
                                // System.out.println(Simulation.activities.get(i));
                                // }
                                System.out.println(Thread.currentThread().getName() + " is writing to file");
                                Simulation.writeToFile();
                                Simulation.activities.clear();
                                // Notify all bacteria threads after writing to file
                                synchronized (runLock) {
                                        runLock.notifyAll(); // Notify all waiting threads
                                }
                        }
                });
                this.simEnviron = createEnvironment(iNutrients, iFBMonomers, totBMonomers, iEPSMonomers, iBacteria,
                                xBlocks, yBlocks);

                startSimulation();
        }

        public synchronized static void resetBarrier()
                        throws InterruptedException, BrokenBarrierException {
                // Simulation.writeToFile();
                // Simulation.activities.clear();
                // System.out.println("resetting");
                // barrier.await();
                // iBacteria = Environment.Bacteria.size();

                SimulationModel.barrier = new CyclicBarrier(iBacteria, new Runnable() {
                        @Override
                        public void run() {
                                // System.out.println("Writing to file with " + iBacteria + " bacteria");
                                // for (int i = 0; i < Simulation.activities.size(); i++) {
                                // System.out.println(Simulation.activities.get(i));
                                // }
                                // synchronized (Simulation.activities) {
                                System.out.println(Thread.currentThread().getName() + " is writing to file2");
                                Simulation.writeToFile();
                                Simulation.activities.clear();
                                // }
                                // Notify all bacteria threads after writing to file
                                synchronized (runLock) {
                                        runLock.notifyAll(); // Notify all waiting threads
                                }
                        }
                });
                System.out.println("New barrier set with " + iBacteria + " bacteria.");
                Environment.Bacteria.get(0).waiting.notifyAll();
                // bac.reset.notifyAll();
        }

        // Method to start the simulation
        private void startSimulation() {
                startTime = System.currentTimeMillis(); // Capture the start time

                // Run the simulation
                biofilmSimulation(simEnviron);
        }
        // Simulation paramaters still to be added here //

        // class for creating and running a task to be completed on each run of a timer
        class DurationCheckTask extends TimerTask {
                @Override
                public void run() {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeInSeconds = (currentTime - startTime) / 1000.0;

                        if (elapsedTimeInSeconds >= duration) {
                                synchronized (runLock) {
                                        System.out.println("duration is:" + duration);
                                        System.out.println("Time elapsed is:" + elapsedTimeInSeconds);
                                        System.out.println("Duration reached. Notifying completion...");
                                        timer.cancel(); // Cancel the timer
                                        System.out.println("Simulation is done!");
                                        System.exit(0);
                                }

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
                SimulationModel.initialBacteriaMade = true;
                return environ;
        }

        public void biofilmSimulation(Environment environ) {

                // creates new timer and a task to give to it
                Timer timer = new Timer();
                TimerTask durationCheckTask = new DurationCheckTask();
                this.timer = timer;

                timer.scheduleAtFixedRate(durationCheckTask, 0, 1000); // Check every 1 sec

        }

}