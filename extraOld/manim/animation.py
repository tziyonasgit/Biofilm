from manim import *
import os


class CellSimulation(Scene):
    def construct(self):
        # Get file path from environment variable
        file_path = os.getenv('INPUT_FILE')
        if file_path is None:
            raise ValueError("No file path provided in environment variables.")

        # Read the commands from the file
        commands = []
        if not os.path.isfile(file_path):
            raise FileNotFoundError(f"The file {file_path} does not exist.")

        with open(file_path, 'r') as file:
            commands = file.readlines()

        # Dictionary to keep track of cells
        cells = {}

        for command in commands:
            command = command.strip()
            if not command:
                continue

            parts = command.split('#')
            cell_id, action = parts[1], parts[2]

            # Create or get cell
            if cell_id not in cells:
                cells[cell_id] = self.create_cell(cell_id)

            cell = cells[cell_id]

            # Perform action
            if action == 'spawn':
                self.play(Create(cell))
            elif action == 'move':
                self.play(cell.animate.shift(RIGHT * 2))
            elif action == 'split':
                new_cell = self.create_cell(cell_id + '_split')
                self.play(Transform(cell, new_cell))
                cells[cell_id + '_split'] = new_cell
            elif action == 'die':
                self.play(FadeOut(cell))
                del cells[cell_id]

    def create_cell(self, cell_id):
        """ Create a cell with a unique label and color """
        return Circle(radius=0.5).set_fill(PINK, opacity=0.5).shift(UP * 2)
