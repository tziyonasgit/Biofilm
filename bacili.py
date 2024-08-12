import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.animation import FuncAnimation


def draw_bacillus(ax, center=(0, 0), length=4, width=1, color='green'):
    # Calculate positions
    radius = width / 2
    body_center_x, body_center_y = center

    # Create the central rod (rectangle)
    body = patches.Rectangle((body_center_x - length / 2, body_center_y - width / 2),
                             length, width, color=color)

    # Create the rounded ends (circles)
    left_end = patches.Circle(
        (body_center_x - length / 2, body_center_y), radius, color=color)
    right_end = patches.Circle(
        (body_center_x + length / 2, body_center_y), radius, color=color)

    # Add shapes to the plot
    ax.add_patch(body)
    ax.add_patch(left_end)
    ax.add_patch(right_end)

    return body, left_end, right_end


# Initialize the figure and axis
fig, ax = plt.subplots()

# Initial position of the bacillus
bacillus_length = 4
bacillus_width = 1
initial_center = (-5, 0)

# Draw the bacillus
body, left_end, right_end = draw_bacillus(
    ax, center=initial_center, length=bacillus_length, width=bacillus_width)

# Set plot limits and aspect ratio
ax.set_xlim(-10, 10)
ax.set_ylim(-5, 5)
ax.set_aspect('equal')
ax.axis('off')

# Function to update the position of the bacillus


def update(frame):
    dx = 0.1  # Move right by 0.1 units per frame

    # Move the body
    body.set_x(body.get_x() + dx)

    # Move the rounded ends
    left_end.set_center((left_end.center[0] + dx, left_end.center[1]))
    right_end.set_center((right_end.center[0] + dx, right_end.center[1]))

    return body, left_end, right_end


# Create the animation
anim = FuncAnimation(fig, update, frames=200, interval=50, blit=True)

# Display the animation
plt.show()
