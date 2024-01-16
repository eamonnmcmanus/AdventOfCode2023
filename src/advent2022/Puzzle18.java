package advent2022;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle18 {
  private static final String SAMPLE =
      """
      2,2,2
      1,2,2
      3,2,2
      2,1,2
      2,3,2
      2,2,1
      2,2,3
      2,2,4
      2,2,6
      1,2,5
      3,2,5
      2,1,5
      2,3,5
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle18.class.getResourceAsStream("puzzle18.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Set<Coord> coords = lines.stream()
            .map(line -> Splitter.on(',').splitToList(line).stream().map(Integer::parseInt).toList())
            .map(list -> new Coord(list.get(0), list.get(1), list.get(2)))
            .collect(toImmutableSet());
        int count = 0;
        for (Coord cube : coords) {
          count += Sets.difference(cube.neighbours(), coords).size();
        }
        System.out.println(STR."Faces for \{name}: \{count}");
      }
    }
  }

  record Coord(int x, int y, int z) {
    Set<Coord> neighbours() {
      return Set.of(
          new Coord(x - 1, y, z),
          new Coord(x + 1, y, z),
          new Coord(x, y - 1, z),
          new Coord(x, y + 1, z),
          new Coord(x, y, z - 1),
          new Coord(x, y, z + 1));
    }
  }
}
