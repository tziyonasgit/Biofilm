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


class NonMotile:
    def __init__(self, root, canvas, ax, filename):
        self.root = root  # Store the root Tkinter widget
        self.ax = ax  # axis on plot
        self.bacterium = None
        self.bacteria = []  # intialise empty list of Bacterium
        self.fig = ax.figure  # figure on the axis
        self.canvas = canvas  # tkinter canvas that holds the plot
        self.filename = filename
        self.lines = []  # initialize list of file lines
        self.frames = []  # initialize list of frames
        self.currentLine = 0
        self.direction = None
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

    def spawn(self, cellID, bacterium):
        # creates a new bacterium at the centre of plot
        initial_position = np.array([0.0, 0.0])  # starts at the center
        bacterium = Bacterium(self.ax, id=cellID, age=0, strain="E.coli",
                              position=initial_position, length=2.0, width=1.0, colour='green')
        self.bacteria.append(bacterium)  # adds bacterium to list bacteria

        bacterium.draw()  # draws bacterium on the plot
        self.canvas.draw_idle()  # updates the canvas

    def split(self, cellID):
        # prepares the bacterium for splitting
        global father
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                break
        # stores the parent bacterium as the father in the daughter (new) bacterium
        father = bacterium

    def die(self, cellID):
        # removes bacterium from the plot and list of bacteria
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                break
        bacterium.removePatches()
        self.bacteria.remove(bacterium)

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

    def processLine(self):
        # reads each line of textfile and determines the next action in simulation
        global father
        if self.currentLine < len(self.lines):
            line = self.lines[self.currentLine]
            self.currentLine += 1

            # extracts commands and objects from line
            line = line.strip()
            parts = line.split('#')

            if len(parts) >= 2:
                cellID = int(parts[1])  # e.g. "1", "2"
                action = parts[2]  # e.g. "spawn", "move", "split", "die"

            if action == "spawn":
                print(f"Cell {cellID} spawns.")
                if cellID == 1:
                    self.spawn(cellID, None)
                else:
                    self.spawn(cellID, father)
            elif action == "move":
                if len(parts) > 3:
                    direction = parts[3]  # extracts dircetion of movement
                    print(f"Cell {cellID} moves {direction}.")
                    for bacterium in self.bacteria:
                        if bacterium.id == cellID:
                            bacterium.move(direction)
                            break
            elif action == "split":
                print(f"Cell {cellID} splits.")
                for bacterium in self.bacteria:
                    if bacterium.id == cellID:
                        self.split(cellID)
                        break
            elif action == "die":
                print(f"Cell {cellID} dies.")
                self.die(cellID)
            else:
                print(f"Unknown action for Cell {cellID}: {action}")

            # updates the animation frame in plot
            self.updateFrame(self.currentLine)

            # adds a delay of 500 ms before next line is processed
            self.root.after(500, self.processLine)


def startAnimation(root, filename, canvas, ax):
    # starts the animation by creating an object
    animation = Animation(root, canvas, ax, filename)
    with open(animation.filename, 'r') as file:
        animation.lines = file.readlines()  # reads all the lines in the uploaded file

        animation.processLine()
