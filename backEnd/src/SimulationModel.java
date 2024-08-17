package backEnd.src;

// class for managing simulation and creating the simulation environment and setting up its parts
public class SimulationModel {
        int iNutrients, mNutrients, iFBMonomers, totBMonomers, mBMonomers, iEPSMonomers, mEPSMonomers,
                        iBacteria, mBacteria, duration, yBlocks, xBlocks;
        Environment simEnviron;
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
                                yBlocks, xBlocks);
                // Simulation paramaters still to be added here //
        }

        // method for creating environment and its parts (blocks, monomers, nutrients,
        // bacteria)
        // for now also used for hardcoded demoing of methods and functionality //
        public Environment createEnvironment(int nutrients, int FBMonomers, int totBMonomers, int EPSMonomers,
                        int bacteria,
                        int yBlocks, int xBlocks) {
                Environment environ = new Environment(nutrients, totBMonomers, FBMonomers, EPSMonomers, bacteria,
                                yBlocks,
                                xBlocks);

                environ.createBlocks(yBlocks, xBlocks);

                System.out.println("");
                System.out.print("Bacteria: ");
                environ.createBacteria(bacteria, yBlocks, xBlocks);

                System.out.print("Free Bacterial Monomers: ");
                environ.createMonomers(FBMonomers, "bacterial");

                System.out.print("EPS Monomers: ");
                environ.createMonomers(EPSMonomers, "EPS");

                System.out.print("Nutrients: ");
                environ.createNutrients(nutrients);

                System.out.println("");
                System.out.println("Simulation fully set up. Simulation running...");

                // below for hardcoded demo //
                environ.BMonomers.get(0).bond(environ.BMonomers.get(1));
                Bacterium tester = environ.Bacteria.get(0);
                tester.tumble(tester.getBlock(), environ.environBlocks[1][1]);
                tester.otherMove(tester.getBlock(), environ.environBlocks[1][1]);
                tester.reproduce(environ.createBacterium());
                tester.die();
                ;
                tester.collide(environ.Bacteria.get(1));
                ;
                tester.secrete(environ.createEPSMonomer(environ.environBlocks[1][1], "EPS", 'r', "covid"));
                ;
                tester.attach(tester.getBlock());
                tester.eat(environ.nutrients.get(0));
                tester.consume(environ.BMonomers.get(0));
                // hardcoded demo ends //

                System.out.println("Simulation completed.");

                return environ;
        }

}