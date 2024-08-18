import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches  # custom shapes
from matplotlib.animation import FuncAnimation, FFMpegWriter
import numpy as np
from Bacterium import *


class SaveAnimation:
    def __init__(self, filename):
        self.fig, self.ax = plt.subplots()
        self.filename = filename
        self.bacteria = []  # intialise empty list of Bacterium
        self.lines = []  # initialize list of file lines
        self.currentLine = 0
        self.direction = np.array([1.0, 0.0])
        self.setup()

        self.writer = FFMpegWriter(
            fps=3, metadata=dict(artist='Me'), bitrate=1800)

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

    def split(self, cellID):
        # prepares the bacterium for splitting
        # stores the parent bacterium as the father in the daughter (new) bacterium
        global father
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                father = bacterium
                break

    def die(self, cellID):
        # removes bacterium from the plot and list of bacteria
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                bacterium.removePatches()
                self.bacteria.remove(bacterium)
                break

    def updateFrame(self, frame):
        # updates the canvas with new graphical elements
        if self.currentLine >= len(self.lines):
            return

        # processes lines in file
        line = self.lines[self.currentLine]
        self.currentLine += 1

        line = line.strip()
        parts = line.split('#')

        if len(parts) >= 2:
            cellID = int(parts[1])
            action = parts[2]

        # determines the method to be called based on action stated in line
        if action == "spawn":
            if cellID == 1:
                self.spawn(cellID, None)
            else:
                self.spawn(cellID, father)
        elif action == "move":
            if len(parts) > 3:
                direction = parts[3]
                for bacterium in self.bacteria:
                    if bacterium.id == cellID:
                        bacterium.move(direction)
                        break
        elif action == "split":
            for bacterium in self.bacteria:
                if bacterium.id == cellID:
                    self.split(cellID)
                    break
        elif action == "die":
            self.die(cellID)

        # clears axis and redraws bacteria
        self.ax.clear()
        self.setup()
        for bacterium in self.bacteria:
            bacterium.draw()

        self.fig.canvas.draw()


def startAnimation(filename):
    # creates an instance of the animation
    saveAnimation = SaveAnimation(filename)
    with open(filename, 'r') as file:
        saveAnimation.lines = file.readlines()

        ani = FuncAnimation(saveAnimation.fig, saveAnimation.updateFrame,
                            frames=len(saveAnimation.lines), interval=7000, repeat=False)
        # save the animation as an MP4
        ani.save("biofilm_animation.mp4", writer=saveAnimation.writer)
