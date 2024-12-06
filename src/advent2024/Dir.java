package advent2024;

import advent2024.CharGrid.Coord;

/**
 * @author Éamonn McManus
 */
enum Dir {
  NW,
  N,
  NE,
  W,
  E,
  SW,
  S,
  SE;

  Coord move(Coord c, int amount) {
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

  Dir right90() {
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
