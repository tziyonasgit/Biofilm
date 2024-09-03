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

    def spawn(self, cellID, father=None):
        if father is None:
            initialPosition = np.array([0.0, 0.0])  # starts at the center
            bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli",
                                  position=initialPosition, length=2.0, width=1.0, colour='green')
        else:
            initialPosition = np.array(
                [father.getPositionX()+1, father.getPositionY()+1])
            bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli",
                                  position=initialPosition, length=2.0, width=1.0, colour='blue', father=father)
        self.bacteria.append(bacterium)  # adds bacterium to list bacteria

        bacterium.draw()  # draws bacterium on the plot
        self.canvas.draw_idle()  # updates the canvas

    def split(self, cellID, childID):
        # prepares the bacterium for splitting
        father = None
        for bacterium in self.bacteria:
            if bacterium.id == cellID:
                break
        # stores the parent bacterium as the father in the daughter (new) bacterium
        father = bacterium
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
                    self.spawn(cellID, None)
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
                    childID = parts[4]
                    print(f"Cell {cellID} splits.")
                    self.split(cellID, childID)
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
