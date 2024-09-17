package backEnd.src;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

// class for managing simulation and creating the simulation environment and setting up its parts
public class SimulationModel {
        private static int iNutrients, totBMonomers, iEPSMonomers,
                        iBacteria, xBlocks, yBlocks;
        private static double duration;
        private Environment simEnviron;
        public volatile String run = "sync";
        public static CyclicBarrier barrier;
        //public static CyclicBarrier barrier2;
        public static Object runLock = new Object();
        public long startTime;
        public Timer timer;
        public static boolean reset = false;
        public static volatile int resetting;
        public static boolean initialBacteriaMade;

        // paramaterised constructor for simulation model
        public SimulationModel(int iEPSMonomers, int iNutrients, int iBacteria, int totBMonomers,
                        int xBlocks, int yBlocks, double duration)
        {
                SimulationModel.initialBacteriaMade = false;
                SimulationModel.iNutrients = iNutrients;
                SimulationModel.totBMonomers = totBMonomers;
                SimulationModel.iEPSMonomers = iEPSMonomers;
                SimulationModel.iBacteria = iBacteria;
                SimulationModel.duration = duration;
                SimulationModel.yBlocks = yBlocks;
                SimulationModel.xBlocks = xBlocks;
                SimulationModel.resetting = iBacteria;

                // Sets barrier with task to run in a thread at setting of barrier
                barrier = new CyclicBarrier(iBacteria, new Runnable() {
                        @Override
                        public void run() {
                                // Writes current activities to file
                                System.out.println(Thread.currentThread().getName() + " is writing to file");
                                Simulation.writeToFile();
                                Simulation.getActivities().clear();
                                // Notify all bacteria threads after writing to file
                                synchronized (runLock) {
                                        runLock.notifyAll(); // Notify all waiting threads
                                }
                        }
                });
                this.simEnviron = createEnvironment(iNutrients, totBMonomers, iEPSMonomers, iBacteria,
                                xBlocks, yBlocks);

                startSimulation();
        }

        public static int getBacteria()
        {
                return iBacteria;
        }

        // Method for resetting barrier that ensures each Bacterium does one action per timestep
        public synchronized static void resetBarrier()
                        throws InterruptedException, BrokenBarrierException {
                iBacteria = Environment.Bacteria.size();

                // Resets barrier with task to run in a thread at each reset
                SimulationModel.barrier = new CyclicBarrier(iBacteria, new Runnable() {
                        @Override
                        public void run() {
                                System.out.println(Thread.currentThread().getName() + " is writing to file2");
                                Simulation.writeToFile();
                                Simulation.getActivities().clear();
                                // Notify all bacteria threads after writing to file
                                synchronized (runLock) {
                                        runLock.notifyAll(); // Notify all waiting threads
                                }

                                System.out.println("New barrier set with " + iBacteria + " bacteria.");
                                // Synchronises and notifies bacteria that were produced in last timestep
                                synchronized (Environment.Bacteria.get(0).waiting) {
                                        Environment.Bacteria.get(0).waiting.notifyAll();
                                }
                        }
                });
        }

        // Method to start the simulation
        private void startSimulation() {
                startTime = System.currentTimeMillis(); // Capture the start time

                // Run the simulation
                biofilmSimulation(simEnviron);
        }

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

        // method for creating environment and its parts (blocks, nutrients, bacteria)
        private Environment createEnvironment(int nutrients, int totBMonomers, int EPSMonomers,
                        int bacteria, int xBlocks, int yBlocks) {
                Environment environ = new Environment(nutrients, totBMonomers, EPSMonomers,
                                bacteria, xBlocks, yBlocks);

                environ.createBlocks(xBlocks, yBlocks);

                System.out.println("Creating bacteria...");
                environ.createBacteria(bacteria, xBlocks, yBlocks, environ);

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

        // Method for running the simulation
        private void biofilmSimulation(Environment environ) {

                // creates new timer and a task to give to it
                Timer timer = new Timer();
                TimerTask durationCheckTask = new DurationCheckTask();
                this.timer = timer;

                timer.scheduleAtFixedRate(durationCheckTask, 0, 1000); // Check every 1 sec

        }

}