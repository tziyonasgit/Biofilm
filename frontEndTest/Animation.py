import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches  # custom shapes
# animations and writing them to file
from matplotlib.animation import FuncAnimation,  FFMpegWriter
import numpy as np
from Bacterium import *
# embedding matplotlib in tkinter
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

# global variables
bacterium = None
fileLength = 0
father = None


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
        self.ax.set_xlim(-20, 20)
        self.ax.set_ylim(-20, 20)
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

    def updateFrame(self, frame):
        # updates the canvas with new graphical elements
        global fileLength
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

                if action == "spawn":
                    print(f"Cell {cellID} spawns.")
                    if cellID == 1:
                        self.spawn(cellID, None)
                    else:
                        self.spawn(cellID, father)
                elif action == "move":
                    if len(parts) > 4:
                        initialPoint = parts[3]  # extracts the initial point
                        finalPoint = parts[4]  # extracts the final point
                        coordinateInitial = tuple(
                            map(float, initialPoint.strip("()").split(",")))
                        coordinateFinal = tuple(
                            map(float, finalPoint.strip("()").split(",")))
                        print(f"Cell {cellID} moves from {
                            initialPoint} to {finalPoint}.")
                        for bacterium in self.bacteria:
                            if bacterium.id == cellID:
                                bacterium.move(coordinateFinal)
                                break
                elif action == "split":
                    print(f"Cell {cellID} splits.")
                    self.split(cellID)
                elif action == "die":
                    print(f"Cell {cellID} dies.")
                    self.die(cellID)
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
            else:
                self.TimeStepLines.append(line)

        # Continue to the next step after a delay of 500 ms
            self.root.after(50, self.nextStep)


def startAnimation(root, filename, canvas, ax, mode):
    # starts the animation by creating an object
    animation = Animation(root, canvas, ax, filename, mode)
    with open(animation.filename, 'r') as file:
        animation.lines = file.readlines()  # reads all the lines in the uploaded file

    animation.currentLine = 0
    animation.nextStep()
