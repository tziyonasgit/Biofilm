import tkinter as tk  # imports tkinter library for GUI
from tkinter import *
from tkinter import filedialog  # for uploading file
import Animation
import SaveAnimation
# embeds matplotlib in tkinter GUI
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure


class BiofilmSimulationApp(tk.Tk):
    def __init__(self):  # constructor
        super().__init__()
        # setups main window
        self.title("Biofilm Simulation")
        self.geometry('600x600')
        self.centerWindow()
        self.filename = ""  # initialises selected filename

        self.createWidgets()  # creates the buttons the window

    # https://www.geeksforgeeks.org/how-to-center-a-window-on-the-screen-in-tkinter/
    def centerWindow(self):
        self.update_idletasks()
        width = self.winfo_width()
        height = self.winfo_height()
        screen_width = self.winfo_screenwidth()
        screen_height = self.winfo_screenheight()
        x = (screen_width - width) // 2  # gets x coord
        y = (screen_height - height) // 2  # gets y coord
        # sets the window to the centre
        self.geometry(f"{width}x{height}+{x}+{y}")

    def uploadFile(self):
        self.filename = filedialog.askopenfilename()  # stores filename

    def startSimulation(self):
        self.ax.clear()  # clears any existing plot
        Animation.startAnimation(self, self.filename, self.canvas, self.ax)
        SaveAnimation.startAnimation(self.filename)

    def createWidgets(self):
        # creates and arranges buttons on the window GUI
        uploadBtn = Button(self, text='Upload data file',
                           command=self.uploadFile)
        uploadBtn.pack(side='top')

        # sets the figure size and resolution
        self.fig = Figure(figsize=(5, 4), dpi=100)
        self.ax = self.fig.add_subplot(111)  # adds the single subplot
        # removes the ticks and labels from the plot
        self.ax.set_xticks([])
        self.ax.set_yticks([])
        self.ax.set_xticklabels([])
        self.ax.set_yticklabels([])

       # canvas to embed the matplotlib
        self.canvas = FigureCanvasTkAgg(self.fig, master=self)
        self.canvas.draw()
        self.canvas.get_tk_widget().pack(side='top', fill='both', expand=True)

        startBtn = Button(self, text='Start simulation',
                          command=self.startSimulation)
        startBtn.pack(side='top')


def main():
    # creates instance of the "application"
    app = BiofilmSimulationApp()
    app.mainloop()


if __name__ == '__main__':
    main()
