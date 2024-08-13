from manim import *


class MovingRod3D(ThreeDScene):

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
        rod = Surface(
            func,
            u_range=[0, 2*rad + mid],
            v_range=[0, 2*PI],
            resolution=[30, 32],
            should_make_jagged=True,
        )

    # Animate the rod moving in a 3D space
        self.play(rod.animate.shift(RIGHT * 4))
        self.play(rod.animate.shift(UP * 2))
        self.play(rod.animate.shift(OUT * 2))
        self.play(rod.animate.rotate(PI / 2, axis=RIGHT))
        self.play(rod.animate.shift(LEFT * 4))

    # Rotate the camera around the scene
        self.move_camera(phi=45 * DEGREES, theta=90 * DEGREES, run_time=3)
        self.wait()

# To render the animation, run this in your terminal:
# manim -pql 3dpy.py MovingRod3D
