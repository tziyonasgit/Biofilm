from manim import *


class BacilliShapeExample(ThreeDScene):
    def construct(self):
        self.set_camera_orientation(phi=75*DEGREES, theta=100*DEGREES)
        rad = 0.5
        mid = 1

        def func(u, v):
            if 0 <= u < rad:
                r = np.sqrt(2*rad*u - u**2)
            elif rad <= u < rad+mid:
                r = rad
            elif rad+mid <= u < 2*rad+mid:
                r = np.sqrt(rad**2 - (u-rad-mid)**2)
            else:
                r = 0
            return np.array([r*np.cos(v), r*np.sin(v), u])
        bac = Surface(
            func,
            u_range=[0, 2*rad + mid],
            v_range=[0, 2*PI],
            resolution=[30, 32],
            should_make_jagged=True,
        )
        self.play(Rotate(bac, angle=2*PI, rate_func=linear, run_time=4))

# To render the scene, you would use:
# manim -pql test.py BacilliShapeExample
