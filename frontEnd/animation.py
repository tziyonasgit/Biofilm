import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches  # custom shapes
# animations and writing them to file
from matplotlib.animation import FuncAnimation,  FFMpegWriter
import numpy as np
from Bacterium import *
import time
# embedding matplotlib in tkinter
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

# global variables
bacterium = None
fileLength = 0


class Animation:
    def __init__(self, root, canvas, ax, filename, mode):
        self.root = root  # Store the root Tkinter widget
        self.ax = ax  # axis on plot
        self.bacterium = None
        self.bacteria = []  # intialise empty list of Bacterium
        self.fig = ax.figure  # figure on the axis
        self.canvas = canvas  # tkinter canvas that holds the plot
        self.filename = filename
        self.TotalLines = []  # initialize list of file lines
        self.TimeStepLines = []
        self.parameters = []
        self.frames = []  # initialize list of frames
        self.currentLine = 0
        self.direction = None
        self.mode = mode

    def setup(self):
        numbers = []
        for line in self.parameters:

            parts = line.split(':')
            if len(parts) > 1:
                number = parts[-1].strip()  # Extract the number part
                numbers.append(number)

        iBacteriaMonomers = numbers[0]
        iEPSMonomers = numbers[1]
        iNutrients = numbers[2]
        iBacteria = numbers[3]
        width = int(numbers[4])
        height = int(numbers[5])
        duration = numbers[6]

        parameter_text = (
            f"Starting bacterial monomers: {iBacteriaMonomers}\n"
            f"Starting EPS monomers: {iEPSMonomers}\n"
            f"Starting nutrients: {iNutrients}\n"
            f"Starting bacteria: {iBacteria}\n"
            f"Simulation width: {width}\n"
            f"Simulation height: {height}\n"
            f"Simulation duration: {duration}"
        )
        # ensures axis has equal aspect ratio and no lines and ticks
        self.ax.set_aspect('equal')
        self.ax.set_xlim(-1, width + 1)
        self.ax.set_ylim(-1, height + 1)
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

        # Create custom patches for the legend
        green_patch = patches.Patch(color='green', label='Parent Bacteria')
        orange_patch = patches.Patch(color='orange', label='Child Bacteria')

        self.ax.annotate(parameter_text, xy=(-0.55, 0.87), xycoords='axes fraction', fontsize=10,
                         verticalalignment='center', bbox=dict(boxstyle="round,pad=0.3", edgecolor="black", facecolor="white"))

        self.ax.legend(handles=[green_patch, orange_patch],
                       loc='upper right', bbox_to_anchor=(-0.044, 0.73), frameon=True, framealpha=1, edgecolor='black')

    def spawn(self, cellID, father=None):
        initialPosition = np.array([0.0, 0.0]) if father is None else np.array(
            [father.getPositionX() + 1, father.getPositionY() + 1])
        bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli", position=initialPosition,
                              length=2.0, width=1.0, colour='green' if father is None else 'orange', father=father)
        self.bacteria.append(bacterium)
        bacterium.draw()

    def reproduce(self, cellID, childID):
        father = next((b for b in self.bacteria if b.id == cellID), None)
        if father:
            self.spawn(childID, father)

    def die(self, cellID):
        # removes bacterium from the plot and list of bacteria
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                break
        bacterium.removePatches()
        self.bacteria.remove(bacterium)

    def updateFrame(self, frame):
        # updates the canvas with new graphical elements
        # global fileLength
        # removes previous graphical elements
        for patch in self.ax.patches[:]:
            patch.remove()

        # draws all present bacteria in their new positions
        for bacterium in self.bacteria:
            bacterium.draw()

        self.canvas.draw_idle()

        # stops the animation when the last frame is reached
        if frame == (fileLength - 1):
            self.ani.event_source.stop()

    def processTimeStep(self):
       # reads each line of the text file and determines the next action in simulation
        global father
        for line in self.TimeStepLines:

            # extracts commands and objects from the line
            line = line.strip()
            parts = line.split(':')

            # Ensure that parts contain enough elements
            if len(parts) >= 3:
                cellID = int(parts[1])  # e.g., "1", "2"
                action = parts[2]  # e.g., "spawn", "move", "split", "die"
                if action == "Spawn":
                    print(f"Bacterium {cellID} spawns.")
                    self.spawn(cellID)
                elif (action == "Run") or (action == "Tumble"):
                    if len(parts) > 4:
                        initialPoint = parts[3]  # extracts the initial point
                        finalPoint = parts[4]  # extracts the final point
                        coordinateFinal = tuple(
                            map(float, parts[4].strip("()").split(",")))
                        print(f"Bacterium {cellID} {action} from {
                            initialPoint} to {finalPoint}.")
                        bacterium = next(
                            (b for b in self.bacteria if b.id == cellID), None)
                        if bacterium:
                            if (action == "Run"):
                                bacterium.run(coordinateFinal)
                            elif (action == "Tumble"):
                                bacterium.tumble(coordinateFinal)
                elif action == "Reproduce":
                    childID = int(parts[4])
                    print(f"Bacterium {cellID} reproduces.")
                    self.reproduce(cellID, childID)
                elif action == "Die":
                    print(f"Bacterium {cellID} dies.")
                    self.die(cellID)
                elif action == "Consume":
                    monomerID = parts[5]
                    print(f"Bacterium {
                          cellID} consumes bacterial monomer {monomerID}.")
                elif action == "Secrete":
                    for bacterium in self.bacteria:  # iterates through bacterium objects until cell.ID matches
                        if bacterium.id == cellID:
                            bacterium.dropEPS()
                        break
                    print(f"Bacterium {
                          cellID} secretes EPS.")
                elif action == "Attach":
                    blockCoordinate = parts[3]
                    print(f"Bacterium {
                          cellID} attaches to {blockCoordinate}.")
                elif action == "Eat":
                    print(f"Bacterium {
                          cellID} eats nutrient.")
                elif action == "Collide":
                    otherBacID = parts[4]
                    print(f"Bacterium {
                          cellID} collides with bacterium {otherBacID}.")
                elif action == "Idle":
                    # block = tuple(
                    #     map(float, parts[3].strip("()").split(",")))
                    block = parts[3]
                    print(f"Bacterium {
                          cellID} is idle on {block}.")
                else:
                    print(f"Unknown action for Cell {cellID}: {action}")
            else:
                print(f"Malformed line: {line}")
            # updates the animation frame in the plot
            self.updateFrame(self.currentLine)

    def nextStep(self):
        if self.currentLine < len(self.TotalLines):
            line = self.TotalLines[self.currentLine]
            self.currentLine += 1
            if line.strip() == "*":
                self.processTimeStep()
                self.TimeStepLines = []
                self.updateFrame(self.currentLine)
            else:
                self.TimeStepLines.append(line)

        # Continue to the next step after a delay of 500 ms
            self.root.after(50, self.nextStep)


def startAnimation(root, filename, canvas, ax, mode):
    # starts the animation by creating an object
    ani = Animation(root, canvas, ax, filename, mode)
    with open(ani.filename, 'r') as file:
        lines = file.readlines()
    ani.parameters = lines[:7]   # Get first 7 lines for parameters
    ani.TotalLines = lines[8:]

    ani.setup()

    ani.currentLine = 0
    ani.nextStep()
