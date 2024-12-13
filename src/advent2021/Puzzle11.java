package advent2021;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Éamonn McManus
 */
public class Puzzle11 {
  private static final String SAMPLE =
      """
      5483143223
      2745854711
      5264556173
      6141336146
      6357385478
      4167524645
      2176841721
      6882881134
      4846848554
      5283751526
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle11.txt")));

  private static class Octopus {
    int energy;

    Octopus(int energy) {
      this.energy = energy;
    }
  }

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        CharGrid grid = new CharGrid(lines);
        Function<Coord, Octopus> nodeFactory = coord -> new Octopus(grid.get(coord) - '0');
        ImmutableGraph<Octopus> graph = grid.toGraph(EnumSet.allOf(Dir.class), nodeFactory);

        // Part 1
        int flashCount = 0;
        for (int i = 0; i < 100; i++) {
          flashCount += step(graph);
        }
        System.out.printf("For %s, after 100 steps, total flashes %d\n", name, flashCount);

        // Part 2
        graph = grid.toGraph(EnumSet.allOf(Dir.class), nodeFactory);
        int step;
        for (step = 1; ; step++) {
          int stepCount = step(graph);
          if (stepCount == grid.size()) {
            break;
          }
        }
        System.out.printf("For %s, all octopodes flashed at step %d\n", name, step);
      }
    }
  }

  private static int step(ImmutableGraph<Octopus> graph) {
    for (var octopus : graph.nodes()) {
      octopus.energy++;
    }
    Set<Octopus> flashed = new LinkedHashSet<>();
    boolean changed;
    do {
      changed = false;
      for (var octopus : graph.nodes()) {
        if (octopus.energy > 9 && flashed.add(octopus)) {
          changed = true;
          for (var adjacent : graph.successors(octopus)) {
            adjacent.energy++;
          }
        }
      }
    } while (changed);
    for (var octopus : flashed) {
      octopus.energy = 0;
    }
    return flashed.size();
  }
}