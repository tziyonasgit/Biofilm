import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import tkinter as tk

# Function to create and save the animation


def save_animation():
    # Number of dots and setup
    num_dots = 12
    radius = 2
    angles = np.linspace(0, 2 * np.pi, num_dots, endpoint=False)

    # Create figure and axis
    fig, ax = plt.subplots()
    ax.set_aspect('equal')
    ax.set_xlim(-10, 10)
    ax.set_ylim(-10, 10)

    # Initialize dots and line
    dots1, = ax.plot([], [], 'o', color='blue')
    line1, = ax.plot([], [], '-', color='blue')

    dots2, = ax.plot([], [], 'o', color='green')
    line2, = ax.plot([], [], '-', color='green')

    # Parameters for movement
    path_radius = 4  # Radius of the path the "cell" will move along
    path_speed = 0.05  # Speed at which the "cell" moves along the path

    # Fixed positions for the dots
    x_fixed = radius * np.cos(angles)
    y_fixed = radius * np.sin(angles)

    # Update function for animation
    def update(frame):
        # Calculate the center position of the first "cell" moving along a circular path
        center_x1 = path_radius * np.cos(path_speed * frame)
        center_y1 = path_radius * np.sin(path_speed * frame)

        # Calculate the separation between the two rings over time
        separation = min(frame / 10.0, 5)  # Limit the maximum separation

        # The second "cell" is offset by the separation distance
        center_x2 = center_x1 + separation
        center_y2 = center_y1

        # Translate the dots and lines for the first ring
        x_translated1 = x_fixed + center_x1
        y_translated1 = y_fixed + center_y1

        dots1.set_data(x_translated1, y_translated1)
        line1.set_data(np.append(x_translated1, x_translated1[0]), np.append(
            y_translated1, y_translated1[0]))

        # Translate the dots and lines for the second ring
        x_translated2 = x_fixed + center_x2
        y_translated2 = y_fixed + center_y2

        dots2.set_data(x_translated2, y_translated2)
        line2.set_data(np.append(x_translated2, x_translated2[0]), np.append(
            y_translated2, y_translated2[0]))

        return dots1, line1, dots2, line2

    # Create animation
    ani = FuncAnimation(fig, update, frames=200, interval=50, blit=True)

    # Save the animation as an MP4 file
    ani.save('animation.mp4', writer='ffmpeg')

    plt.close(fig)  # Close the figure to prevent it from displaying


# Create the main window
root = tk.Tk()
root.title("Animation GUI")

# Create the button to start the animation
save_button = tk.Button(root, text="Save Animation", command=save_animation)
save_button.pack()

# Start the Tkinter main loop
root.mainloop()
