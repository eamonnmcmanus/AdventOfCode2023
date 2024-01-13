package advent2022;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle14 {
  private static final String SAMPLE =
      """
      498,4 -> 498,6 -> 496,6
      503,4 -> 502,4 -> 502,9 -> 494,9
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle14.class.getResourceAsStream("puzzle14.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int countPart1 = solve(lines, false);
        System.out.println(STR."For \{name}, part 1 count is \{countPart1}");
        int countPart2 = solve(lines, true);
        System.out.println(STR."For \{name}, part 2 count is \{countPart2}");
      }
    }
  }

  private static int solve(List<String> lines, boolean part2) {
    char[][] grid = makeGrid(lines);

    if (part2) {
      int maxY = 0;
      for (int y = 0; y < grid.length; y++) {
        if (!allDots(grid[y])) {
          maxY = y;
        }
      }
      Arrays.fill(grid[maxY + 2], '#');
    }

    int count = 0;
    while (grid[0][500] == '.') {
      boolean changed = false;
      int x = 500;
      for (int y = 1; y < grid.length; y++) {
        if (grid[y][x] != '.') {
          if (grid[y][x - 1] == '.') {
            --x;
          } else if (grid[y][x + 1] == '.') {
            ++x;
          } else {
            grid[y - 1][x] = 'o';
            changed = true;
            break; // I forgot this at first, so I wasted a lot of time debugging.
          }
        }
      }
      if (changed) {
        count++;
      } else {
        break;
      }
    }
    return count;
  }

  private static boolean allDots(char[] line) {
    for (char c : line) {
      if (c != '.') {
        return false;
      }
    }
    return true;
  }

  private static char[][] makeGrid(List<String> lines) {
    char[][] grid = new char[501][1000];
    for (char[] line : grid) {
      Arrays.fill(line, '.');
    }
    record Pair(int x, int y) {}
    for (String line : lines) {
      List<Pair> pairs = Pattern.compile("(\\d+),(\\d+)")
          .matcher(line)
          .results()
          .map(mr -> new Pair(Integer.parseInt(mr.group(1)), Integer.parseInt(mr.group(2))))
          .toList();
      for (int i = 1; i < pairs.size(); i++) {
        Pair from = pairs.get(i - 1);
        Pair to = pairs.get(i);
        if (from.x == to.x) {
          for (int y = min(from.y, to.y); y <= max(from.y, to.y); y++) {
            grid[y][from.x] = '#';
          }
        } else if (from.y == to.y) {
          for (int x = min(from.x, to.x); x <= max(from.x, to.x); x++) {
            grid[from.y][x] = '#';
          }
        } else {
          throw new IllegalStateException(STR."Diagonal line \{from.x},\{from.y} -> \{to.x},\{to.y}");
        }
      }
    }
    return grid;
  }
}
