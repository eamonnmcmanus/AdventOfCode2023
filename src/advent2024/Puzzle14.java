package advent2024;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.common.math.IntMath.mod;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multiset;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle14 {
  private static final String SAMPLE =
      """
      p=0,4 v=3,-3
      p=6,3 v=-1,-3
      p=10,3 v=-1,2
      p=2,0 v=2,-1
      p=0,0 v=1,3
      p=3,0 v=-2,-2
      p=7,6 v=-1,-3
      p=3,0 v=-1,-2
      p=9,3 v=2,3
      p=7,3 v=-1,2
      p=2,4 v=2,-3
      p=9,5 v=-3,-3
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle14.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        final ImmutableSetMultimap<Coord, Robot> robots = parseRobots(lines);
        Coord bounds =
            switch (name) {
              case "sample" -> new Coord(11, 7);
              case "problem" -> new Coord(101, 103);
              default -> throw new IllegalArgumentException(name);
            };
        robots.keySet().forEach(c -> checkState(c.valid(bounds), "%s ≥ %s", c, bounds));
        var movedRobots = move(robots, 100, bounds);
        System.out.printf("For %s, safety factor %d\n", name, safetyFactor(movedRobots, bounds));
        if (name.equals("problem")) {
          // Some guesswork involved here. I first displayed grids for the first 1000 steps and
          // looked through them all. No Christmas tree, but I did see that some vertical clusters
          // seemed to be forming. For my input, that happened at step 14, and again every 101
          // steps after that. So I thought I would look for vertical clusters, and tried various
          // thresholds. A threshold of 35 found the solution at step 6377 = 14 + 63 * 101. At
          // first I looked just at 14, 115, 216, etc, but actually we don't need to rely on the
          // cycling. We can just look at all arrangements, since 6377 isn't all that big.
          for (int i = 1; i <= 100_000; i++) {
            movedRobots = move(robots, i, bounds);
            if (verticalLine(movedRobots.keySet())) {
              System.out.printf("Pattern after %d steps\n", i);
              // display(movedRobots.keySet(), bounds);
              break;
            }
          }
        }
      }
    }
  }

  private static boolean verticalLine(ImmutableSet<Coord> coords) {
    return coords.stream().collect(groupingBy(Coord::x)).values().stream()
            .mapToInt(List::size)
            .max()
            .getAsInt()
        > 35;
  }

  private static void display(ImmutableSet<Coord> coords, Coord bounds) {
    for (int y = 0; y < bounds.y; y++) {
      for (int x = 0; x < bounds.x; x++) {
        System.out.print(coords.contains(new Coord(x, y)) ? '#' : '.');
      }
      System.out.println();
    }
  }

  private static long safetyFactor(ImmutableSetMultimap<Coord, Robot> robots, Coord bounds) {
    record Quadrant(boolean right, boolean lower) {}
    int halfX = bounds.x / 2;
    int halfY = bounds.y / 2;
    Predicate<Coord> halfway = c -> c.x == halfX || c.y == halfY;
    Multiset<Quadrant> quadrants =
        robots.entries().stream()
            .map(Map.Entry::getKey)
            .filter(halfway.negate()::test)
            .map(c -> new Quadrant(c.x > halfX, c.y > halfY))
            .collect(toImmutableMultiset());
    return quadrants.entrySet().stream()
        .map(Multiset.Entry::getCount)
        .reduce(1, Math::multiplyExact);
  }

  private static ImmutableSetMultimap<Coord, Robot> move(
      ImmutableSetMultimap<Coord, Robot> robots, int steps, Coord bounds) {
    return robots.entries().stream()
        .map(e -> entry(e.getValue().move(e.getKey(), steps, bounds), e.getValue()))
        .collect(toImmutableSetMultimap(e -> e.getKey(), e -> e.getValue()));
  }

  record Coord(int x, int y) {
    boolean valid(Coord bounds) {
      return x >= 0 && y >= 0 && x < bounds.x && y < bounds.y;
    }

    Coord reduce(Coord bounds) {
      return valid(bounds) ? this : new Coord(mod(x, bounds.x), mod(y, bounds.y));
    }

    Coord plus(Coord vector, Coord bounds) {
      return new Coord(addExact(x, vector.x), addExact(y, vector.y)).reduce(bounds);
    }
  }

  record Robot(Coord velocity) {
    Coord move(Coord start, int steps, Coord bounds) {
      Coord vector = new Coord(multiplyExact(velocity.x, steps), multiplyExact(velocity.y, steps));
      return start.plus(vector, bounds);
    }
  }

  private static ImmutableSetMultimap<Coord, Robot> parseRobots(List<String> lines) {
    Pattern pattern = Pattern.compile("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)");
    return lines.stream()
        .map(pattern::matcher)
        .peek(m -> checkState(m.matches()))
        .map(
            m ->
                IntStream.rangeClosed(1, m.groupCount())
                    .map(i -> Integer.parseInt(m.group(i)))
                    .toArray())
        .collect(
            toImmutableSetMultimap(
                a -> new Coord(a[0], a[1]), a -> new Robot(new Coord(a[2], a[3]))));
  }
}