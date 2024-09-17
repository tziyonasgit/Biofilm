
# Biofilm prototype

## Prerequisites
- Python 3.x
Required Python packages:
- matplotlib
- numpy
- tkinter
- ffmpeg (for saving animations as video files)

You can install the required Python packages using pip:
``` pip install matplotlib numpy ```

## Usage

1. Running the Simulation:

To run the simulation, execute the BiofilmSimulationApp.py script:

``` python BiofilmSimulationApp.py ``` 

The GUI will open, allowing you to upload a command file and start the simulation.

2. Command File Structure:

The simulation relies on a text file containing commands that dictate the actions of the bacteria. The structure of the command file is as follows:

```  time#cellID#action#direction ``` 

- time: The time step at which the command should be executed.
- cellID: The ID of the bacterium affected by the command.
- action: The action to be performed (spawn, move, split, die).
- direction: The direction for movement (up, down, left, right), if applicable.


