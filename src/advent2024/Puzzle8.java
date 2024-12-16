package advent2024;

import static java.util.Arrays.stream;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.CharStreams;
import com.google.common.primitives.Booleans;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle8 {
  private static final String SAMPLE =
      """
      ............
      ........0...
      .....0......
      .......0....
      ....0.......
      ......A.....
      ............
      ............
      ........A...
      .........A..
      ............
      ............
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle8.class.getResourceAsStream("puzzle8.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        CharGrid grid = new CharGrid(lines);
        ListMultimap<Character, Coord> antennae = ArrayListMultimap.create();
        for (int line = 0; line < grid.height(); line++) {
          for (int col = 0; col < grid.width(); col++) {
            Coord coord = new Coord(line, col);
            char c = grid.get(coord);
            if (c != '.') {
              antennae.put(c, coord);
            }
          }
        }
        System.out.printf("Part 1 antinode count for %s is %d\n", name, part1(grid, antennae));
        System.out.printf("Part 2 antinode count for %s is %d\n", name, part2(grid, antennae));
      }
    }
  }

  private static long part1(CharGrid grid, ListMultimap<Character, Coord> antennae) {
    boolean[][] antinodes = new boolean[grid.height()][grid.width()];
    Multimaps.asMap(antennae)
        .forEach(
            (c, coords) -> {
              for (int i = 0; i < coords.size(); i++) {
                Coord coord1 = coords.get(i);
                for (int j = i + 1; j < coords.size(); j++) {
                  Coord coord2 = coords.get(j);
                  Coord delta = coord2.minus(coord1);
                  Coord antinode1 = coord1.minus(delta);
                  if (grid.valid(antinode1)) {
                    antinodes[antinode1.line()][antinode1.col()] = true;
                  }
                  Coord antinode2 = coord2.plus(delta);
                  if (grid.valid(antinode2)) {
                    antinodes[antinode2.line()][antinode2.col()] = true;
                  }
                }
              }
            });
    return stream(antinodes).mapToInt(Booleans::countTrue).sum();
  }

  private static long part2(CharGrid grid, ListMultimap<Character, Coord> antennae) {
    boolean[][] antinodes = new boolean[grid.height()][grid.width()];
    Multimaps.asMap(antennae)
        .forEach(
            (c, coords) -> {
              for (int i = 0; i < coords.size(); i++) {
                Coord coord1 = coords.get(i);
                for (int j = i + 1; j < coords.size(); j++) {
                  Coord coord2 = coords.get(j);
                  Coord delta = coord2.minus(coord1);
                  for (Coord antinode = coord1;
                      grid.valid(antinode);
                      antinode = antinode.minus(delta)) {
                    antinodes[antinode.line()][antinode.col()] = true;
                  }
                  for (Coord antinode = coord2;
                      grid.valid(antinode);
                      antinode = antinode.plus(delta)) {
                    antinodes[antinode.line()][antinode.col()] = true;
                  }
                }
              }
            });
    return stream(antinodes).mapToInt(Booleans::countTrue).sum();
  }
}