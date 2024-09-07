import tkinter as tk  # imports tkinter library for GUI
from tkinter import *
from tkinter import filedialog  # for uploading file
import SaveAnimation as SaveAnimation
# embeds matplotlib in tkinter GUI
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
import Animation
from pathlib import Path
from tkinter import Tk, Canvas, Entry, Text, Button, PhotoImage


OUTPUT_PATH = Path(__file__).parent
ASSETS_PATH = OUTPUT_PATH / "assets"


class BiofilmSimulationApp(tk.Tk):
    def __init__(self):  # constructor
        super().__init__()
        # setups main window
        self.title("")
        self.geometry('900x900')
        self.configure(bg="#FFFFFF")
        self.centerWindow()
        self.filename = ""  # initialises selected filename
        self.createWidgets()  # creates the buttons the window
        self.mode = ""

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
        # Create a new window for the simulation
        simulation_window = tk.Toplevel(self)
        simulation_window.title("Simulation Window")
        simulation_window.geometry("800x600")

        # Create the matplotlib figure and canvas in the new window
        fig = Figure(figsize=(5, 4), dpi=100)
        ax = fig.add_subplot(111)  # Add the single subplot

        canvas = FigureCanvasTkAgg(fig, master=simulation_window)
        canvas.draw()
        canvas.get_tk_widget().pack(side='top', fill='both', expand=True)
        self.mode = "Motile"
        ax.clear()  # clears any existing plot
        Animation.startAnimation(
            self, self.filename, canvas, ax, self.mode)
        SaveAnimation.startAnimation(self.filename, self.mode)

    def startMotile(self):
        self.mode = "Motile"
        self.ax.clear()  # clears any existing plot
        Animation.startAnimation(
            self, self.filename, self.canvas, self.ax, self.mode)
        SaveAnimation.startAnimation(self.filename, self.mode)

    def startNonMotile(self):
        self.mode = "NonMotile"
        self.ax.clear()  # clears any existing plot
        Animation.startAnimation(
            self, self.filename, self.canvas, self.ax, self.mode)

    def startPSLTrail(self):
        self.mode = "PSL"
        self.ax.clear()  # clears any existing plot
        Animation.startAnimation(self, self.filename,
                                 self.canvas, self.ax, self.mode)

    def startEPSMatrix(self):
        self.mode = "EPS"
        self.ax.clear()  # clears any existing plot
        Animation.startAnimation(
            self, self.filename, self.canvas, self.ax, self.mode)

    def createWidgets(self):
        # # sets the figure size and resolution
        # self.fig = Figure(figsize=(5, 4), dpi=100)
        # self.ax = self.fig.add_subplot(111)  # adds the single subplot

        # # canvas to embed the matplotlib
        # self.canvas = FigureCanvasTkAgg(self.fig, master=self)
        # self.canvas.draw()
        # self.canvas.get_tk_widget().pack(side='top', fill='both', expand=True)

        self.ui_canvas = tk.Canvas(
            self, bg="#FFFFFF", width=900, height=900, bd=0, highlightthickness=0)
        self.ui_canvas.pack()

        # # removes the ticks and labels from the plot
        # self.ax.set_xticks([])
        # self.ax.set_yticks([])
        # self.ax.set_xticklabels([])
        # self.ax.set_yticklabels([])

        # startBtn = Button(self, text='Start simulation',
        #                   command=self.startSimulation)
        # startBtn.pack(side='top')

        def relative_to_assets(path: str) -> Path:
            return ASSETS_PATH / Path(path)

        self.ui_canvas.create_text(
            130.0,
            59.0,
            anchor="nw",
            text="BIOFILM SIMULATOR",
            fill="#000000",
            font=("IstokWeb Bold", 64 * -1))

        self.uploadBtnImage = PhotoImage(
            file=relative_to_assets("upload.png"))

        uploadBtn = Button(
            image=self.uploadBtnImage,
            borderwidth=0,
            highlightthickness=0,
            command=self.uploadFile,
            relief="flat"
        )
        uploadBtn.place(
            x=130.0,
            y=230.0,
            width=286.0,
            height=63.0
        )

        self.playBtnImage = PhotoImage(
            file=relative_to_assets("play.png"))

        playBtn = Button(
            image=self.playBtnImage,
            borderwidth=0,
            highlightthickness=0,
            command=self.startSimulation,
            relief="flat"
        )
        playBtn.place(
            x=130.0,
            y=317.0,
            width=286.0,
            height=63.0
        )

        # nonMotileBtn = Button(self, text='Non-motile cells',
        #                       command=self.startNonMotile)
        # nonMotileBtn.pack(side='top')

        # MotileBtn = Button(self, text='Motile cells',
        #                    command=self.startMotile)
        # MotileBtn.pack(side='top')

        # PSLTrailBtn = Button(self, text='PSL trails',
        #                      command=self.startPSLTrail)
        # PSLTrailBtn.pack(side='top')

        # EPSMatrixBtn = Button(self, text='EPS mstrix',
        #                       command=self.startEPSMatrix)
        # EPSMatrixBtn.pack(side='top')


def main():
    # creates instance of the "application"
    app = BiofilmSimulationApp()
    app.mainloop()


if __name__ == '__main__':
    main()
