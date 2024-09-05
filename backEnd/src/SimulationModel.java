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
        }
        // Simulation paramaters still to be added here //

        class Helper extends TimerTask {
                public int i = 0;

                public void run() {
                        i++;
                        Simulation.writeToFile(i);
                        
                        synchronized (Simulation.activities)
                        {
                                Simulation.activities.clear();
                        }

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
                backEnd.src.Environment environ = new Environment(nutrients, totBMonomers, FBMonomers, EPSMonomers,
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

                Timer timer = new Timer();
                TimerTask task = new Helper();
                timer.schedule(task, 0, 5);

                try {
                        synchronized (run) {
                                run.wait();
                        }
                        timer.cancel();
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }

                // while (time < this.duration)
                // {
                // environ.BMonomers.get(0).bond(environ.BMonomers.get(1));
                // Bacterium tester = environ.Bacteria.get(0);
                // tester.tumble(tester.getBlock(), environ.environBlocks[1][1]);
                // tester.otherMove(tester.getBlock(), environ.environBlocks[1][1]);
                // tester.reproduce(environ.createBacterium());
                // tester.die();;
                // tester.collide(environ.Bacteria.get(1));;
                // tester.secrete(environ.createEPSMonomer(environ.environBlocks[1][1]));;
                // tester.attach(tester.getBlock());
                // tester.eat(environ.nutrients.get(0));
                // tester.consume(environ.BMonomers.get(0));
                // Simulation.riteToFile();
                // Simulation.activities.clear();
                // time ++;
                // }

                Bacterium tester = environ.Bacteria.get(0);
                tester.run(tester.getBlock(), environ.environBlocks[0][0]); // calls ether run or tumble motion which
                                                                            // calls move method

                // testing adding and removing bacterial monomers and nutrients from block's
                // LinkedList of
                // bacterial monomers and nutrients
                System.out.println(environ.nutrients.get(0).position.elements);
                System.out.println(environ.BMonomers.get(environ.BMonomers.size() - 1).position.elements);
                environ.Bacteria.get(0).eat(environ.nutrients.get(0));
                environ.Bacteria.get(0).consume(environ.BMonomers.get(environ.BMonomers.size() - 1));
                System.out.println(environ.nutrients.get(0).position.elements);
                System.out.println(environ.BMonomers.get(environ.BMonomers.size() - 1).position.elements);

                System.out.println("");
                System.out.println("Simulation fully set up. Simulation running...");
                System.out.println("Simulation completed.");

                return environ;
        }

}