import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches
from matplotlib.animation import FuncAnimation
import numpy as np
from Bacterium import *
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

bacterium = None
fileLength = 0


class BiofilmAnimation:
    def __init__(self, root, canvas, ax, filename):
        self.root = root  # Store the root or a Tkinter widget
        self.ax = ax
        self.bacterium = None
        self.bacteria = []  # Ensure this is initialized correctly
        self.fig = ax.figure
        self.canvas = canvas
        self.filename = filename
        self.lines = []  # Initialize the lines list
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

    def spawn(self, cellID):
        initial_position = np.array([0.0, 0.0])  # Starting at the center
        bacterium = Bacterium(self.ax, id=cellID, age=0, strain="E.coli",
                              position=initial_position, length=2.0, width=1.0, colour='green')
        self.bacteria.append(bacterium)  # adds bacterium to list bacteria

        bacterium.draw()
        self.canvas.draw_idle()

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

    # def run(self):
        # self.ani = FuncAnimation(self.fig, self.updateFrame,
        # frames = fileLength, interval = 500, blit = False)
            # self.updateFrame()
            # self.canvas.draw()  # This draws the first frame on the Tkinter canvas

    def processLine(self):
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
                self.spawn(cellID)
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
            elif action == "die":
                print(f"Cell {cellID} dies.")
            else:
                print(f"Unknown action for Cell {cellID}: {action}")

            self.updateFrame(self.currentLine)
            # Schedule the next line to be processed
            self.root.after(500, self.processLine)  # Adjust delay as need

    def run(self):
        with open(self.filename, 'r') as file:
            self.lines = file.readlines()

            self.processLine()  # Start processing the first line


def startAnimation(root, filename, canvas, ax):
    animation = BiofilmAnimation(root, canvas, ax, filename)
    animation.run()

    #         fileLength = sum(1 for _ in file)
    #         file.seek(0)  # Reset file pointer to the beginning

    # def startAnimation(filename, canvas, ax):
    #     global fileLength

    #     animation = BiofilmAnimation(canvas, ax)

    #     with open(filename, 'r') as file:
    #         fileLength = sum(1 for _ in file)
    #         file.seek(0)  # Reset file pointer to the beginning

    #         for line in file:
    #             # Strip any leading/trailing whitespace (including newlines)
    #             line = line.strip()
    #             # Split the line into parts
    #             parts = line.split('#')

    #             if len(parts) >= 2:
    #                 cellID = int(parts[1])  # e.g., "1", "2"
    #                 action = parts[2]  # e.g., "spawn", "move", "split", "die"

    #                 if action == "spawn":
    #                     print(f"Cell {cellID} spawns.")
    #                     animation.spawn(cellID)
    #                 elif action == "move":
    #                     if len(parts) > 3:
    #                         # e.g., "up", "down", "left", "right"
    #                         direction = parts[3]
    #                         print(f"Cell {cellID} moves {direction}.")
    #                         for bacterium in animation.bacteria:
    #                             if bacterium.id == cellID:
    #                                 bacterium.move(direction)
    #                                 break
    #                 elif action == "split":
    #                     print(f"Cell {cellID} splits.")
    #                 elif action == "die":
    #                     print(f"Cell {cellID} dies.")
    #                 else:
    #                     print(f"Unknown action for Cell {cellID}: {action}")
    #     animation.run()
