package advent2019;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.abs;

import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle3 {
  private static final String SAMPLE1 =
      """
      R8,U5,L5,D3
      U7,R6,D4,L4
      """;

  private static final String SAMPLE2 =
      """
      R75,D30,R83,U83,L12,D49,R71,U7,L72
      U62,R66,U55,R34,D71,R55,D58,R83
      """;

  private static final String SAMPLE3 =
      """
      R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51
      U98,R91,D20,R16,D67,R40,U7,R15,U6,R7
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample 1", () -> new StringReader(SAMPLE1),
          "sample 2", () -> new StringReader(SAMPLE2),
          "sample 3", () -> new StringReader(SAMPLE3),
          "problem", () -> new InputStreamReader(Puzzle3.class.getResourceAsStream("puzzle3.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        checkState(lines.size() == 2);
        Map<Coord, Integer> coords1 = wireCoords(lines.get(0));
        Map<Coord, Integer> coords2 = wireCoords(lines.get(1));
        int nearest =
            Sets.intersection(coords1.keySet(), coords2.keySet()).stream()
                .map(c -> abs(c.line()) + abs(c.col()))
                .sorted()
                .findFirst()
                .get();
        System.out.printf("For %s, nearest is %d\n", name, nearest);
        int shortest =
            Sets.intersection(coords1.keySet(), coords2.keySet()).stream()
                .map(c -> coords1.get(c) + coords2.get(c))
                .sorted()
                .findFirst()
                .get();
        System.out.printf("For %s, shortest is %d\n", name, shortest);
      }
    }
  }

  private static Map<Coord, Integer> wireCoords(String line) {
    List<String> parts = Splitter.on(',').splitToList(line);
    Map<Coord, Integer> coords = new LinkedHashMap<>();
    Coord coord = new Coord(0, 0);
    int step = 0;
    for (String part : parts) {
      Dir dir =
          switch (part.charAt(0)) {
            case 'L' -> Dir.W;
            case 'R' -> Dir.E;
            case 'U' -> Dir.N;
            case 'D' -> Dir.S;
            default -> throw new AssertionError(part);
          };
      int size = Integer.parseInt(part.substring(1));
      for (int i = 0; i < size; i++) {
        coord = dir.move(coord);
        ++step;
        coords.putIfAbsent(coord, step);
      }
    }
    coords.remove(new Coord(0, 0));
    return coords;
  }
}