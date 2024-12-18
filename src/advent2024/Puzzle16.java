package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.util.stream.Collectors.toSet;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle16 {
  private static final String SAMPLE1 =
      """
      ###############
      #.......#....E#
      #.#.###.#.###.#
      #.....#.#...#.#
      #.###.#####.#.#
      #.#.#.......#.#
      #.#.#####.###.#
      #...........#.#
      ###.#.#####.#.#
      #...#.....#.#.#
      #.#.#.###.#.#.#
      #.....#...#.#.#
      #.###.#.#.#.#.#
      #S..#.....#...#
      ###############
      """;

  private static final String SAMPLE2 =
      """
      #################
      #...#...#...#..E#
      #.#.#.#.#.#.#.#.#
      #.#.#.#...#...#.#
      #.#.#.#.###.#.#.#
      #...#.#.#.....#.#
      #.#.#.#.#.#####.#
      #.#...#.#.#.....#
      #.#.#####.#.###.#
      #.#.#.......#...#
      #.#.###.#####.###
      #.#.#...#.....#.#
      #.#.#.#####.###.#
      #.#.#.........#.#
      #.#.#.#########.#
      #S#.............#
      #################
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample 1",
          () -> new StringReader(SAMPLE1),
          "sample 2",
          () -> new StringReader(SAMPLE2),
          "problem",
          () -> new InputStreamReader(Puzzle16.class.getResourceAsStream("puzzle16.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        var maze = parseMaze(lines);
        var costs = computeCosts(maze);
        int leastCost = Collections.min(costs.row(maze.end).values());
        System.out.printf("For %s, least cost is %d\n", name, leastCost);
        Set<CoordPair> bestEdges = new LinkedHashSet<>();
        reverseVisit(maze, costs, bestEdges, maze.end, leastCost);
        Set<Coord> bestCoords = new LinkedHashSet<>();
        for (var pair : bestEdges) {
          // TODO: clean up
          Coord start = pair.first;
          Coord end = pair.second;
          bestCoords.add(start);
          bestCoords.add(end);
          if (start.col() == end.col()) {
            for (int line = min(start.line(), end.line());
                line < max(start.line(), end.line());
                line++) {
              bestCoords.add(new Coord(line, start.col()));
            }
          } else {
            checkState(start.line() == end.line());
            for (int col = min(start.col(), end.col()); col < max(start.col(), end.col()); col++) {
              bestCoords.add(new Coord(start.line(), col));
            }
          }
        }
        System.out.printf("For %s, tiles on path: %d\n", name, bestCoords.size());
      }
    }
  }

  private static Dir direction(Coord a, Coord b) {
    if (a.line() == b.line()) {
      return a.col() < b.col() ? Dir.E : Dir.W;
    } else {
      checkState(a.col() == b.col());
      return a.line() < b.line() ? Dir.N : Dir.S;
    }
  }

  private static int distance(Coord a, Coord b) {
    return abs(a.line() - b.line() + abs(a.col() - b.col()));
  }

  private static Table<Coord, Dir, Integer> computeCosts(Maze maze) {
    Table<Coord, Dir, Integer> costs = HashBasedTable.create();
    visit(maze, costs, maze.start, Dir.E, 0);
    return costs;
  }

  // Each node in the graph has a map from direction entered to lowest cost. If we reach a node
  // in a certain direction and it already has a cost for that direction, then if that cost is lower
  // than the current cost we can stop along this path, and otherwise we can update the cost and
  // keep going.
  private static void visit(
      Maze maze,
      Table<Coord, Dir, Integer> costs,
      Coord current,
      Dir enteringFrom,
      int costOnCurrentPath) {
    Integer existingCost = costs.get(current, enteringFrom);
    if (existingCost != null && existingCost <= costOnCurrentPath) {
      return;
    }
    costs.put(current, enteringFrom, costOnCurrentPath);
    for (Coord next : maze.graph.successors(current)) {
      int nextCost = costOnCurrentPath + distance(current, next);
      Dir direction = direction(current, next);
      if (direction != enteringFrom) {
        nextCost += 1000;
      }
      visit(maze, costs, next, direction, nextCost);
    }
  }

  record CoordPair(Coord first, Coord second) {
    @Override
    public String toString() {
      return first + "-" + second;
    }
  }

  // Work backwards from the end, starting with the final cost. At each point, we have a target cost
  // which we know correspnods to the best path, and we can trace along every edge where the
  // difference between this target cost and the cost of the other end of the edge is indeed the
  // cost that the edge would have added.
  private static void reverseVisit(
      Maze maze,
      Table<Coord, Dir, Integer> costs,
      Set<CoordPair> bestEdges,
      Coord current,
      int targetCost) {
    for (Coord prev : maze.graph.predecessors(current)) {
      int distance = distance(prev, current);
      Dir dir = direction(prev, current);
      costs
          .row(prev)
          .forEach(
              (prevDir, cost) -> {
                int edgeCost = (dir == prevDir) ? distance : distance + 1000;
                if (cost + edgeCost == targetCost) {
                  bestEdges.add(new CoordPair(prev, current));
                  reverseVisit(maze, costs, bestEdges, prev, cost);
                }
              });
    }
  }

  record Maze(CharGrid grid, ImmutableGraph<Coord> graph, Coord start, Coord end) {}

  private static Maze parseMaze(List<String> lines) {
    CharGrid grid = new CharGrid(lines);
    Coord start = new Coord(grid.height() - 2, 1);
    checkArgument(grid.get(start) == 'S');
    Coord end = new Coord(1, grid.width() - 2);
    checkArgument(grid.get(end) == 'E');
    ImmutableGraph.Builder<Coord> builder = GraphBuilder.undirected().immutable();
    parseMaze(builder, grid);
    ImmutableGraph<Coord> graph = builder.build();
    if (false) {
      System.out.println(graph);
      for (Coord coord : grid.coords()) {
        if (coord.col() == 0) {
          System.out.println();
        }
        if (graph.nodes().contains(coord)) {
          System.out.print('+');
        } else {
          System.out.print(grid.get(coord));
        }
      }
      System.out.println();
    }
    return new Maze(grid, graph, start, end);
  }

  /**
   * Constructs a graph from the maze grid. This is a little clunky and it might be better just to
   * do a DFS through the grid. Instead, we look at the neighbours of every empty cell. If it has
   * more than 2 neighbours, or if it has exactly 2 and they are not opposite each other, then we
   * have a node in the graph.
   */
  private static void parseMaze(ImmutableGraph.Builder<Coord> builder, CharGrid grid) {
    // Determine the nodes of the graph. A position is a node if it is S (start) or E (end), or if
    // it has non-walls on two non-opposite sides.
    Set<Coord> nodes = new LinkedHashSet<>();
    for (Coord coord : grid.coords()) {
      switch (grid.get(coord)) {
        case 'S', 'E' -> nodes.add(coord);
        case '.' -> {
          if (isCorner(grid, coord)) {
            nodes.add(coord);
          }
        }
        case '#' -> {}
        default -> throw new AssertionError(grid.get(coord));
      }
    }

    // Trace each row, making an edge between two consecutive nodes unless there is a wall between.
    for (int line = 1; line < grid.height() - 1; line++) {
      Coord prev = null;
      for (int col = 1; col < grid.width() - 1; col++) {
        Coord here = new Coord(line, col);
        if (grid.get(here) == '#') {
          prev = null;
        }
        if (nodes.contains(here)) {
          if (prev != null) {
            builder.putEdge(prev, here);
          }
          prev = here;
        }
      }
    }

    // Same thing for the columns.
    for (int col = 1; col < grid.width() - 1; col++) {
      Coord prev = null;
      for (int line = 1; line < grid.height() - 1; line++) {
        Coord here = new Coord(line, col);
        if (grid.get(here) == '#') {
          prev = null;
        }
        if (nodes.contains(here)) {
          if (prev != null) {
            builder.putEdge(prev, here);
          }
          prev = here;
        }
      }
    }
  }

  private static final Set<Dir> NS = EnumSet.of(Dir.N, Dir.S);
  private static final Set<Dir> EW = EnumSet.of(Dir.E, Dir.W);
  private static final Set<Set<Dir>> NS_OR_EW = ImmutableSet.of(NS, EW);

  private static boolean isCorner(CharGrid grid, Coord coord) {
    Set<Dir> adjacent =
        Dir.NEWS.stream().filter(dir -> grid.get(dir.move(coord)) != '#').collect(toSet());
    return switch (adjacent.size()) {
      case 0, 1 -> false;
      case 3, 4 -> true;
      case 2 -> !NS_OR_EW.contains(adjacent);
      default -> throw new AssertionError(adjacent.size());
    };
  }
}