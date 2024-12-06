package advent2022;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.DiscreteDomain.integers;
import static java.lang.Math.abs;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle15 {
  private static final String SAMPLE =
      """
      Sensor at x=2, y=18: closest beacon is at x=-2, y=15
      Sensor at x=9, y=16: closest beacon is at x=10, y=16
      Sensor at x=13, y=2: closest beacon is at x=15, y=3
      Sensor at x=12, y=14: closest beacon is at x=10, y=16
      Sensor at x=10, y=20: closest beacon is at x=10, y=16
      Sensor at x=14, y=17: closest beacon is at x=10, y=16
      Sensor at x=8, y=7: closest beacon is at x=2, y=10
      Sensor at x=2, y=0: closest beacon is at x=2, y=10
      Sensor at x=0, y=11: closest beacon is at x=2, y=10
      Sensor at x=20, y=14: closest beacon is at x=25, y=17
      Sensor at x=17, y=20: closest beacon is at x=21, y=22
      Sensor at x=16, y=7: closest beacon is at x=15, y=3
      Sensor at x=14, y=3: closest beacon is at x=15, y=3
      Sensor at x=20, y=1: closest beacon is at x=15, y=3
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
        List<Sensor> sensors = parseSensors(lines);
        part1(name, sensors);
        part2(name, sensors);
      }
    }
  }

  // For Part 1: for each sensor, compute how far its Y coordinate is from the target one (dy), and
  // what its Manhattan distance is from its beacon (d). Now if its X coordinate is x then the
  // disallowed values are values x1 such that |x1 - x| + dy ≤ d. Considering values x1 > x, that's
  // x1 - x + dy ≤ d or x1 ≤ d - dy + x, and for values x1 < x, it's
  // x - x1 + dy ≤ d or dy + x ≤ d + x1 or x1 ≥ dy + x - d. So the full range is
  // dy + x - d ≤ x1 ≤ d - dy + x or equivalently x - (d - dy) ≤ x1 ≤ x + (d - dy). This makes sense
  // since d - dy is the number of steps it takes to reach the target row, and if x1 is fewer than
  // the remaining steps to get to d then it is in range.
  // Clearly, too, if d - dy < 0 then the range is empty.
  // Finally, the range could also include the beacon, which should then be excluded.

  private static void part1(String name, List<Sensor> sensors) {
    int targetY =
        switch (name) {
          case "sample" -> 10;
          case "problem" -> 2_000_000;
          default -> throw new AssertionError(name);
        };
    RangeSet<Integer> ranges = impossibleRanges(sensors, targetY);
    for (Sensor sensor : sensors) {
      if (sensor.beaconY == targetY) {
        ranges.remove(Range.closed(sensor.beaconX, sensor.beaconX));
      }
    }
    int size = ranges.asRanges().stream().mapToInt(r -> size(r)).sum();
    System.out.println("Part 1 result for " + name + " is " + size);
  }

  private static void part2(String name, List<Sensor> sensors) {
    int maxCoord =
        switch (name) {
          case "sample" -> 20;
          case "problem" -> 4_000_000;
          default -> throw new AssertionError(name);
        };
    Range<Integer> allX = Range.closed(0, maxCoord);
    record Coord(int x, int y) {}
    Set<Coord> found = new HashSet<>();
    for (int targetY = 0; targetY <= maxCoord; targetY++) {
      RangeSet<Integer> impossible = impossibleRanges(sensors, targetY);
      if (!impossible.encloses(allX)) {
        RangeSet<Integer> remaining = TreeRangeSet.create();
        remaining.add(allX);
        remaining.removeAll(impossible);
        int yy = targetY;
        found.addAll(
            remaining.asRanges().stream()
                .flatMap(r -> ContiguousSet.create(r, integers()).stream())
                .map(x -> new Coord(x, yy))
                .toList());
      }
    }
    System.out.println("For " + name + ", found " + found);
    if (found.size() == 1) {
      Coord coord = Iterables.getOnlyElement(found);
      System.out.println("Tuning frequency " + coord.x * 4_000_000L + coord.y);
    }
  }

  private static RangeSet<Integer> impossibleRanges(List<Sensor> sensors, int targetY) {
    RangeSet<Integer> ranges = TreeRangeSet.create();
    for (Sensor sensor : sensors) {
      int d = abs(sensor.y - sensor.beaconY) + abs(sensor.x - sensor.beaconX);
      int dy = abs(sensor.y - targetY);
      int extraSteps = d - dy;
      if (extraSteps < 0) {
        continue;
      }
      Range<Integer> range = Range.closed(sensor.x - extraSteps, sensor.x + extraSteps);
      ranges.add(range);
    }
    return ranges;
  }

  private static int size(Range<Integer> range) {
    return ContiguousSet.create(range, integers()).size();
  }

  private static final Pattern SENSOR_PATTERN =
      Pattern.compile("Sensor at x=(.*), y=(.*): closest beacon is at x=(.*), y=(.*)");

  private static List<Sensor> parseSensors(List<String> lines) {
    return lines.stream()
        .map(SENSOR_PATTERN::matcher)
        .peek(m -> checkState(m.matches()))
        .map(
            m ->
                new Sensor(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4))))
        .toList();
  }

  record Sensor(int x, int y, int beaconX, int beaconY) {}
}
