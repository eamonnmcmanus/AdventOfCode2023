package advent2024;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle18 {
  private static final String SAMPLE =
      """
      5,4
      4,2
      4,5
      3,0
      2,1
      6,3
      2,4
      1,5
      0,6
      3,3
      2,6
      5,1
      1,2
      5,5
      2,5
      6,5
      1,4
      0,4
      6,4
      1,1
      6,1
      1,0
      0,5
      1,6
      2,0
      """;

  @FunctionalInterface
  interface ReaderCallable extends Callable<Reader> {}

  record Input(ReaderCallable reader, int part1Max) {}

  private static final ImmutableMap<String, Input> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          new Input(() -> new StringReader(SAMPLE), 12),
          "problem",
          new Input(
              () -> new InputStreamReader(Puzzle18.class.getResourceAsStream("puzzle18.txt")),
              1024));

  record Coord(int x, int y) {
    List<Coord> adjacent() {
      return List.of(
          new Coord(x - 1, y), new Coord(x + 1, y), new Coord(x, y - 1), new Coord(x, y + 1));
    }
  }

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      int part1Max = entry.getValue().part1Max;
      try (Reader r = entry.getValue().reader.call()) {
        List<String> lines = CharStreams.readLines(r);
        Pattern coordPattern = Pattern.compile("(\\d+),(\\d+)");
        List<Coord> coords =
            lines.stream()
                .map(coordPattern::matcher)
                .peek(m -> checkState(m.matches()))
                .map(m -> new Coord(parseInt(m.group(1)), parseInt(m.group(2))))
                .toList();
        int maxX = coords.stream().mapToInt(Coord::x).max().getAsInt();
        int maxY = coords.stream().mapToInt(Coord::y).max().getAsInt();

        Set<Coord> blocked = new LinkedHashSet<>(coords.subList(0, part1Max));

        {
          Map<Coord, Integer> costs = new LinkedHashMap<>();
          traverse(new Coord(0, 0), 0, maxX, maxY, blocked, costs);
          System.out.printf(
              "For %s, minimum distance is %d\n", name, costs.get(new Coord(maxX, maxY)));
        }

        // Part2
        {
          // This brute-force solution takes about 3 minutes to run. One idea to speed it up is to
          // construct a shortest path explicitly. Then as long as no newly-blocked coordinate is on
          // that path, we don't need to reconstruct it.
          Coord result = null;
          for (Coord block : coords.subList(part1Max, coords.size())) {
            blocked.add(block);
            Map<Coord, Integer> costs = new LinkedHashMap<>();
            traverse(new Coord(0, 0), 0, maxX, maxY, blocked, costs);
            if (costs.get(new Coord(maxX, maxY)) == null) {
              result = block;
              break;
            }
          }
          checkNotNull(result);
          System.out.printf("For %s, first blocking coord is %d,%d\n", name, result.x, result.y);
        }
      }
    }
  }

  private static void traverse(
      Coord start,
      int startCost,
      int maxX,
      int maxY,
      Set<Coord> blocked,
      Map<Coord, Integer> costs) {
    if (startCost >= costs.getOrDefault(start, Integer.MAX_VALUE)) {
      return;
    }
    costs.put(start, startCost);
    for (Coord adj : start.adjacent()) {
      if (adj.x >= 0 && adj.y >= 0 && adj.x <= maxX && adj.y <= maxY && !blocked.contains(adj)) {
        traverse(adj, startCost + 1, maxX, maxY, blocked, costs);
      }
    }
  }
}