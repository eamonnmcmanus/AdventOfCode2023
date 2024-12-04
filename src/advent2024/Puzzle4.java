package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.incrementExact;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

/**
 * @author Éamonn McManus
 */
public class Puzzle4 {
  private static final String SAMPLE =
      """
      MMMSXXMASM
      MSAMXMSMSA
      AMXSXMAAMM
      MSAMASMSMX
      XMASAMXAMM
      XXAMMXXAMA
      SMSMSASXSS
      SAXAMASAAA
      MAMMMXMMMM
      MXMXAXMASX
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle4.class.getResourceAsStream("puzzle4.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Grid grid = new Grid(lines);
        System.out.println("Part 1 count for " + name + " is " + grid.countMatches1());
        System.out.println("Part 2 count for " + name + " is " + grid.countMatches2());
      }
    }
  }

  private record Coord(int line, int col) {}

  private enum Dir {
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
      return new Coord(c.line + lineDelta, c.col + colDelta);
    }
  }

  private record Grid(List<String> lines) {
    Grid {
      checkArgument(lines != null && !lines.isEmpty());
      int width = lines.getFirst().length();
      checkArgument(lines.stream().allMatch(line -> line.length() == width));
    }

    char get(Coord coord) {
      return get(coord.line, coord.col);
    }

    char get(int line, int col) {
      if (line < 0
          || line >= lines.size()
          || col < 0
          || col >= lines.getFirst().length()) {
        return ' ';
      }
      return lines.get(line).charAt(col);
    }

    int countMatches1() {
      int count = 0;
      EnumSet<Dir> allDirs = EnumSet.allOf(Dir.class);
      int width = lines.getFirst().length();
      for (int line = 0; line < lines.size(); line++) {
        for (int col = 0; col < width; col++) {
          Coord coord = new Coord(line, col);
          for (Dir dir : allDirs) {
            boolean match = true;
            for (int i = 0; i < 4; i++) {
              char c = get(dir.move(coord, i));
              if (c != "XMAS".charAt(i)) {
                match = false;
                break;
              }
            }
            if (match) {
              count = incrementExact(count);
            }
          }
        }
      }
      return count;
    }

    int countMatches2() {
      var ms = ImmutableSet.of('M', 'S');
      int width = lines.getFirst().length();
      int count = 0;
      for (int line = 1; line < lines.size() - 1; line++) {
        for (int col = 1; col < width - 1; col++) {
          if (get(line, col) != 'A') {
            continue;
          }
          var set1 = ImmutableSet.of(get(line - 1, col - 1), get(line + 1, col + 1));
          if (!set1.equals(ms)) {
            continue;
          }
          var set2 = ImmutableSet.of(get(line - 1, col + 1), get(line + 1, col - 1));
          if (!set2.equals(ms)) {
            continue;
          }
          count = incrementExact(count);
        }
      }
      return count;
    }
  }
}