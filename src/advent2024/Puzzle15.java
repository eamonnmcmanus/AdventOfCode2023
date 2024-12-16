package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static java.util.stream.Collectors.toMap;
import static advent2024.Puzzle15.Contents.BOX;
import static advent2024.Puzzle15.Contents.BOX_LEFT;
import static advent2024.Puzzle15.Contents.BOX_RIGHT;
import static advent2024.Puzzle15.Contents.EMPTY;
import static advent2024.Puzzle15.Contents.ROBOT;
import static advent2024.Puzzle15.Contents.WALL;
import static com.google.common.base.Preconditions.checkState;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;

/**
 * @author Éamonn McManus
 */
public class Puzzle15 {
  private static final String SMALL_SAMPLE =
      """
      ########
      #..O.O.#
      ##@.O..#
      #...O..#
      #.#.O..#
      #...O..#
      #......#
      ########

      <^^>>>vv<v>>v<<
      """;

  private static final String BIG_SAMPLE =
      """
      ##########
      #..O..O.O#
      #......O.#
      #.OO..O.O#
      #..O@..O.#
      #O#..O...#
      #O..O..O.#
      #.OO.O.OO#
      #....O...#
      ##########

      <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
      vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
      ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
      <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
      ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
      ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
      >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
      <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
      ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
      v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "small sample",
          () -> new StringReader(SMALL_SAMPLE),
          "big sample",
          () -> new StringReader(BIG_SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle15.class.getResourceAsStream("puzzle15.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int blank = lines.indexOf("");
        checkArgument(blank >= 0 && blank == lines.lastIndexOf(""));
        List<String> gridLines = lines.subList(0, blank);
        CharGrid grid = new CharGrid(gridLines);
        Map<Coord, Contents> gridMap = gridMap(grid);
        List<Dir> moves = parseMoves(lines.subList(blank + 1, lines.size()));

        System.out.printf("For Part 1 %s, sum is %d\n", name, part1Sum(gridMap, moves));

        CharGrid wideGrid = new CharGrid(widen(gridLines));
        Map<Coord, Contents> wideGridMap = gridMap(wideGrid);
        System.out.printf("For Part 2 %s, sum is %d\n", name, part2Sum(wideGridMap, moves));
        List<String> flippedGridLines = flipLinesLR(gridLines);
        List<Dir> flippedMoves = flipMovesLR(moves);
        CharGrid flippedWideGrid = new CharGrid(flippedWiden(flippedGridLines));
        Map<Coord, Contents> flippedWideGridMap = gridMap(flippedWideGrid);
        System.out.printf(
            "For Part 2 %s, flipped sum is %d\n", name, part2Sum(flippedWideGridMap, flippedMoves));
        show(wideGrid, wideGridMap);
        System.out.println(moves);
        System.out.println();
        show(flippedWideGrid, flippedWideGridMap);
        System.out.println(flippedMoves);
        for (Coord coord : wideGridMap.keySet()) {
          Coord flip = new Coord(coord.line(), flippedWideGrid.width() - 1 - coord.col());
          Contents orig = wideGridMap.get(coord);
          Contents flipped =
              switch (orig) {
                case BOX_LEFT -> BOX_RIGHT;
                case BOX_RIGHT -> BOX_LEFT;
                default -> orig;
              };
          if (flipped != flippedWideGridMap.get(flip)) {
            System.out.printf(
                "Disparity at %s (flipped to %s for width %d) orig %s flipped %s\n",
                coord,
                flip,
                flippedWideGrid.width(),
                wideGridMap.get(coord),
                flippedWideGridMap.get(flip));
          }
        }
      }
    }
  }

  private static List<String> flipLinesLR(List<String> lines) {
    return lines.stream().map(s -> new StringBuilder(s).reverse().toString()).toList();
  }

  private static List<Dir> flipMovesLR(List<Dir> moves) {
    return moves.stream()
        .map(
            move ->
                switch (move) {
                  case W -> Dir.E;
                  case E -> Dir.W;
                  default -> move;
                })
        .toList();
  }

  private static int part1Sum(Map<Coord, Contents> gridMap, List<Dir> moves) {
    Coord robot = robotCoord(gridMap);
    for (Dir move : moves) {
      Coord pos = move.move(robot);
      while (gridMap.get(pos) == BOX) {
        pos = move.move(pos);
      }
      switch (gridMap.get(pos)) {
        case WALL -> {}
        case EMPTY -> {
          // We're basically rotating from @OOO. to .@OOO, so we only need to adjust the ends.
          gridMap.put(pos, BOX);
          gridMap.put(robot, EMPTY);
          robot = move.move(robot);
          gridMap.put(robot, ROBOT);
        }
        default -> ise(gridMap.get(pos));
      }
    }
    return gpsSum(gridMap);
  }

  // E/W moves are basically the same as in Part 1, but N/S moves are much more complicated. If
  // moving north, we have to check both halves of each box in a stack. Each one must have either an
  // empty space or another box-half north of it. In the case where a box has two boxes north of it,
  // each of those must recursively meet the same conditions. Potentially we can have a very wide
  // row of boxes at the end.
  private static int part2Sum(Map<Coord, Contents> gridMap, List<Dir> moves) {
    Coord robot = robotCoord(gridMap);
    for (Dir move : moves) {
      robot = part2Move(gridMap, robot, move);
    }
    return gpsSum(gridMap);
  }

  static Coord part2Move(Map<Coord, Contents> gridMap, Coord robot, Dir move) {
    switch (move) {
      case E, W -> {
        Coord toPos = move.move(robot);
        for (Contents c; (c = gridMap.get(toPos)) == BOX_LEFT || c == BOX_RIGHT; ) {
          toPos = move.move(toPos);
        }
        switch (gridMap.get(toPos)) {
          case WALL -> {}
          case EMPTY -> {
            // Pull the boxes and the robot across, then make the old robot position empty.
            Coord fromPos = move.opposite().move(toPos);
            do {
              gridMap.put(toPos, gridMap.get(fromPos));
              toPos = fromPos;
              fromPos = move.opposite().move(fromPos);
            } while (!toPos.equals(robot));
            gridMap.put(robot, EMPTY);
            robot = move.move(robot);
          }
          default -> ise(gridMap.get(toPos));
        }
      }
      case N, S -> {
        Coord toPos = move.move(robot);
        boolean robotMove = false;
        switch (gridMap.get(toPos)) {
          case WALL -> {}
          case EMPTY -> robotMove = true;
          case BOX_LEFT -> robotMove = pushNorthSouth(gridMap, move, toPos, Dir.E.move(toPos));
          case BOX_RIGHT -> robotMove = pushNorthSouth(gridMap, move, Dir.W.move(toPos), toPos);
        }
        if (robotMove) {
          gridMap.put(toPos, ROBOT);
          gridMap.put(robot, EMPTY);
          robot = toPos;
        }
      }
    }
    return robot;
  }

  /**
   * Recursively pushes a horizontal row of boxes. The row can have gaps but there is a BOX_LEFT at
   * the west end and a BOX_RIGHT at the right end.
   *
   * @return true if this row and all following rows were successfully pushed.
   */
  private static boolean pushNorthSouth(
      Map<Coord, Contents> gridMap, Dir move, Coord westEnd, Coord eastEnd) {
    checkArgument(
        gridMap.get(westEnd) == BOX_LEFT, "Unexpected %s at %s", gridMap.get(westEnd), westEnd);
    checkArgument(gridMap.get(eastEnd) == BOX_RIGHT);
    int line = westEnd.line();

    // First check if we can move at all. Each box-half must be able to move into an empty space.
    boolean sawBoxes = false;
    for (int col = westEnd.col(); col <= eastEnd.col(); col++) {
      Coord here = new Coord(line, col);
      Coord next = move.move(here);
      switch (gridMap.get(here)) {
        case EMPTY -> {}
        case BOX_LEFT, BOX_RIGHT -> {
          switch (gridMap.get(next)) {
            case BOX_LEFT, BOX_RIGHT -> sawBoxes = true;
            case EMPTY -> {}
            case WALL -> {
              return false;
            }
            default -> ise(gridMap.get(next));
          }
        }
        default -> ise(gridMap.get(here));
      }
    }

    // If we haven't returned, then either there is empty space everywhere needed or there are boxes
    // in the next row. In the first case we can move the current row now, but in the second case we
    // need to try recursively moving the boxes above. The span to move may be wider or narrower
    // depending on what's above each end of the current row.
    if (sawBoxes) {
      Coord nextWestEnd = move.move(westEnd);
      switch (gridMap.get(nextWestEnd)) {
        case EMPTY -> {
          do {
            nextWestEnd = Dir.E.move(nextWestEnd);
          } while (gridMap.get(nextWestEnd) == EMPTY);
        }
        case BOX_LEFT -> {}
        case BOX_RIGHT -> nextWestEnd = Dir.W.move(nextWestEnd);
        default -> ise(gridMap.get(nextWestEnd));
      }
      Coord nextEastEnd = move.move(eastEnd);
      switch (gridMap.get(nextEastEnd)) {
        case EMPTY -> {
          do {
            nextEastEnd = Dir.W.move(nextEastEnd);
          } while (gridMap.get(nextEastEnd) == EMPTY);
        }
        case BOX_RIGHT -> {}
        case BOX_LEFT -> nextEastEnd = Dir.E.move(nextEastEnd);
        default -> ise(gridMap.get(nextEastEnd));
      }
      if (!pushNorthSouth(gridMap, move, nextWestEnd, nextEastEnd)) {
        return false;
      }
    }

    for (int col = westEnd.col(); col <= eastEnd.col(); col++) {
      Coord here = new Coord(line, col);
      Coord next = move.move(here);
      if (gridMap.get(next) != WALL) {
        // A gap between boxes can be pushed into a wall, which of course stays a wall.
        gridMap.put(next, gridMap.get(here));
      }
      gridMap.put(here, EMPTY);
    }
    return true;
  }

  private static Coord robotCoord(Map<Coord, Contents> gridMap) {
    return gridMap.entrySet().stream()
        .filter(e -> e.getValue() == ROBOT)
        .map(Map.Entry::getKey)
        .collect(onlyElement());
  }

  enum Contents {
    EMPTY,
    BOX,
    BOX_LEFT,
    BOX_RIGHT,
    WALL,
    ROBOT;

    static Contents fromChar(char c) {
      return switch (c) {
        case '.' -> EMPTY;
        case 'O' -> BOX;
        case '[' -> BOX_LEFT;
        case ']' -> BOX_RIGHT;
        case '#' -> WALL;
        case '@' -> ROBOT;
        default -> throw new IllegalArgumentException("Bad char " + c);
      };
    }

    char toChar() {
      return switch (this) {
        case EMPTY -> '.';
        case BOX -> 'O';
        case BOX_LEFT -> '[';
        case BOX_RIGHT -> ']';
        case WALL -> '#';
        case ROBOT -> '@';
      };
    }
  }

  private static Map<Coord, Contents> gridMap(CharGrid grid) {
    return grid.coordStream()
        .collect(
            toMap(c -> c, c -> Contents.fromChar(grid.get(c)), binaryThrow(), LinkedHashMap::new));
  }

  private static <U> BinaryOperator<U> binaryThrow() {
    return (a, b) -> {
      throw new IllegalStateException("Duplicate value");
    };
  }

  private static List<Dir> parseMoves(List<String> list) {
    return String.join("", list).chars().mapToObj(Dir::fromChar).collect(toImmutableList());
  }

  private static int gps(Coord coord) {
    return 100 * coord.line() + coord.col();
  }

  private static int gpsSum(Map<Coord, Contents> gridMap) {
    var box = EnumSet.of(BOX, BOX_LEFT);
    return gridMap.entrySet().stream()
        .filter(e -> box.contains(e.getValue()))
        .map(e -> gps(e.getKey()))
        .reduce(0, Math::addExact);
  }

  private static List<String> widen(List<String> lines) {
    return lines.stream().map(Puzzle15::widen).toList();
  }

  private static String widen(String line) {
    return line.replace("#", "##").replace("O", "[]").replace(".", "..").replace("@", "@.");
  }

  private static List<String> flippedWiden(List<String> lines) {
    return lines.stream().map(Puzzle15::flippedWiden).toList();
  }

  private static String flippedWiden(String line) {
    return line.replace("#", "##").replace("O", "[]").replace(".", "..").replace("@", ".@");
  }

  private static void show(CharGrid grid, Map<Coord, Contents> gridMap) {
    for (var coord : grid.coords()) {
      System.out.print(gridMap.get(coord).toChar());
      if (coord.col() + 1 == grid.width()) {
        System.out.println();
      }
    }
  }

  private static void ise(Object x) {
    throw new IllegalStateException(String.valueOf(x));
  }
}