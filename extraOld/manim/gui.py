import tkinter as tk
from tkinter import filedialog, messagebox
import subprocess
import os


def run_animation(file_path):
    try:
        # Set the environment variable for file path
        os.environ['INPUT_FILE'] = file_path

        # Run the Manim script
        result = subprocess.run(
            ["manim", "-pql", "animation.py", "CellSimulation"],
            capture_output=True, text=True
        )

    except Exception as e:
        messagebox.showerror("Error", str(e))


def open_file():
    file_path = filedialog.askopenfilename(
        filetypes=[("Text Files", "*.txt"), ("All Files", "*.*")]
    )
    if file_path:
        run_animation(file_path)


# Set up the Tkinter GUI
root = tk.Tk()
root.title("Manim GUI")

open_button = tk.Button(
    root, text="Select File and Run Animation", command=open_file)
open_button.pack(pady=20)

root.mainloop()
