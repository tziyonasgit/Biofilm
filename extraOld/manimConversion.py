from manim import *


class Bacterium(VMobject):
    def __init__(self, cellID, position=ORIGIN, length=2.0, width=1.0, color=GREEN, **kwargs):
        super().__init__(**kwargs)
        self.cellID = cellID
        self.body = Rectangle(width=width, height=length,
                              color=color).move_to(position)
        self.add(self.body)

    def move_bacterium(self, direction):
        move_vector = {
            "right": RIGHT,
            "left": LEFT,
            "up": UP,
            "down": DOWN
        }
        if direction in move_vector:
            self.body.shift(move_vector[direction])

    def split(self):
        return Bacterium(self.cellID + 1, position=self.body.get_center() + RIGHT * 2)


class BiofilmAnimation(Scene):
    def __init__(self, filename, **kwargs):
        super().__init__(**kwargs)
        self.filename = filename
        self.bacteria = {}
        self.currentLine = 0
        self.father = None  # Define father as an instance variable

    def spawn(self, cellID):
        if cellID == 1:
            position = ORIGIN
        else:
            position = self.bacteria[self.father].body.get_center() + RIGHT * 2
        bacterium = Bacterium(cellID, position=position)
        self.bacteria[cellID] = bacterium
        self.add(bacterium)

    def die(self, cellID):
        bacterium = self.bacteria.pop(cellID, None)
        if bacterium:
            self.remove(bacterium)

    def processLine(self):
        if self.currentLine < len(self.lines):
            line = self.lines[self.currentLine]
            self.currentLine += 1

            # Process the line
            line = line.strip()
            parts = line.split('#')

            if len(parts) >= 2:
                cellID = int(parts[1])
                action = parts[2]

            if action == "spawn":
                self.spawn(cellID)
            elif action == "move":
                if len(parts) > 3:
                    direction = parts[3]
                    self.bacteria[cellID].move_bacterium(direction)
            elif action == "split":
                self.father = cellID  # Update father before the split
                new_bacterium = self.bacteria[cellID].split()
                self.spawn(new_bacterium.cellID)
            elif action == "die":
                self.die(cellID)

            self.wait(0.5)  # Add some delay between actions
            self.processLine()

    def construct(self):
        with open(self.filename, 'r') as file:
            self.lines = file.readlines()

        self.processLine()

# Example of how to run the animation with a sample file


def startAnimation(filename):
    scene = BiofilmAnimation(filename)
    scene.render()


# Running the scene
if __name__ == "__main__":
    startAnimation("/Users/cccohen/Desktop/data 2.txt")
