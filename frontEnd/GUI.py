import tkinter as tk
from tkinter import *
from tkinter import filedialog
from animation import *
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure


# global variables
filename = ""


class Screen(tk.Tk):
    def __init__(self):  # constructor
        super().__init__()


# https://www.geeksforgeeks.org/how-to-center-a-window-on-the-screen-in-tkinter/
def center_window(window):
    window.update_idletasks()
    width = window.winfo_width()
    height = window.winfo_height()
    screen_width = window.winfo_screenwidth()
    screen_height = window.winfo_screenheight()
    x = (screen_width - width) // 2
    y = (screen_height - height) // 2
    window.geometry(f"{width}x{height}+{x}+{y}")


def uploadFile():
    global filename
    filename = filedialog.askopenfilename()
    # print(filename)


def startSim(canvas, ax):
    ax.clear()
    startAnimation(canvas, ax)


def main():

    # homescreen
    home = Screen()  # creates instance of Screen class
    home.title("Biofilm Simulation")
    home.geometry('600x600')
    center_window(home)

    # Code to add widgets will go here
    uploadBtn = Button(home, text='Upload data file', command=uploadFile)
    uploadBtn.pack(side='top')

    # Creating a Matplotlib figure
    fig = Figure(figsize=(5, 4), dpi=100)
    ax = fig.add_subplot(111)  # Create a single subplot
    ax.set_xticks([])
    ax.set_yticks([])
    ax.set_xticklabels([])
    ax.set_yticklabels([])

    # Creating a canvas to embed the Matplotlib figure
    canvas = FigureCanvasTkAgg(fig, master=home)
    canvas.draw()
    canvas.get_tk_widget().pack(side='top', fill='both', expand=True)

    startBtn = Button(home, text='Start simulation',
                      command=lambda: startSim(canvas, ax))
    startBtn.pack(side='top')

    home.mainloop()  # runs the application


if __name__ == '__main__':
    main()
