from matplotlib import patches
import numpy as np
import matplotlib.pyplot as plt
from typing import Optional  # specifies optional types in function arguments


class Bacterium:
    def __init__(self, ax, id: int, age: int, strain: str = None, position:  np.ndarray = None, length: float = 1.0,
                 width: float = 0.5, colour: str = "green", father: Optional['Bacterium'] = None):
        self.ax = ax
        self.id = id  # unique identifier for bacterium
        self.age = 0  # initialise age to zero
        self.strain = "E.coli"
        # initial position
        if position is None:  # default initial position to the origin of the plot
            self.position = np.array([0.0, 0.0])
        else:
            self.position = position
        self.length = length  # length of bacterium body
        self.width = width  # width of bacterium body
        self.colour = colour  # colour of bacterium body
        self.father = father  # father Bacterium if necessary

        # graphical elements of bacterium body
        self.rectangleBody = None
        self.leftEnd = None
        self.rightEnd = None

        self.createBody()

    def updateFather(self, Bacterium):
        # updates the reference to the father Bacterium when its parent bacterium splits
        self.father = Bacterium

    def createBody(self):
        # creates rod-shape
        radius = self.width / 2
        centreBodyX, centreBodyY = self.position

        # central rectangular body
        self.rectangleBody = patches.Rectangle(
            (centreBodyX - self.length / 2, centreBodyY - self.width / 2),
            self.length, self.width, color=self.colour
        )

        # create rounded ends (circles)
        self.leftEnd = patches.Circle(
            (centreBodyX - self.length / 2, centreBodyY), radius, color=self.colour
        )
        self.rightEnd = patches.Circle(
            (centreBodyX + self.length / 2, centreBodyY), radius, color=self.colour
        )

    def getPosition(self):
        # returns the current position of the bacterium
        return self.position

    def draw(self):
        self.removePatches()  # removes previous graphical elements
        self.createBody()

       # adds new patches to axis
        self.ax.add_patch(self.rectangleBody)
        self.ax.add_patch(self.leftEnd)
        self.ax.add_patch(self.rightEnd)

    def removePatches(self):
        # removes existing patches on plot
        if self.rectangleBody is not None:
            self.rectangleBody.set_visible(False)  # hides the patch
            self.rectangleBody = None
        if self.leftEnd is not None:
            self.leftEnd.set_visible(False)  # hides the patch
            self.leftEnd = None
        if self.rightEnd is not None:
            self.rightEnd.set_visible(False)  # hides the patch
            self.rightEnd = None

    def move(self, direction):
        # moves the bacterium in the specified direction
        if direction == "up":
            self.position[1] += 1
        elif direction == "down":
            self.position[1] -= 1
        elif direction == "left":
            self.position[0] -= 1
        elif direction == "right":
            self.position[0] += 1

        self.draw()  # redraws the bacterium at the new position
