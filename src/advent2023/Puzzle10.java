package advent2023;

import static advent2023.Puzzle10.Direction.DOWN;
import static advent2023.Puzzle10.Direction.LEFT;
import static advent2023.Puzzle10.Direction.RIGHT;
import static advent2023.Puzzle10.Direction.UP;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle10 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle10.class.getResourceAsStream("puzzle10.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<List<EnumSet<Direction>>> grid = new ArrayList<>();

      // Parse the map into a 2D representation where each cell is either null or says which way
      // the pipe goes.
      int sourceI = -1;
      int sourceJ = -1;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        List<EnumSet<Direction>> dirs = new ArrayList<>();
        for (int j = 0; j < line.length(); j++) {
          char c = line.charAt(j);
          if (c == 'S') {
            assert sourceI < 0 && sourceJ < 0;
            sourceI = i;
            sourceJ = j;
          }
          EnumSet<Direction> cellDirs = CHAR_TO_DIR.get(c);
          if (cellDirs == null) {
            cellDirs = EnumSet.noneOf(Direction.class);
          }
          dirs.add(cellDirs); // may be null
        }
        grid.add(dirs);
      }

      // Figure out which kind of corner the source (S) cell should be.
      assert sourceI >= 0 && sourceJ >= 0;
      EnumSet<Direction> sourceDirs = EnumSet.noneOf(Direction.class);
      if (sourceI > 0 && grid.get(sourceI - 1).get(sourceJ).contains(DOWN)) {
        sourceDirs.add(UP);
      }
      if (sourceI + 1 < lines.size() && grid.get(sourceI + 1).get(sourceJ).contains(UP)) {
        sourceDirs.add(DOWN);
      }
      if (sourceJ > 0 && grid.get(sourceI).get(sourceJ - 1).contains(RIGHT)) {
        sourceDirs.add(LEFT);
      }
      if (sourceJ + 1 < grid.get(sourceI).size()
          && grid.get(sourceI).get(sourceJ + 1).contains(LEFT)) {
        sourceDirs.add(RIGHT);
      }
      Direction dir = sourceDirs.iterator().next();

      // We make an equivalent representation using Unicode box-drawing characters. This isn't
      // necessary, but makes reading and debugging easier.
      char[][] pipe = new char[lines.size()][lines.getFirst().length()];
      for (char[] line : pipe) {
        Arrays.fill(line, ' ');
      }
      pipe[sourceI][sourceJ] = BOX_DRAWING.get(CHAR_TO_DIR.inverse().get(sourceDirs));
      System.out.println("Source (" + sourceI + ", " + sourceJ + "), direction " + dir);

      // Solution for Part 1, and also fill in the pipe[][] array.
      int steps = 0;
      for (int i = sourceI, j = sourceJ; ; ) {
        steps++;
        // dir tells us which way to go now
        switch (dir) {
          case LEFT -> j--;
          case RIGHT -> j++;
          case UP -> i--;
          case DOWN -> i++;
        }
        if (i == sourceI && j == sourceJ) {
          break;
        }
        EnumSet<Direction> dirs = grid.get(i).get(j);
        pipe[i][j] = BOX_DRAWING.get(CHAR_TO_DIR.inverse().get(dirs));
        // Now if we entered from the left, we were going right, so this is either {RIGHT, UP} or
        // {RIGHT, DOWN}. The new direction is then UP or DOWN accordingly.
        dir = otherDir(dirs, dir.opposite());
      }

      System.out.println("Total steps " + steps + ", halfway " + steps / 2);

      int insideCount = 0;
      enum Corner {
        NONE,
        FROM_ABOVE,
        FROM_BELOW
      }

      // We're inside the loop if we have crossed an odd number of lines starting from the left
      // edge.
      // It's a bit more complicated, though, because we might encounter e.g. ┏━┛. That's
      // essentially
      // the same as encountering ┃, so if we were outside we are now inside, and vice versa.
      // On the other hand ┏━┓ has no effect on "insideness". So we remember a "corner" state that
      // tells us which starting corner (┏ or ┓) we encountered before. Then when we encounter the
      // corresponding ending corner (┛ or ┓), we switch insideness if the ending corner has the
      // opposite vertical direction from the starting one.
      // A simpler idea that occurred to me later would be to scan diagonally rather than
      // horizontally. Then we can just ignore corners as we track insideness. The same is true
      // of Puzzle 18. In both cases, there's an even simpler alternative, pointed out by @lowasser:
      // https://en.wikipedia.org/wiki/Shoelace_formula
      for (char[] line : pipe) {
        boolean inside = false;
        Corner corner = Corner.NONE;
        for (int j = 0; j < line.length; j++) {
          switch (line[j]) {
            case ' ' -> {
              if (inside) {
                line[j] = '*';
                insideCount++;
              }
            }
            case '━' -> {}
            case '┃' -> inside = !inside;
            case '┏' -> corner = Corner.FROM_BELOW;
            case '┗' -> corner = Corner.FROM_ABOVE;
            case '┓' -> {
              if (corner == Corner.FROM_ABOVE) {
                inside = !inside;
              } else {
                assert corner == Corner.FROM_BELOW;
              }
              corner = Corner.NONE;
            }
            case '┛' -> {
              if (corner == Corner.FROM_BELOW) {
                inside = !inside;
              } else {
                assert corner == Corner.FROM_ABOVE;
              }
              corner = Corner.NONE;
            }
          }
        }
      }

      // Print a pretty map.
      for (char[] line : pipe) {
        for (int j = 0; j < line.length; j++) {
          System.out.print(line[j]);
        }
        System.out.println();
      }

      System.out.println("Inside count " + insideCount);
    }
  }

  private static Direction otherDir(EnumSet<Direction> dirs, Direction dir) {
    assert dirs.contains(dir) : "Dirs " + dirs + " dir " + dir;
    var copy = EnumSet.copyOf(dirs);
    copy.remove(dir);
    return copy.iterator().next();
  }

  enum Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN;

    Direction opposite() {
      return switch (this) {
        case LEFT -> RIGHT;
        case RIGHT -> LEFT;
        case UP -> DOWN;
        case DOWN -> UP;
      };
    }
  }

  private static final ImmutableBiMap<Character, EnumSet<Direction>> CHAR_TO_DIR =
      ImmutableBiMap.of(
          '-', EnumSet.of(LEFT, RIGHT),
          '|', EnumSet.of(UP, DOWN),
          'J', EnumSet.of(LEFT, UP),
          'F', EnumSet.of(DOWN, RIGHT),
          'L', EnumSet.of(UP, RIGHT),
          '7', EnumSet.of(DOWN, LEFT));

  private static final ImmutableMap<Character, Character> BOX_DRAWING =
      ImmutableMap.of('-', '━', '|', '┃', 'J', '┛', 'F', '┏', 'L', '┗', '7', '┓');
}
