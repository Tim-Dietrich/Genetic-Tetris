package org.tetris.simple.Utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tuple<X, Y> {
    public X x;
    public Y y;
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
    public Tuple(Tuple<X, Y> tuple) {
        this.x = tuple.x;
        this.y = tuple.y;
    }

    @Override
    public String toString() {
        return "Tuple [X=" + x + ", Rot=" + y + "]";
    }
}
