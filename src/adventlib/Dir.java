package adventlib;

import adventlib.CharGrid.Coord;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Éamonn McManus
 */
public enum Dir {
  NW,
  N,
  NE,
  W,
  E,
  SW,
  S,
  SE;

  public static Set<Dir> NEWS = EnumSet.of(N, E, W, S);

  public Coord move(Coord c, int amount) {
    int lineDelta =
        switch (this) {
          case NW, N, NE -> -amount;
          case SW, S, SE -> +amount;
          case W, E -> 0;
        };
    int colDelta =
        switch (this) {
          case NW, W, SW -> -amount;
          case NE, E, SE -> +amount;
          case N, S -> 0;
        };
    return new Coord(c.line() + lineDelta, c.col() + colDelta);
  }

  public Dir right90() {
    return switch (this) {
      case NW -> NE;
      case N -> E;
      case NE -> SE;
      case E -> S;
      case SE -> SW;
      case S -> W;
      case SW -> NW;
      case W -> N;
    };
  }
}
