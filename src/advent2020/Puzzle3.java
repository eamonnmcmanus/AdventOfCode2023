package advent2020;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
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
public class Puzzle3 {
  private static final String SAMPLE =
      """
      ..##.......
      #...#...#..
      .#....#..#.
      ..#.#...#.#
      .#...##..#.
      ..#.##.....
      .#.#.#....#
      .#........#
      #.##...#...
      #...##....#
      .#..#...#.#
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle3.class.getResourceAsStream("puzzle3.txt")));

  private static final Pattern PATTERN = Pattern.compile("(\\d+)-(\\d+) (.): (.*)");

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        CharGrid grid = new CharGrid(lines);
        System.out.printf("For %s, Part 1 tree count %d\n", name, treeCount(grid, 3, 1));
        long part2Trees =
            treeCount(grid, 1, 1)
                * treeCount(grid, 3, 1)
                * treeCount(grid, 5, 1)
                * treeCount(grid, 7, 1)
                * treeCount(grid, 1, 2);
        System.out.printf("For %s, Part 2 tree product %d\n", name, part2Trees);
      }
    }
  }

  private static long treeCount(CharGrid grid, int colDelta, int lineDelta) {
    int trees = 0;
    for (Coord coord = new Coord(0, 0);
        coord.line() < grid.height();
        coord = new Coord(coord.line() + lineDelta, (coord.col() + colDelta) % grid.width())) {
      if (grid.get(coord) == '#') {
        trees++;
      }
    }
    return trees;
  }
}