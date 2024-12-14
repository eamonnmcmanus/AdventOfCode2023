package advent2024;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
        ImmutableSet<Coord> path = part1(grid, start, Dir.N);
        System.out.println("Part 1 result for " + name + " is " + path.size());
        System.out.println("Part 2 result for " + name + " is " + part2(grid, start, Dir.N, path));
      }
    }
  }

  // Walk through the grid from the starting position and direction until we exit. Return the number
  // of different positions visited (including the starting position).
  private static ImmutableSet<Coord> part1(CharGrid grid, Coord coord, Dir dir) {
    ImmutableSet.Builder<Coord> visited = ImmutableSet.builder();
    visited.add(coord);
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
    return visited.build();
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

  // We can only change the path with an obstacle that is in the path, so only try those positions.
  private static long part2(CharGrid grid, Coord start, Dir dir, Set<Coord> path) {
    long total = 0;
    for (Coord coord : path) {
      if (grid.get(coord) == '.') {
        CharGrid newGrid = grid.withChange(coord, '#');
        if (!willExit(newGrid, start, dir)) {
          total++;
        }
      }
    }
    return total;
  }
}