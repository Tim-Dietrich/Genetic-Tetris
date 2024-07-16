package org.tetris.simple.Utils;

import java.awt.*;

public enum TetrisColors {
    // General application colors
    COLOR_1(new Color(13, 27, 42)),
    COLOR_2(new Color(27, 38, 59)),
    COLOR_3(new Color(65, 90, 119)),
    COLOR_4(new Color(119, 141, 169)),
    COLOR_5(new Color(224, 225, 221)),

    // Block colors
    // TODO: find better color variations, palette:
    //  https://coolors.co/00cdcd-cdcd00-9a00cd-0000cd-cd6600-00cd00-dc0105
    BLOCK_I(new Color(0, 205, 205)),
    BLOCK_O(new Color(205, 205, 0)),
    BLOCK_T(new Color(154, 0, 205)),
    BLOCK_J(new Color(0, 0, 205)),
    BLOCK_L(new Color(205, 102, 0)),
    BLOCK_S(new Color(0, 205, 0)),
    BLOCK_Z(new Color(220, 1, 5));

    public final Color color;

    TetrisColors(Color color) {
        this.color = color;
    }
}
