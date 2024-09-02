import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches  # custom shapes
# animations and writing them to file
from matplotlib.animation import FuncAnimation,  FFMpegWriter
import numpy as np
from Bacterium import *
import time
# embedding matplotlib in tkinter
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

from Animation import Animation

# global variables
bacterium = None
fileLength = 0
father = None


class Motile(Animation):
    def spawn(self, cellID, bacterium):
        # creates a new bacterium at the centre of plot
        initial_position = np.array([0.0, 0.0])  # starts at the center
        bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli",
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
    motileAni = Motile(root, canvas, ax, filename, mode)
    with open(motileAni.filename, 'r') as file:
        # reads all the lines in the uploaded file
        motileAni.TotalLines = file.readlines()

    motileAni.currentLine = 0
    motileAni.nextStep()
