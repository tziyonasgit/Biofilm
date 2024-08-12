import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.animation import FuncAnimation


def draw_bacillus(grid, center=(0, 0), length=4, width=1, color='green'):
    # Calculate positions
    radius = width / 2
    centreBodyX, centreBodyY = center

    # Create the central rod (rectangle)
    rectangleBody = patches.Rectangle((centreBodyX - length / 2, centreBodyY - width / 2),
                                      length, width, color=color)

    # Create the rounded ends (circles)
    leftEnd = patches.Circle(
        (centreBodyX - length / 2, centreBodyY), radius, color=color)
    rightEnd = patches.Circle(
        (centreBodyX + length / 2, centreBodyY), radius, color=color)

    # Add shapes to the plot
    grid.add_patch(rectangleBody)
    grid.add_patch(leftEnd)
    grid.add_patch(rightEnd)

    return rectangleBody, leftEnd, rightEnd


def simulation(filename):
    print(filename)
