import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.animation import FuncAnimation, FFMpegWriter
import numpy as np
from Bacterium import *


class BiofilmAnimation:
    def __init__(self, filename):
        self.fig, self.ax = plt.subplots()
        self.filename = filename
        self.bacteria = []
        self.lines = []
        self.currentLine = 0
        self.direction = np.array([1.0, 0.0])
        self.setup()

        # For saving the animation
        self.writer = FFMpegWriter(
            fps=3, metadata=dict(artist='Me'), bitrate=1800)

    def setup(self):
        self.ax.set_aspect('equal')
        self.ax.set_xlim(-20, 20)
        self.ax.set_ylim(-20, 20)
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

    def spawn(self, cellID, bacterium):
        initial_position = np.array([0.0, 0.0])
        bacterium = Bacterium(self.ax, id=cellID, age=0, strain="E.coli",
                              position=initial_position, length=2.0, width=1.0, colour='green')
        self.bacteria.append(bacterium)
        bacterium.draw()

    def split(self, cellID):
        global father
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                father = bacterium
                break

    def die(self, cellID):
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                bacterium.removePatches()
                self.bacteria.remove(bacterium)
                break

    def updateFrame(self, frame):
        if self.currentLine >= len(self.lines):
            return

        # Process current line
        line = self.lines[self.currentLine]
        self.currentLine += 1

        line = line.strip()
        parts = line.split('#')

        if len(parts) >= 2:
            cellID = int(parts[1])
            action = parts[2]

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

        # Clear and redraw
        self.ax.clear()
        self.setup()  # Reapply axis settings
        for bacterium in self.bacteria:
            bacterium.draw()

        self.fig.canvas.draw()

    def run(self):
        with open(self.filename, 'r') as file:
            self.lines = file.readlines()

        ani = FuncAnimation(self.fig, self.updateFrame,
                            frames=len(self.lines), interval=7000, repeat=False)

        # Save the animation
        ani.save("biofilm_animation.mp4", writer=self.writer)


def startAnimation(filename):
    animation = BiofilmAnimation(filename)
    animation.run()
