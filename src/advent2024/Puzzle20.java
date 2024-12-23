package advent2024;

import static java.lang.Math.abs;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import adventlib.GraphAlgorithms;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle20 {
  private static final String SAMPLE =
      """
      ###############
      #...#...#.....#
      #.#.#.#.#.###.#
      #S#...#.#.#...#
      #######.#.#.###
      #######.#.#...#
      #######.#.###.#
      ###..E#...#...#
      ###.#######.###
      #...###...#...#
      #.#####.#.###.#
      #.#...#.#.#...#
      #.#.#.#.#.#.###
      #...#...#...###
      ###############
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle20.class.getResourceAsStream("puzzle20.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        var grid = new CharGrid(lines);
        Coord start = grid.firstMatch(c -> c == 'S').get();
        Coord end = grid.firstMatch(c -> c == 'E').get();
        Graph<Coord> graph = makeGraph(grid);
        ImmutableList<Coord> path = GraphAlgorithms.shortestPath(graph, start, end);
        path = ImmutableList.<Coord>builder().add(start).addAll(path).build();
        var distances = GraphAlgorithms.distances(graph, end);
        int minSave = 100;
        if (name.equals("sample")) {
          minSave = 20;
        }
        System.out.printf(
            "For %s, Part 1 cheats saving at least %d: %d\n",
            name, minSave, cheatCount(path, distances, 2, minSave));
        if (name.equals("sample")) {
          minSave = 70;
        }
        System.out.printf(
            "For %s, Part 2 cheats saving at least %d: %d\n",
            name, minSave, cheatCount(path, distances, 20, minSave));
      }
    }
  }

  // We consider every possible pair (a,b) where a is on the path and b has a Manhattan distance
  // from a that is no greater than cheatLength. For Part 1, I initially just considered every
  // possible combination of directions for the two moves, but the Part 2 algorithm works just fine
  // with cheatLength=2.
  private static int cheatCount(
      ImmutableList<Coord> path,
      ImmutableMap<Coord, Integer> distances,
      int cheatLength,
      int minSave) {
    int cheatCount = 0;
    int distance = path.size() - 1;
    for (Coord coord : path) {
      for (int rowJump = -cheatLength; rowJump <= cheatLength; rowJump++) {
        int remain = cheatLength - abs(rowJump);
        for (int colJump = -remain; colJump <= remain; colJump++) {
          Coord cheat = new Coord(coord.line() + rowJump, coord.col() + colJump);
          if (distances.containsKey(cheat)) {
            int cheatDistance = distances.get(cheat);
            // If the current distance is 80, the cheat distance is 60 then the saving from
            // cheating is 80 - 60 - j, where j is the size of the jump.
            int saving = distance - cheatDistance - abs(rowJump) - abs(colJump);
            if (saving >= minSave) {
              cheatCount++;
            }
          }
        }
      }
      distance--;
    }
    return cheatCount;
  }

  private static ImmutableGraph<Coord> makeGraph(CharGrid grid) {
    ImmutableGraph.Builder<Coord> builder = GraphBuilder.undirected().immutable();
    for (Coord coord : grid.coords()) {
      if (grid.get(coord) != '#') {
        for (Dir dir : Dir.NEWS) {
          Coord moved = dir.move(coord);
          if (grid.get(moved) != '#') {
            builder.putEdge(coord, moved);
          }
        }
      }
    }
    return builder.build();
  }
}