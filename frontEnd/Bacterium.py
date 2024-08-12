import numpy as np


class Bacterium:

    def __init__(self, id: int, age: int, strain: str = None, position:  np.ndarray = None):
        self.id = id
        self.age = 0
        self.strain = "E.coli"
        # initial position
        if position is None:
            self.position = np.array([0, 0])
        else:
            self.position = position

    def getPosition(self):
        return self.position
