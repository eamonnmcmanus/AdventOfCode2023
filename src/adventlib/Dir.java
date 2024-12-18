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

  public static Dir fromChar(char c) {
    return switch (c) {
      case '<' -> W;
      case '>' -> E;
      case '^' -> N;
      case 'v' -> S;
      default ->
          throw new IllegalArgumentException(
              String.format("Unknown direction char %c (U+%04x)", c, (int) c));
    };
  }

  public static Dir fromChar(int c) {
    char cc = (char) c;
    if (cc != c) {
      throw new IllegalArgumentException(String.format("Unknown direction char %c (U+%04x)", c, c));
    }
    return fromChar(cc);
  }

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

  public Coord move(Coord c) {
    return move(c, 1);
  }

  public Dir opposite() {
    return switch (this) {
      case NW -> SE;
      case N -> S;
      case NE -> SW;
      case E -> W;
      case SE -> NW;
      case S -> N;
      case SW -> NE;
      case W -> E;
    };
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

  public Dir left90() {
    return switch (this) {
      case NW -> SW;
      case N -> W;
      case NE -> NW;
      case E -> N;
      case SE -> NE;
      case S -> E;
      case SW -> SE;
      case W -> S;
    };
  }
}
