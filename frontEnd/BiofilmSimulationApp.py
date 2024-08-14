import tkinter as tk
from tkinter import *
from tkinter import filedialog
import Animation
import SaveAnimation
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure


class BiofilmSimulationApp(tk.Tk):
    def __init__(self):  # constructor
        super().__init__()
        # homescreen
        self.title("Biofilm Simulation")
        self.geometry('600x600')
        self.centerWindow()
        self.filename = ""

        self.createWidgets()

    # https://www.geeksforgeeks.org/how-to-center-a-window-on-the-screen-in-tkinter/
    def centerWindow(self):
        self.update_idletasks()
        width = self.winfo_width()
        height = self.winfo_height()
        screen_width = self.winfo_screenwidth()
        screen_height = self.winfo_screenheight()
        x = (screen_width - width) // 2
        y = (screen_height - height) // 2
        self.geometry(f"{width}x{height}+{x}+{y}")

    def uploadFile(self):
        self.filename = filedialog.askopenfilename()

    def startSimulation(self):
        self.ax.clear()
        Animation.startAnimation(self, self.filename, self.canvas, self.ax)
        SaveAnimation.startAnimation(self.filename)

    def createWidgets(self):
        uploadBtn = Button(self, text='Upload data file',
                           command=self.uploadFile)
        uploadBtn.pack(side='top')

        # Creating a Matplotlib figure
        self.fig = Figure(figsize=(5, 4), dpi=100)
        self.ax = self.fig.add_subplot(111)  # Create a single subplot
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

        # Creating a canvas to embed the Matplotlib figure
        self.canvas = FigureCanvasTkAgg(self.fig, master=self)
        self.canvas.draw()
        self.canvas.get_tk_widget().pack(side='top', fill='both', expand=True)

        startBtn = Button(self, text='Start simulation',
                          command=self.startSimulation)
        startBtn.pack(side='top')


def main():
    app = BiofilmSimulationApp()
    app.mainloop()


if __name__ == '__main__':
    main()
