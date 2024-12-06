package advent2024;

import advent2024.CharGrid.Coord;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle6 {
  private static final String SAMPLE =
      """
      ....#.....
      .........#
      ..........
      ..#.......
      .......#..
      ..........
      .#..^.....
      ........#.
      #.........
      ......#...
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle6.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        CharGrid grid = new CharGrid(lines);
        Coord start = grid.firstMatch(c -> c == '^').get();
        System.out.println("Part 1 result for " + name + " is " + part1(grid, start, Dir.N));
        System.out.println("Part 2 result for " + name + " is " + part2(grid, start, Dir.N));
      }
    }
  }

  // Walk through the grid from the starting position and direction until we exit. Return the number
  // of different positions visited (including the starting position).
  private static int part1(CharGrid grid, Coord coord, Dir dir) {
    Set<Coord> visited = new LinkedHashSet<>(Set.of(coord));
    while (true) {
      Coord next = dir.move(coord, 1);
      if (!grid.valid(next)) {
        break;
      }
      if (grid.get(next) == '#') {
        dir = dir.right90();
        continue;
      }
      visited.add(next);
      coord = next;
    }
    return visited.size();
  }

  // Walk through the grid from the starting position and direction until we exit or detect a loop.
  // A loop is when we find ourselves in a position and direction we have already been in.
  // Return true if we exited or false if we looped.
  private static boolean willExit(CharGrid grid, Coord coord, Dir dir) {
    record Position(Coord coord, Dir dir) {}
    Set<Position> visited = new LinkedHashSet<>(Set.of(new Position(coord, dir)));
    while (true) {
      Coord next = dir.move(coord, 1);
      if (!grid.valid(next)) {
        return true; // exited
      }
      if (grid.get(next) == '#') {
        dir = dir.right90();
        continue;
      }
      if (!visited.add(new Position(next, dir))) {
        return false; // looped
      }
      coord = next;
    }
  }

  private static long part2(CharGrid grid, Coord start, Dir dir) {
    long total = 0;
    for (int line = 0; line < grid.height(); line++) {
      for (int col = 0; col < grid.width(); col++) {
        if (grid.get(line, col) == '.') {
          CharGrid newGrid = grid.withChange(new Coord(line, col), '#');
          if (!willExit(newGrid, start, dir)) {
            total++;
          }
        }
      }
    }
    return total;
  }
}