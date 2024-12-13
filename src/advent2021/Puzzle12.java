package advent2021;

import static com.google.common.base.Preconditions.checkArgument;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Éamonn McManus
 */
public class Puzzle12 {
  private static final String SAMPLE =
      """
      fs-end
      he-DX
      fs-he
      start-DX
      pj-DX
      end-zg
      zg-sl
      zg-pj
      pj-he
      RW-he
      fs-DX
      pj-RW
      zg-RW
      start-pj
      he-WI
      zg-he
      pj-fs
      start-RW
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle12.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        ImmutableGraph.Builder<String> graphBuilder = GraphBuilder.undirected().immutable();
        for (String line : lines) {
          var parts = Splitter.on('-').splitToList(line);
          checkArgument(parts.size() == 2);
          graphBuilder.putEdge(parts.get(0), parts.get(1));
        }
        Graph<String> graph = graphBuilder.build();

        // Part 1
        long part1Paths = countPaths1(graph, "start", ImmutableSet.of());
        System.out.printf("For Part 1 %s, number of paths is %d\n", name, part1Paths);

        // Part 2
        long part2Paths = 0;
        part2Paths += countPaths2(graph, "start", ImmutableSet.of(), false);
        System.out.printf("For Part 2 %s, number of paths is %d\n", name, part2Paths);
      }
    }
  }

  private static long countPaths1(Graph<String> graph, String start, ImmutableSet<String> visited) {
    if (visited.contains(start)) {
      return 0;
    }
    if (start.equals("end")) {
      return 1;
    }
    long paths = 0;
    if (startsWithLowerCase(start)) {
      visited = ImmutableSet.<String>builder().addAll(visited).add(start).build();
    }
    for (String next : graph.successors(start)) {
      paths += countPaths1(graph, next, visited);
    }
    return paths;
  }

  private static final ImmutableSet<String> START_END = ImmutableSet.of("start", "end");

  private static long countPaths2(
      Graph<String> graph, String start, ImmutableSet<String> visited, boolean haveVisitedTwice) {
    if (visited.contains(start)) {
      if (haveVisitedTwice || START_END.contains(start)) {
        return 0;
      }
      haveVisitedTwice = true;
    }
    if (start.equals("end")) {
      return 1;
    }
    long paths = 0;
    if (startsWithLowerCase(start)) {
      visited = ImmutableSet.<String>builder().addAll(visited).add(start).build();
    }
    for (String next : graph.successors(start)) {
      paths += countPaths2(graph, next, visited, haveVisitedTwice);
    }
    return paths;
  }

  private static boolean startsWithLowerCase(String s) {
    return Character.isLowerCase(s.charAt(0));
  }
}