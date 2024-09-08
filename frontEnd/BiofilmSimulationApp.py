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
        self.geometry('700x400')
        self.configure(bg="#FFFFFF")
        self.centerWindow(self)
        self.filename = ""  # initialises selected filename
        self.createWidgets()  # creates the buttons the window
        self.mode = ""

    # https://www.geeksforgeeks.org/how-to-center-a-window-on-the-screen-in-tkinter/

    def centerWindow(self, window):
        window.update_idletasks()
        width = window.winfo_width()
        height = window.winfo_height()
        screen_width = window.winfo_screenwidth()
        screen_height = window.winfo_screenheight()
        x = (screen_width - width) // 2  # gets x coord
        y = (screen_height - height) // 2  # gets y coord
        # sets the window to the centre
        window.geometry(f"{width}x{height}+{x}+{y}")

    def uploadFile(self):
        if hasattr(self, 'text_id'):
            self.ui_canvas.delete(self.text_id)  # Remove the previous text

        self.filename = filedialog.askopenfilename()  # stores filename
        self.text_id = self.ui_canvas.create_text(
            155.0,
            130.0,
            anchor="nw",
            text="File seleted: " + self.filename,
            fill="#000000",
            font=("IstokWeb Bold", 15 * -1))

    def startSimulation(self):
        # Create a new window for the simulation
        simulation_window = tk.Toplevel(self)

        simulation_window.title("Simulation Window")
        simulation_window.geometry("800x600")
        self.centerWindow(simulation_window)
        simulation_window.attributes("-topmost", True)

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

    def createWidgets(self):

        self.ui_canvas = tk.Canvas(
            self, bg="#FFFFFF", width=900, height=900, bd=0, highlightthickness=0)
        self.ui_canvas.pack()

        def relative_to_assets(path: str) -> Path:
            return ASSETS_PATH / Path(path)

        self.ui_canvas.create_text(
            45.0,
            30.0,
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
            x=190.0,
            y=170.0,
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
            x=190.0,
            y=250.0,
            width=286.0,
            height=63.0
        )


def main():
    # creates instance of the "application"
    app = BiofilmSimulationApp()
    app.mainloop()


if __name__ == '__main__':
    main()
