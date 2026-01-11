#!/usr/bin/env python3
"""
Smart Inventory Recall - Icon Generator
Generates a 32x32 Minecraft-style pixel art icon
"""

from PIL import Image, ImageDraw

# Define colors (Minecraft-style palette)
COLORS = {
    'bg': (64, 64, 64),           # Dark gray background
    'chest_dark': (101, 67, 33),  # Dark brown (chest body)
    'chest_mid': (139, 90, 43),   # Medium brown (chest highlights)
    'chest_light': (168, 125, 82), # Light brown (chest edges)
    'lock': (180, 180, 180),      # Silver (lock)
    'lock_dark': (100, 100, 100), # Dark gray (lock shadow)
    'recall_glow': (100, 255, 100), # Green glow (recall effect)
    'recall_bright': (180, 255, 180), # Bright green
    'black': (0, 0, 0),           # Black outlines
}

def create_icon():
    # Create 32x32 image
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Background (transparent, so skip)

    # Draw chest (centered, slightly lower)
    # Chest main body
    chest_pixels = [
        # Chest outline
        (10, 12), (11, 12), (12, 12), (13, 12), (14, 12), (15, 12), (16, 12), (17, 12), (18, 12), (19, 12), (20, 12), (21, 12),
        (10, 13), (21, 13),
        (10, 14), (21, 14),
        (10, 15), (21, 15),
        (10, 16), (21, 16),
        (10, 17), (21, 17),
        (10, 18), (21, 18),
        (10, 19), (21, 19),
        (10, 20), (21, 20),
        (10, 21), (11, 21), (12, 21), (13, 21), (14, 21), (15, 21), (16, 21), (17, 21), (18, 21), (19, 21), (20, 21), (21, 21),
    ]

    # Fill chest body
    for y in range(13, 21):
        for x in range(11, 21):
            draw.point((x, y), fill=COLORS['chest_mid'])

    # Draw chest outline
    for x, y in chest_pixels:
        draw.point((x, y), fill=COLORS['black'])

    # Add chest texture details
    for y in range(14, 20):
        draw.point((11, y), fill=COLORS['chest_light'])
        draw.point((20, y), fill=COLORS['chest_dark'])

    # Draw lock
    lock_pixels = [
        (14, 16), (15, 16), (16, 16), (17, 16),
        (14, 17), (17, 17),
        (14, 18), (15, 18), (16, 18), (17, 18),
    ]
    for x, y in lock_pixels:
        draw.point((x, y), fill=COLORS['lock'])

    # Lock highlight
    draw.point((15, 17), fill=COLORS['lock_dark'])

    # Draw recall effect (circular particles around chest)
    # Upper left particles
    draw.point((8, 10), fill=COLORS['recall_glow'])
    draw.point((9, 10), fill=COLORS['recall_bright'])
    draw.point((7, 11), fill=COLORS['recall_bright'])

    # Upper right particles
    draw.point((22, 10), fill=COLORS['recall_glow'])
    draw.point((23, 10), fill=COLORS['recall_bright'])
    draw.point((24, 11), fill=COLORS['recall_bright'])

    # Left side particles
    draw.point((7, 15), fill=COLORS['recall_glow'])
    draw.point((8, 16), fill=COLORS['recall_bright'])
    draw.point((7, 17), fill=COLORS['recall_glow'])

    # Right side particles
    draw.point((24, 15), fill=COLORS['recall_glow'])
    draw.point((23, 16), fill=COLORS['recall_bright'])
    draw.point((24, 17), fill=COLORS['recall_glow'])

    # Lower particles
    draw.point((9, 23), fill=COLORS['recall_glow'])
    draw.point((10, 24), fill=COLORS['recall_bright'])
    draw.point((11, 23), fill=COLORS['recall_glow'])

    draw.point((20, 23), fill=COLORS['recall_glow'])
    draw.point((21, 24), fill=COLORS['recall_bright'])
    draw.point((22, 23), fill=COLORS['recall_glow'])

    # Add circular recall arrow (top)
    arrow_pixels = [
        # Arrow pointing down and curving
        (15, 8), (16, 8),
        (15, 9), (16, 9),
        (14, 9), (17, 9),  # Arrow tips
    ]
    for x, y in arrow_pixels:
        draw.point((x, y), fill=COLORS['recall_bright'])

    return img

def main():
    print("Generating Smart Inventory Recall icon...")

    # Create the icon
    icon = create_icon()

    # Save as PNG
    output_path = "icon.png"
    icon.save(output_path, "PNG")

    print(f"✓ Icon saved to: {output_path}")
    print(f"  Size: 32x32 pixels")
    print(f"  Format: PNG with transparency")
    print("\nNext steps:")
    print(f"  1. Move icon.png to: src/main/resources/assets/smartinventoryrecall/icon.png")
    print(f"  2. Rebuild your mod")

if __name__ == "__main__":
    main()