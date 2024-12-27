package advent2021;

import static java.util.stream.Collectors.joining;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle15 {
  private static final String SAMPLE =
      """
      1163751742
      1381373672
      2136511328
      3694931569
      7463417111
      1319128137
      1359912421
      3125421639
      1293138521
      2311944581
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle15.class.getResourceAsStream("puzzle15.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        CharGrid grid = new CharGrid(lines);
        System.out.printf(
            "For %s small grid, lowest total risk is %d\n", name, computeBestCost(grid));
        CharGrid expandedGrid = expandedGrid(lines);
        System.out.printf(
            "For %s large grid, lowest total risk is %d\n", name, computeBestCost(expandedGrid));
      }
    }
  }

  private static int computeBestCost(CharGrid grid) {
    Map<Coord, Integer> bestCost = new LinkedHashMap<>();
    Coord origin = new Coord(0, 0);
    record CoordCost(Coord coord, int cost) {}
    PriorityQueue<CoordCost> toVisit = new PriorityQueue<>(Comparator.comparing(CoordCost::cost));
    toVisit.add(new CoordCost(origin, 0));
    while (!toVisit.isEmpty()) {
      CoordCost coordCost = toVisit.remove();
      if (coordCost.cost < bestCost.getOrDefault(coordCost.coord, Integer.MAX_VALUE)) {
        bestCost.put(coordCost.coord, coordCost.cost);
        for (Dir dir : Dir.NEWS) {
          Coord next = dir.move(coordCost.coord);
          if (grid.valid(next)) {
            int nextCost = coordCost.cost + grid.get(next) - '0';
            toVisit.add(new CoordCost(next, nextCost));
          }
        }
      }
    }
    return bestCost.get(new Coord(grid.height() - 1, grid.width() - 1));
  }

  private static CharGrid expandedGrid(List<String> lines) {
    List<String> newLines = new ArrayList<>();
    for (String line : lines) {
      newLines.add(expandHorizontally(line));
    }
    for (int i = 0; i < 4 * lines.size(); i++) {
      newLines.add(increment(newLines.get(i)));
    }
    return new CharGrid(newLines);
  }

  private static String expandHorizontally(String line) {
    StringBuilder newLine = new StringBuilder(line);
    for (int i = 0; i < 4 * line.length(); i++) {
      newLine.append(increment(newLine.charAt(i)));
    }
    return newLine.toString();
  }

  private static String increment(String line) {
    return line.chars().mapToObj(c -> Character.toString(increment((char) c))).collect(joining());
  }

  private static char increment(char c) {
    return (c == '9') ? '1' : (char) (c + 1);
  }
}