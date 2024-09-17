from datetime import datetime
import threading
import tkinter as tk  # imports tkinter library for GUI
from tkinter import *
from tkinter import filedialog
from tkinter import messagebox  # for uploading file
import SaveAnimation as SaveAnimation
# embeds matplotlib in tkinter GUI
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
import Animation
from pathlib import Path
from tkinter import Tk, Canvas, Entry, Text, Button, PhotoImage
import subprocess
import os
from PIL import Image, ImageTk


OUTPUT_PATH = Path(__file__).parent
ASSETS_PATH = OUTPUT_PATH / "assets"


class BiofilmSimulationApp(tk.Tk):
    def __init__(self):  # constructor
        super().__init__()
        # setups main window
        self.title("")
        self.geometry('700x450')
        self.configure(bg="#FFFFFF")
        self.centerWindow(self)
        self.default_values = {
            "bmonomers": "20",
            "eps": "25",
            "nutrients": "30",
            "bacteria": "3",
            "width": "100",
            "height": "100",
            "duration": "20"
        }

        self.filename = ""  # initialises selected filename
        self.createWidgets()  # creates the buttons the window
        self.mode = ""
        self.inputs = {}
        self.warningLabels = {}

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
        if hasattr(self, 'textID'):
            self.UICanvas.delete(self.textID)  # Remove the previous text

        self.filename = filedialog.askopenfilename(
            filetypes=[("Text files", "*.txt")],  # Only show .txt files
            title="Select a .txt file"  # Optional: Set the title of the dialog
        )
        self.textID = self.UICanvas.create_text(
            155.0,
            130.0,
            anchor="nw",
            text="File seleted: " + self.filename,
            fill="#000000",
            font=("IstokWeb Bold", 15 * -1))

        self.update_idletasks()
        canvasWidth = self.UICanvas.winfo_width()

        # Get the bounding box of the text to calculate its width
        bbox = self.UICanvas.bbox(self.textID)
        textWidth = bbox[2] - bbox[0]  # Calculate the width of the text

        # Calculate the x-coordinate to center the text
        xText = (canvasWidth - textWidth) / 2

        # Move the text to the center horizontally
        self.UICanvas.coords(self.textID, xText, 130.0)

    def relativeToAssets(self, path: str) -> Path:
        return ASSETS_PATH / Path(path)

    def collectData(self, inputWindow):
        self.clearWarnings()
        if not self.validateInputs():
            return
        self.clearWarnings()
        # Collect the user inputs from Entry fields and store them
        self.inputs["free_bacterial_monomers"] = self.bMonomers.get()
        self.inputs["free_eps_monomers"] = self.EPSMonomers.get()
        self.inputs["nutrients"] = self.nutrients.get()
        self.inputs["bacteria"] = self.bacteria.get()
        self.inputs["simulation_width"] = self.widthInput.get()
        self.inputs["simulation_height"] = self.heightInput.get()
        self.inputs["simulation_duration"] = self.duration.get()

        self.clearWarnings()
        inputWindow.destroy()
        self.loadingScreen()

    def loadingScreen(self):
        loadingWindow = tk.Toplevel()
        loadingWindow.geometry('700x450')
        loadingWindow.configure(bg="#FFFFFF")
        self.centerWindow(loadingWindow)
        loadingWindow.title("Please be patient")

        label = tk.Label(
            loadingWindow, text="Generating file...", font=("Arial", 24), bg="white")
        label.pack(pady=20)

        # Replace with your GIF path
        self.loading_gif = Image.open(self.relativeToAssets("loading.gif"))
        self.frames = []
        try:
            for i in range(self.loading_gif.n_frames):
                self.loading_gif.seek(i)
                # Ensure each frame is in the correct mode
                frame = self.loading_gif.copy().convert("RGBA")
                self.frames.append(ImageTk.PhotoImage(frame))
        except EOFError:
            pass  # End of sequence

        gif_label = tk.Label(loadingWindow)
        gif_label.pack(pady=20)

        # Function to animate the GIF
        def update_gif(frame_idx):
            gif_label.config(image=self.frames[frame_idx])
            frame_idx = (frame_idx + 1) % len(self.frames)
            # Adjust speed of GIF if necessary
            loadingWindow.after(100, update_gif, frame_idx)

        update_gif(0)  # Start GIF animation

        # Disable resizing of the loading window
        loadingWindow.resizable(False, False)
        thread = threading.Thread(
            target=self.runMakefile, args=(loadingWindow,))
        thread.start()

    def clearWarnings(self):
        for entry in self.warningLabels.keys():
            entry.config(bg="white")
            self.warningLabels[entry].config(text="")

    def validateInputs(self):
        self.clearWarnings()
        inputError = False
        # Check if all inputs are valid
        try:
            # Validate that these are integers
            self.checkInt(self.bMonomers, "Free Bacterial Monomers")
            self.checkInt(self.EPSMonomers, "EPS Monomers")
            self.checkInt(self.nutrients, "Nutrients")
            self.checkInt(self.bacteria, "Bacteria")
            self.checkInt(self.widthInput, "Simulation Width")
            self.checkInt(self.heightInput, "Simulation Height")
            self.checkInt(self.duration, "Simulation Duration")

            # Check blocks within valid range
            self.checkBlocks(self.widthInput, self.heightInput)

            # Ensure the number of bacteria fits within the environment
            self.checkiBacteria(
                self.bacteria, self.widthInput, self.heightInput)

            # Validate simulation duration as an integer
            self.checkInt(self.duration, "Simulation Duration")

        except ValueError:
            inputError = True

        return not inputError

    def checkInt(self, entry, fieldName):
        value = entry.get()
        try:
            int(value)
        except ValueError:
            # Show warning if not integer
            self.issueWarning(entry, f"{fieldName} must be an integer.")
            raise ValueError  # Raise an error to trigger validation failure

    def checkBlocks(self, widthEntry, heightEntry):
        width = int(widthEntry.get())
        height = int(heightEntry.get())

        # Check if width and height are within the valid range
        if width < 100 or width > 2500:
            self.issueWarning(
                widthEntry, "Width must be between 100 and 2500 blocks.")
            raise ValueError("Invalid width")
        if height < 100 or height > 2500:
            self.issueWarning(
                heightEntry, "Height must be between 100 and 2500 blocks.")
            raise ValueError("Invalid height")

    def checkiBacteria(self, bacteriaEntry, width, height):
        width = int(width.get())
        height = int(height.get())
        bacteria = int(bacteriaEntry.get())

        totalBlocks = width * height

        # Validate that the number of bacteria is less than total blocks
        if bacteria >= totalBlocks:
            self.issueWarning(
                bacteriaEntry, "Too many bacteria for the available space.")
            raise ValueError("Invalid number of bacteria")

    def issueWarning(self, entry, message):
        entry.config(bg="#FFCCCC")  # Highlight the entry box in red
        self.warningLabels[entry].config(
            text=message)  # Show the error message

    def openInputWindow(self):
        # Create a new top-level window
        inputWindow = Toplevel(self)
        inputWindow.title("Enter simulation parameters")
        inputWindow.geometry('750x550')
        inputWindow.configure(bg="#FFFFFF")
        self.centerWindow(inputWindow)

        submitBtn = Button(inputWindow, text="Submit",
                           command=lambda: self.collectData(inputWindow), bg="white", fg="black", bd=0)

        # Place the button at the center of the window
        submitBtn.place(relx=0.5, rely=0.95, anchor="center")

        self.warningLabels.clear()

        inputFrame = Frame(inputWindow, bg="white")
        inputFrame.grid(row=0, column=0, padx=100, pady=20)

        # Configure column 0 (labels) to stretch
        inputWindow.columnconfigure(0, weight=1)
        inputWindow.columnconfigure(1, weight=2)

        # Label and Entry for free bacterial monomers
        self.bMonomerLabel = Label(
            inputFrame, text="Enter starting number of free bacterial monomers:", bg="white")
        self.bMonomers = Entry(
            inputFrame, highlightbackground="white", highlightcolor="white", highlightthickness=1)
        self.bMonomers.insert(
            0, self.default_values["bmonomers"])
        self.warningLabels[self.bMonomers] = Label(
            inputFrame, text="", fg="red", bg="white")

        # Align labels and entries using sticky
        self.bMonomerLabel.grid(row=0, column=0, pady=(0, 10), sticky="w")
        self.bMonomers.grid(row=0, column=1, pady=(0, 0), sticky="ew")
        self.warningLabels[self.bMonomers].grid(
            row=1, column=1, sticky="n",  padx=0, pady=(0, 10))

        # Label and Entry for EPS monomers
        self.EPSMonomerLabel = Label(
            inputFrame, text="Enter starting number of free EPS monomers:", bg="white")
        self.EPSMonomers = Entry(
            inputFrame, highlightbackground="white", highlightcolor="white", highlightthickness=1)
        self.EPSMonomers.insert(
            0, self.default_values["eps"])
        self.warningLabels[self.EPSMonomers] = Label(
            inputFrame, text="", fg="red", bg="white")

        self.EPSMonomerLabel.grid(row=2, column=0, pady=(0, 10), sticky="w")
        self.EPSMonomers.grid(row=2, column=1, pady=(0, 10), sticky="ew")
        self.warningLabels[self.EPSMonomers].grid(
            row=3, column=1, sticky="n",  padx=0, pady=(0, 10))

        # Label and Entry for nutrients
        self.nutrientsLabel = Label(
            inputFrame, text="Enter starting number of nutrients:", bg="white")
        self.nutrients = Entry(inputFrame, highlightbackground="white",
                               highlightcolor="white", highlightthickness=1)
        self.nutrients.insert(
            0, self.default_values["nutrients"])
        self.warningLabels[self.nutrients] = Label(
            inputFrame, text="", fg="red", bg="white")

        self.nutrientsLabel.grid(row=4, column=0, pady=(0, 10), sticky="w")
        self.nutrients.grid(row=4, column=1, pady=(0, 10), sticky="ew")
        self.warningLabels[self.nutrients].grid(
            row=5, column=1, sticky="n",  padx=0, pady=(0, 10))

        # Label and Entry for bacteria
        self.bacteriaLabel = Label(
            inputFrame, text="Enter starting number of bacteria:", bg="white")
        self.bacteria = Entry(inputFrame, highlightbackground="white",
                              highlightcolor="white", highlightthickness=1)
        self.bacteria.insert(
            0, self.default_values["bacteria"])
        self.warningLabels[self.bacteria] = Label(
            inputFrame, text="", fg="red", bg="white")

        self.bacteriaLabel.grid(row=6, column=0, pady=(0, 10), sticky="w")
        self.bacteria.grid(row=6, column=1, pady=(0, 10), sticky="ew")
        self.warningLabels[self.bacteria].grid(
            row=7, column=1, sticky="n",  padx=0, pady=(0, 10))

        # Label and Entry for simulation width
        self.widthLabel = Label(
            inputFrame, text="Enter simulation width (number of blocks wide):", bg="white")
        self.widthInput = Entry(inputFrame, highlightbackground="white",
                                highlightcolor="white", highlightthickness=1)
        self.widthInput.insert(
            0, self.default_values["width"])
        self.warningLabels[self.widthInput] = Label(
            inputFrame, text="", fg="red", bg="white")

        self.widthLabel.grid(row=8, column=0, pady=(0, 10), sticky="w")
        self.widthInput.grid(row=8, column=1, pady=(0, 10), sticky="ew")
        self.warningLabels[self.widthInput].grid(
            row=9, column=1, sticky="n",  padx=0, pady=(0, 10))

        # Label and Entry for simulation height
        self.heightLabel = Label(
            inputFrame, text="Enter simulation height (number of blocks high):", bg="white")
        self.heightInput = Entry(
            inputFrame, highlightbackground="white", highlightcolor="white", highlightthickness=1)
        self.heightInput.insert(
            0, self.default_values["height"])
        self.warningLabels[self.heightInput] = Label(
            inputFrame, text="", fg="red", bg="white")

        self.heightLabel.grid(row=10, column=0, pady=(0, 10), sticky="w")
        self.heightInput.grid(row=10, column=1, pady=(0, 10), sticky="ew")
        self.warningLabels[self.heightInput].grid(
            row=11, column=1, sticky="n",  padx=0, pady=(0, 10))

        # Label and Entry for simulation duration
        self.durationLabel = Label(
            inputFrame, text="Enter simulation duration (seconds):", bg="white")
        self.duration = Entry(inputFrame, highlightbackground="white",
                              highlightcolor="white", highlightthickness=1)
        self.duration.insert(
            0, self.default_values["duration"])
        self.warningLabels[self.duration] = Label(
            inputFrame, text="", fg="red", bg="white")

        self.durationLabel.grid(row=12, column=0, pady=(0, 10), sticky="w")
        self.duration.grid(row=12, column=1, pady=(0, 10), sticky="ew")
        self.warningLabels[self.duration].grid(
            row=13, column=1, sticky="n",  padx=0, pady=(0, 10))

    def runMakefile(self, loadingWindow):
        bacterial_monomers = self.inputs["free_bacterial_monomers"]
        eps_monomers = self.inputs["free_eps_monomers"]
        nutrients = self.inputs["nutrients"]
        bacteria = self.inputs["bacteria"]
        width = self.inputs["simulation_width"]
        height = self.inputs["simulation_height"]
        duration = self.inputs["simulation_duration"]
        fileName = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

        makefilePath = Path(*list(Path(__file__).parent.parents)[-4::-1][:3])
        print(makefilePath)

        try:
            # Compile the Java program using Makefile
            compileCommand = f"make -C {makefilePath} all"
            subprocess.run(compileCommand, shell=True, check=True)

            # Prepare the ARGS string
            args = f"{bacterial_monomers} {eps_monomers} {
                nutrients} {bacteria} {width} {height} {duration} {fileName}"

            # Run the Java program using Makefile and pass the arguments
            runCommand = f"make -C {makefilePath} run ARGS='{args}'"
            subprocess.run(runCommand, shell=True, check=True)
            print(runCommand)
            subprocess.run(runCommand, shell=True, check=True)
        except subprocess.CalledProcessError as e:
            messagebox.showerror("Error", f"Error occurred: {e}")

        loadingWindow.after(0, loadingWindow.destroy)

    def startSimulation(self):

        if not self.filename:
            # Display a warning message if no file is selected
            messagebox.showwarning(
                "No File Selected", "Please upload a file before starting the simulation.")
            return
        # Create a new window for the simulation
        simulationWindow = tk.Toplevel(self)

        simulationWindow.title("Simulation Window")
        simulationWindow.geometry("1000x600")
        self.centerWindow(simulationWindow)
        simulationWindow.attributes("-topmost", True)

        # Create the matplotlib figure and canvas in the new window
        fig = Figure(figsize=(5, 4), dpi=100)
        ax = fig.add_subplot(111)  # Add the single subplot

        canvas = FigureCanvasTkAgg(fig, master=simulationWindow)
        canvas.draw()
        canvas.get_tk_widget().pack(side='top', fill='both', expand=True)
        self.mode = "PSL"
        ax.clear()  # clears any existing plot
        Animation.startAnimation(
            self, self.filename, canvas, ax, self.mode)
        SaveAnimation.startAnimation(self.filename, self.mode)

    def createWidgets(self):
        self.UICanvas = tk.Canvas(
            self, bg="#FFFFFF", width=1000, height=1000, bd=0, highlightthickness=0)
        self.UICanvas.pack()

        textID = self.UICanvas.create_text(
            45.0,
            30.0,
            anchor="nw",
            text="BIOFILM SIMULATOR",
            fill="#000000",
            font=("IstokWeb Bold", 64 * -1))

        self.update_idletasks()

        canvasWidth = self.UICanvas.winfo_width()
        canvasHeight = self.UICanvas.winfo_height()

        bbox = self.UICanvas.bbox(textID)
        textWidth = bbox[2] - bbox[0]
        textHeight = bbox[3] - bbox[1]
        x = (canvasWidth - textWidth) / 2
        y = (canvasHeight - textHeight) / 2 - 140

        self.UICanvas.coords(textID, x, y)

        self.uploadBtnImage = PhotoImage(
            file=self.relativeToAssets("upload.png"))

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
            file=self.relativeToAssets("play.png"))

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

        self.generateBtnImage = PhotoImage(
            # Add image for Java compile button
            file=self.relativeToAssets("generate.png"))

        generateBtn = Button(
            image=self.generateBtnImage,
            borderwidth=0,
            highlightthickness=0,
            command=self.openInputWindow,
            relief="flat"
        )
        generateBtn.place(
            x=190.0,
            y=330.0,  # Adjust the Y position
            width=286.0,
            height=63.0
        )


def main():
    # creates instance of the "application"
    app = BiofilmSimulationApp()
    app.mainloop()


if __name__ == '__main__':
    main()
