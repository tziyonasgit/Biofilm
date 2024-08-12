import matplotlib.pyplot as plt  # simple plots
import matplotlib.patches as patches
from matplotlib.animation import FuncAnimation
import numpy as np
from Bacterium import *
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

bacterium = None


class BiofilmAnimation:
    def __init__(self, canvas, ax):
        self.ax = ax
        self.bacterium = None
        self.bacteria = []  # Ensure this is initialized correctly
        self.fig = ax.figure
        self.canvas = canvas
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

        initial_position = np.array([0.0, 0.0])  # Starting at the center
        bacterium = Bacterium(self.ax, id=1, age=0, strain="E.coli",
                              position=initial_position, length=2.0, width=1.0, colour='green')
        self.bacteria.append(bacterium)  # adds bacterium to list bacteria

        bacterium.draw()

    def updateFrame(self, frame):

        # Remove previous patches
        for patch in self.ax.patches[:]:
            patch.remove()

        for bacterium in self.bacteria:
            if frame == 0:
                bacterium.position[0] += 1  # Move right by 1 unit
            elif frame == 1:
                bacterium.position[0] -= 1  # Move left by 1 unit

            bacterium.draw()

        self.canvas.draw_idle()

        if frame == 1:
            self.ani.event_source.stop()

    def run(self):
        self.ani = FuncAnimation(self.fig, self.updateFrame,
                                 frames=2, interval=500, blit=False)
        # self.updateFrame()
        self.canvas.draw()  # This draws the first frame on the Tkinter canvas


def startAnimation(canvas, ax):
    animation = BiofilmAnimation(canvas, ax)
    animation.run()
