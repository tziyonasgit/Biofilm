from matplotlib import patches
import numpy as np
import matplotlib.pyplot as plt
from typing import Optional  # Import Optional from the typing module


class Bacterium:

    def __init__(self, ax, id: int, age: int, strain: str = None, position:  np.ndarray = None, length: float = 1.0,
                 width: float = 0.5, colour: str = "green", father: Optional['Bacterium'] = None):
        self.ax = ax
        self.id = id
        self.age = 0
        self.strain = "E.coli"
        # initial position
        if position is None:
            self.position = np.array([0.0, 0.0])
        else:
            self.position = position
        self.length = length
        self.width = width
        self.colour = colour
        self.father = father  # Reference to the "father" bacterium

        # Initialize the shape attributes (patches)
        self.rectangleBody = None
        self.leftEnd = None
        self.rightEnd = None

        self.createBody()

    def updateFather(self, Bacterium):
        self.father = Bacterium

    def createBody(self):
        # Calculate the positions
        radius = self.width / 2
        centreBodyX, centreBodyY = self.position

        # Create the central rod (rectangle)
        self.rectangleBody = patches.Rectangle(
            (centreBodyX - self.length / 2, centreBodyY - self.width / 2),
            self.length, self.width, color=self.colour
        )

        # Create the rounded ends (circles)
        self.leftEnd = patches.Circle(
            (centreBodyX - self.length / 2, centreBodyY), radius, color=self.colour
        )
        self.rightEnd = patches.Circle(
            (centreBodyX + self.length / 2, centreBodyY), radius, color=self.colour
        )

    def getPosition(self):
        return self.position

    def draw(self):
        self.removePatches()
        # Recreate the shapes at the new position
        self.createBody()

        # Add the shapes to the axis
        self.ax.add_patch(self.rectangleBody)
        self.ax.add_patch(self.leftEnd)
        self.ax.add_patch(self.rightEnd)

    def removePatches(self):
        """Remove existing patches from the axis."""
        if self.rectangleBody is not None:
            self.rectangleBody.set_visible(False)  # Hide the patch
            self.rectangleBody = None
        if self.leftEnd is not None:
            self.leftEnd.set_visible(False)  # Hide the patch
            self.leftEnd = None
        if self.rightEnd is not None:
            self.rightEnd.set_visible(False)  # Hide the patch
            self.rightEnd = None

    def move(self, direction):
        if direction == "up":
            self.position[1] += 1
        elif direction == "down":
            self.position[1] -= 1
        elif direction == "left":
            self.position[0] -= 1
        elif direction == "right":
            self.position[0] += 1

        self.draw()  # Redraw the bacterium at the new position
