import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation, FFMpegWriter
import numpy as np
from Bacterium import *


class SaveAnimation:
    def __init__(self, filename, mode):
        self.fig, self.ax = plt.subplots()
        self.filename = filename
        self.bacteria = []  # Initialize empty list of Bacterium
        self.lines = []  # Initialize list of file lines
        self.currentLine = 0
        self.direction = np.array([1.0, 0.0])
        self.mode = mode
        self.TimeStepLines = []  # Initialize TimeStepLines
        self.setup()

        self.writer = FFMpegWriter(
            fps=20, metadata=dict(artist='Me'), bitrate=1800)

    def setup(self):
        # Ensures axis has equal aspect ratio and no lines and ticks
        self.ax.set_aspect('equal')
        self.ax.set_xlim(0, 100)
        self.ax.set_ylim(0, 100)
        self.ax.set_xticks([])
        self.ax.set_yticks([])

    def spawn(self, cellID, father=None):
        initialPosition = np.array([0.0, 0.0]) if father is None else np.array(
            [father.getPositionX() + 1, father.getPositionY() + 1])
        bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli", position=initialPosition,
                              length=2.0, width=1.0, colour='green' if father is None else 'blue', father=father)
        self.bacteria.append(bacterium)
        bacterium.draw()

    def split(self, cellID, childID):
        father = next((b for b in self.bacteria if b.id == cellID), None)
        if father:
            self.spawn(childID, father)

    def die(self, cellID):
        bacterium = next((b for b in self.bacteria if b.id == cellID), None)
        if bacterium:
            bacterium.removePatches()
            self.bacteria.remove(bacterium)

    def processCommandsForFrame(self, frame):
        """Process commands corresponding to the given frame."""
        if self.currentLine < len(self.lines):
            line = self.lines[self.currentLine].strip()
            if line == "*":
                self.processTimeStep()
                self.currentLine += 1
            else:
                self.TimeStepLines.append(line)
                self.currentLine += 1

    def processTimeStep(self):
        """Process all the commands stored in self.TimeStepLines."""
        for line in self.TimeStepLines:
            parts = line.split(':')
            if len(parts) >= 3:
                cellID = int(parts[1])
                action = parts[2]
                if action == "spawn":
                    self.spawn(cellID)
                elif action == "move":
                    if len(parts) > 4:
                        coordinateFinal = tuple(
                            map(float, parts[4].strip("()").split(",")))
                        bacterium = next(
                            (b for b in self.bacteria if b.id == cellID), None)
                        if bacterium:
                            bacterium.move(coordinateFinal)
                elif action == "split":
                    childID = int(parts[4])
                    self.split(cellID, childID)
                elif action == "die":
                    self.die(cellID)
        self.TimeStepLines = []

    def updateFrame(self, frame):
        self.ax.clear()
        self.setup()

        self.processCommandsForFrame(frame)

        for bacterium in self.bacteria:
            bacterium.draw()

        self.fig.canvas.draw()


def startAnimation(filename, mode):
    saveAnimation = SaveAnimation(filename, mode)
    with open(filename, 'r') as file:
        saveAnimation.lines = file.readlines()

    ani = FuncAnimation(saveAnimation.fig, saveAnimation.updateFrame, frames=len(
        saveAnimation.lines), interval=100, repeat=False)
    ani.save("bio.mp4", writer=saveAnimation.writer)
