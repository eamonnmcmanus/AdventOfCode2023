package advent2022;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Integer.max;
import static java.lang.Integer.min;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
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
        Set<Coord> cubes = lines.stream()
            .map(line -> Splitter.on(',').splitToList(line).stream().map(Integer::parseInt).toList())
            .map(list -> new Coord(list.get(0), list.get(1), list.get(2)))
            .collect(toImmutableSet());
        part1(name, cubes);
        part2(name, cubes);
      }
    }
  }

  // I found this much easier than preceding days. For part 1, all we have to do is to count for
  // each cube how many neighbouring spaces are not occupied by other cubes. For part 2, we can
  // calculate a bounding box for all the cubes, with one extra space in each direction; fill
  // the outside starting from a corner, using DFS; and count for each cube how many neighbouring
  // spaces are in the outside that we just determined. The space is quite small, 22x22x21 = 10164,
  // so we don't need any fancy representations or algorithms. We can just have a set of coordinate
  // objects.

  private static void part1(String name, Set<Coord> cubes) {
    int count = 0;
    for (Coord cube : cubes) {
      count += Sets.difference(cube.neighbours(), cubes).size();
    }
    System.out.println(STR."Faces for \{name}: \{count}");
  }

  private static void part2(String name, Set<Coord> cubes) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    for (Coord cube : cubes) {
      minX = min(minX, cube.x - 1);
      minY = min(minY, cube.y - 1);
      minZ = min(minZ, cube.z - 1);
      maxX = max(maxX, cube.x + 1);
      maxY = max(maxY, cube.y + 1);
      maxZ = max(maxZ, cube.z + 1);
    }
    Coord min = new Coord(minX, minY, minZ);
    Coord max = new Coord(maxX, maxY, maxZ);
    System.out.println(STR."For \{name} span is \{min} to \{max}");
    Filler filler = new Filler(cubes, min, max);
    filler.fill(min);
    Set<Coord> outside = filler.fill;
    long count = 0;
    for (Coord cube : cubes) {
      count += cube.neighbours().stream().filter(outside::contains).count();
    }
    System.out.println(STR."Outside faces for \{name}: \{count}");
  }

  private static class Filler {
    private final Set<Coord> fill = new HashSet<>();
    private final Set<Coord> cubes;
    private final Coord min;
    private final Coord max;

    Filler(Set<Coord> cubes, Coord min, Coord max) {
      this.cubes = cubes;
      this.min = min;
      this.max = max;
    }

    boolean inBounds(Coord coord) {
      return coord.x >= min.x && coord.y >= min.y && coord.z >= min.z
          && coord.x <= max.x && coord.y <= max.y && coord.z <= max.z;
    }

    Set<Coord> fill(Coord start) {
      if (fill.add(start)) {
        for (Coord coord : start.neighbours()) {
          if (inBounds(coord) && !cubes.contains(coord)) {
            fill(coord);
          }
        }
      }
      return fill;
    }
  }

  record Coord(int x, int y, int z) {
    @Override public String toString() {
      return STR."(\{x},\{y},\{z})";
    }

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
