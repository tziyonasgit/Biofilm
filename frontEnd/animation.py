import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches
from matplotlib.animation import FuncAnimation,  FFMpegWriter
import numpy as np
from Bacterium import *
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import io

bacterium = None
fileLength = 0
father = None


class Animation:
    def __init__(self, root, canvas, ax, filename):
        self.root = root  # Store the root or a Tkinter widget
        self.ax = ax
        self.bacterium = None
        self.bacteria = []  # Ensure this is initialized correctly
        self.fig = ax.figure
        self.canvas = canvas
        self.filename = filename
        self.lines = []  # Initialize the lines list
        self.frames = []  # Initialize the frames list
        self.currentLine = 0
        self.direction = np.array([1.0, 0.0])  # Start moving to the right
        self.setup()

    def setup(self):
        self.ax.set_aspect('equal')
        self.ax.set_xlim(-20, 20)
        self.ax.set_ylim(-20, 20)
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

    def spawn(self, cellID, bacterium):
        initial_position = np.array([0.0, 0.0])  # Starting at the center
        bacterium = Bacterium(self.ax, id=cellID, age=0, strain="E.coli",
                              position=initial_position, length=2.0, width=1.0, colour='green')
        self.bacteria.append(bacterium)  # adds bacterium to list bacteria

        bacterium.draw()
        self.canvas.draw_idle()

    def split(self, cellID):
        global father
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                break
        father = bacterium

    def die(self, cellID):

        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                break
        bacterium.removePatches()
        self.bacteria.remove(bacterium)

    def updateFrame(self, frame):
        global fileLength
        # Remove previous patches
        for patch in self.ax.patches[:]:
            patch.remove()

        for bacterium in self.bacteria:
            bacterium.draw()

        self.canvas.draw_idle()

        if frame == (fileLength - 1):
            self.ani.event_source.stop()

    def processLine(self):
        global father
        if self.currentLine < len(self.lines):
            line = self.lines[self.currentLine]
            self.currentLine += 1

            # Process the line
            line = line.strip()
            parts = line.split('#')

            if len(parts) >= 2:
                cellID = int(parts[1])  # e.g., "1", "2"
                action = parts[2]  # e.g., "spawn", "move", "split", "die"

            if action == "spawn":
                print(f"Cell {cellID} spawns.")
                if cellID == 1:
                    self.spawn(cellID, None)
                else:
                    self.spawn(cellID, father)
            elif action == "move":
                if len(parts) > 3:
                    direction = parts[3]
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

            self.updateFrame(self.currentLine)

            self.root.after(500, self.processLine)  # Adjust delay as need


def startAnimation(root, filename, canvas, ax):
    animation = Animation(root, canvas, ax, filename)
    with open(animation.filename, 'r') as file:
        animation.lines = file.readlines()

        animation.processLine()  # Start processing the first line
