package advent2024;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

import com.google.common.collect.ImmutableMap;
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
        Coord start = new Coord(0, 0);
        Coord end = new Coord(maxX, maxY);

        Set<Coord> blocked = new LinkedHashSet<>(coords.subList(0, part1Max));

        {
          Map<Coord, Integer> costs = new LinkedHashMap<>();
          traverse(start, end, 0, blocked, costs);
          System.out.printf(
              "For %s, minimum distance is %d\n", name, costs.get(new Coord(maxX, maxY)));
        }

        // Part2. To speed this up, we remember the last successful path. If a new blocked cell is
        // not on that path then we don't need to construct a new path. This reduces running time
        // from about three minutes to less than a second.
        // Forum discussion suggests binary search, which I didn't think of, though it seems obvious
        // in retrospect. It would probably be much faster, but this was fast enough.
        {
          Coord result = null;
          Set<Coord> lastPath = null;
          for (Coord block : coords.subList(part1Max, coords.size())) {
            blocked.add(block);
            if (lastPath != null && !lastPath.contains(block)) {
              continue;
            }
            Map<Coord, Integer> costs = new LinkedHashMap<>();
            traverse(start, end, 0, blocked, costs);
            if (costs.get(end) == null) {
              result = block;
              break;
            }
            lastPath = constructPath(costs, start, end);
          }
          checkNotNull(result);
          System.out.printf("For %s, first blocking coord is %d,%d\n", name, result.x, result.y);
        }
      }
    }
  }

  private static void traverse(
      Coord current, Coord end, int startCost, Set<Coord> blocked, Map<Coord, Integer> costs) {
    if (startCost >= costs.getOrDefault(current, Integer.MAX_VALUE)) {
      return;
    }
    costs.put(current, startCost);
    for (Coord next : current.adjacent()) {
      if (next.x >= 0
          && next.y >= 0
          && next.x <= end.x
          && next.y <= end.y
          && !blocked.contains(next)) {
        traverse(next, end, startCost + 1, blocked, costs);
      }
    }
  }

  private static Set<Coord> constructPath(Map<Coord, Integer> costs, Coord start, Coord end) {
    Set<Coord> path = new LinkedHashSet<>(Set.of(end));
    Integer target = costs.get(end) - 1;
    outer:
    for (Coord c = end; !c.equals(start); ) {
      for (Coord prev : c.adjacent()) {
        if (target.equals(costs.get(prev))) {
          path.add(prev);
          --target;
          c = prev;
          continue outer;
        }
      }
      throw new AssertionError();
    }
    return path;
  }
}
