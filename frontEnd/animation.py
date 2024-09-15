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
        self.frames = []  # initialize list of frames
        self.currentLine = 0
        self.direction = None
        self.mode = mode
        self.setup()

    def setup(self):
        # ensures axis has equal aspect ratio and no lines and ticks
        self.ax.set_aspect('equal')
        self.ax.set_xlim(0, 100)
        self.ax.set_ylim(0, 100)
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

        # Create custom patches for the legend
        green_patch = patches.Patch(color='green', label='Parent Bacteria')
        blue_patch = patches.Patch(color='blue', label='Child Bacteria')

        # Add the legend to the plot
        self.ax.legend(handles=[green_patch, blue_patch], loc='upper right')

    def spawn(self, cellID, father=None):
        # if father is None:
        #     initialPosition = np.array([0.0, 0.0])  # starts at the center
        #     bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli",
        #                           position=initialPosition, length=2.0, width=1.0, colour='green')
        # else:
        #     initialPosition = np.array(
        #         [father.getPositionX()+1, father.getPositionY()+1])
        #     bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli",
        #                           position=initialPosition, length=2.0, width=1.0, colour='blue', father=father)
        # self.bacteria.append(bacterium)  # adds bacterium to list bacteria

        # bacterium.draw()  # draws bacterium on the plot
        initialPosition = np.array([0.0, 0.0]) if father is None else np.array(
            [father.getPositionX() + 1, father.getPositionY() + 1])
        bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli", position=initialPosition,
                              length=2.0, width=1.0, colour='green' if father is None else 'blue', father=father)
        self.bacteria.append(bacterium)
        bacterium.draw()
        # self.canvas.draw_idle()  # updates the canvas

    def reproduce(self, cellID, childID):
        # # prepares the bacterium for splitting
        # father = None
        # for bacterium in self.bacteria:
        #     if bacterium.id == cellID:
        #         break
        # # stores the parent bacterium as the father in the daughter (new) bacterium
        # father = bacterium
        # self.spawn(childID, father)
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
                        # coordinateInitial = tuple(
                        #     map(float, initialPoint.strip("()").split(",")))
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
                else:
                    print(f"Unknown action for Cell {cellID}: {action}")
            else:
                print(f"Malformed line: {line}")
            # updates the animation frame in the plot
            self.updateFrame(self.currentLine)
            # print(f"Current bacteria: {[b.id for b in self.bacteria]}")

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
        # reads all the lines in the uploaded file
        ani.TotalLines = file.readlines()

    ani.currentLine = 0
    ani.nextStep()
