package advent2024;


import static java.lang.Math.incrementExact;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
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
        CharGrid grid = new CharGrid(lines);
        System.out.println("Part 1 count for " + name + " is " + countMatches1(grid));
        System.out.println("Part 2 count for " + name + " is " + countMatches2(grid));
      }
    }
  }

  private static int countMatches1(CharGrid grid) {
    int count = 0;
    EnumSet<Dir> allDirs = EnumSet.allOf(Dir.class);
    for (int line = 0; line < grid.height(); line++) {
      for (int col = 0; col < grid.width(); col++) {
        Coord coord = new Coord(line, col);
        for (Dir dir : allDirs) {
          boolean match = true;
          for (int i = 0; i < 4; i++) {
            char c = grid.get(dir.move(coord, i));
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

  private static int countMatches2(CharGrid grid) {
    var ms = ImmutableSet.of('M', 'S');
    int count = 0;
    for (int line = 1; line < grid.height() - 1; line++) {
      for (int col = 1; col < grid.width() - 1; col++) {
        if (grid.get(line, col) != 'A') {
          continue;
        }
        var set1 = ImmutableSet.of(grid.get(line - 1, col - 1), grid.get(line + 1, col + 1));
        if (!set1.equals(ms)) {
          continue;
        }
        var set2 = ImmutableSet.of(grid.get(line - 1, col + 1), grid.get(line + 1, col - 1));
        if (!set2.equals(ms)) {
          continue;
        }
        count = incrementExact(count);
      }
    }
    return count;
  }
}