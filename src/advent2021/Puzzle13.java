package advent2021;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.primitives.Booleans;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle13 {
  private static final String SAMPLE =
      """
      6,10
      0,14
      9,10
      0,3
      10,4
      4,11
      6,0
      6,12
      4,1
      0,13
      10,12
      3,4
      3,0
      8,4
      1,10
      2,14
      8,10
      9,0

      fold along y=7
      fold along x=5
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle13.txt")));

  record Coord(int x, int y) {}

  sealed interface Fold {}

  record XFold(int x) implements Fold {}

  record YFold(int y) implements Fold {}

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int blank = lines.indexOf("");
        checkState(blank > 0);
        checkState(blank == lines.lastIndexOf(""));

        List<Coord> coords =
            lines.subList(0, blank).stream()
                .map(
                    line -> {
                      var xy = Splitter.on(',').splitToStream(line).map(Integer::valueOf).toList();
                      return new Coord(xy.get(0), xy.get(1));
                    })
                .toList();
        int maxX = coords.stream().mapToInt(Coord::x).max().getAsInt();
        int maxY = coords.stream().mapToInt(Coord::y).max().getAsInt();
        boolean[][] grid = new boolean[maxX + 1][maxY + 1];
        for (Coord coord : coords) {
          grid[coord.x][coord.y] = true;
        }

        Pattern foldAlong = Pattern.compile("fold along ([xy])=(\\d+)");
        List<Fold> folds =
            lines.subList(blank + 1, lines.size()).stream()
                .map(foldAlong::matcher)
                .peek(m -> checkState(m.matches()))
                .map(
                    m -> {
                      int line = Integer.parseInt(m.group(2));
                      return (Fold)
                          switch (m.group(1)) {
                            case "x" -> new XFold(line);
                            case "y" -> new YFold(line);
                            default -> throw new IllegalArgumentException(m.toString());
                          };
                    })
                .toList();
        boolean[][] gridAfterFirstFold = fold(grid, folds.get(0));
        System.out.printf(
            "For %s, after first fold dot count is %d\n", name, count(gridAfterFirstFold));
        boolean[][] newGrid = grid;
        for (Fold fold : folds) {
          newGrid = fold(newGrid, fold);
        }
        show(newGrid);
      }
    }
  }

  // input is indexed [x][y] so input[0] is all spaces with x coordinate 0.
  // input.length says how many x values there are, so it is the width.
  private static boolean[][] fold(boolean[][] input, Fold fold) {
    int height = input[0].length;
    int width = input.length;
    return switch (fold) {
      case XFold(int x) -> {
        boolean[][] output = new boolean[x][height];
        for (int i = 1; i <= x && x + i < width; i++) {
          for (int y = 0; y < height; y++) {
            output[x - i][y] = input[x - i][y] | input[x + i][y];
          }
        }
        yield output;
      }
      case YFold(int y) -> {
        boolean[][] output = new boolean[width][y];
        for (int i = 1; i <= y && y + i < height; i++) {
          for (int x = 0; x < width; x++) {
            output[x][y - i] = input[x][y - i] | input[x][y + i];
          }
        }
        yield output;
      }
    };
  }

  private static int count(boolean[][] grid) {
    return stream(grid).mapToInt(Booleans::countTrue).sum();
  }

  private static void show(boolean[][] grid) {
    int height = grid[0].length;
    int width = grid.length;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        System.out.print(grid[x][y] ? '#' : '.');
      }
      System.out.println();
    }
  }
}