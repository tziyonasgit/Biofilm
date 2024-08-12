from matplotlib import patches
import numpy as np
import matplotlib.pyplot as plt


class Bacterium:

    def __init__(self, ax, id: int, age: int, strain: str = None, position:  np.ndarray = None, length: float = 1.0,
                 width: float = 0.5, colour: str = 'green'):
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

        # Initialize the shape attributes (patches)
        self.rectangleBody = None
        self.leftEnd = None
        self.rightEnd = None

        self.createBody()

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
        if self.rectangleBody in self.ax.patches:
            self.ax.patches.remove(self.rectangleBody)
        if self.leftEnd in self.ax.patches:
            self.ax.patches.remove(self.leftEnd)
        if self.rightEnd in self.ax.patches:
            self.ax.patches.remove(self.rightEnd)
