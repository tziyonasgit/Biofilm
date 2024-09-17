import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation, FFMpegWriter
import numpy as np
from Bacterium import *
import os


class SaveAnimation:
    def __init__(self, filename, mode):
        self.fig, self.ax = plt.subplots(figsize=(12, 8))
        self.filename = filename
        self.bacteria = []  # Initialize empty list of Bacterium
        self.totalLines = []  # Initialize list of file lines
        self.currentLine = 0
        self.parameters = []
        self.direction = np.array([1.0, 0.0])
        self.mode = mode
        self.TimeStepLines = []  # Initialize TimeStepLines

        self.writer = FFMpegWriter(
            fps=20, metadata=dict(artist='Me'), bitrate=1800)

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

        parameterText = (
            f"Starting bacterial monomers: {iBacteriaMonomers}\n"
            f"Starting EPS monomers: {iEPSMonomers}\n"
            f"Starting nutrients: {iNutrients}\n"
            f"Starting bacteria: {iBacteria}\n"
            f"Simulation width: {width}\n"
            f"Simulation height: {height}\n"
            f"Simulation duration: {duration}"
        )
        # Ensures axis has equal aspect ratio and no lines and ticks
        self.ax.set_aspect('equal')
        self.ax.set_xlim(-1, width + 1)
        self.ax.set_ylim(-1, height + 1)
        self.ax.set_xticks([])
        self.ax.set_yticks([])

        self.ax.annotate(parameterText, xy=(-0.415, 0.87), xycoords='axes fraction', fontsize=10,
                         verticalalignment='center', bbox=dict(boxstyle="round,pad=0.3", edgecolor="black", facecolor="white"))

        # Create your existing patches
        # Assuming you have green_patch defined
        green_patch = patches.Patch(color='green', label='Parent Bacterium')
        # Assuming you have orange_patch defined
        orange_patch = patches.Patch(color='orange', label='Child Bacterium')

        # Create new patches for PSL and EPS
        psl_patch = patches.Patch(color='red', label='PSL')
        eps_patch = patches.Patch(color='blue', label='EPS')

        # Combine all patches into a single list
        all_patches = [green_patch, orange_patch, psl_patch, eps_patch]

        # Add the combined patches to the legend
        self.ax.legend(handles=all_patches,
                       loc='upper right', bbox_to_anchor=(-0.03, 0.69),
                       frameon=True, framealpha=1, edgecolor='black')

    def spawn(self, cellID, father=None):
        initialPosition = np.array([0.0, 0.0]) if father is None else np.array(
            [father.getPositionX() + 1, father.getPositionY() + 1])
        bacterium = Bacterium(self.ax, self.mode, id=cellID, age=0, strain="E.coli", position=initialPosition,
                              length=2.0, width=1.0, colour='green' if father is None else 'blue', father=father)
        self.bacteria.append(bacterium)
        bacterium.draw()

    def reproduce(self, cellID, childID):
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
        if self.currentLine < len(self.totalLines):
            line = self.totalLines[self.currentLine].strip()
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
                if action == "Spawn":
                    self.spawn(cellID)
                elif (action == "Run") or (action == "Tumble"):
                    if len(parts) > 4:
                        coordinateFinal = tuple(
                            map(float, parts[4].strip("()").split(",")))
                        bacterium = next(
                            (b for b in self.bacteria if b.id == cellID), None)
                        if bacterium:
                            if (action == "Run"):
                                bacterium.run(coordinateFinal)
                            elif (action == "Tumble"):
                                bacterium.tumble(coordinateFinal)
                elif action == "Reproduce":
                    childID = int(parts[4])
                    self.reproduce(cellID, childID)
                elif action == "Die":
                    self.die(cellID)
                elif action == "Secrete":
                    for bacterium in self.bacteria:  # iterates through bacterium objects until cell.ID matches
                        if bacterium.id == cellID:
                            bacterium.dropEPS()
                        break
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
        lines = file.readlines()
    saveAnimation.parameters = lines[:7]   # Get first 7 lines for parameters
    saveAnimation.totalLines = lines[8:]

    saveAnimation.setup()

    ani = FuncAnimation(saveAnimation.fig, saveAnimation.updateFrame, frames=len(
        saveAnimation.totalLines), interval=100, repeat=False)
    ani.save(os.path.splitext(os.path.basename(filename))[
             0] + ".mp4", writer=saveAnimation.writer)

    # ani.save("neww.mp4", writer=saveAnimation.writer)
