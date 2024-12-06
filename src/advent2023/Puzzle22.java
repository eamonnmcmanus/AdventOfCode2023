package advent2023;

import static java.lang.Integer.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toCollection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle22 {
  /*
   * I'm always a bit nervous when I have to deal with 3D, but here it was rather straightforward.
   * The 3D bricks only move vertically, so we just need to know the (x,y) coordinates of each
   * part of the brick and compare those with the bricks below.
   * Total run time about 17 seconds.
   */
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle22.class.getResourceAsStream("puzzle22.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<Brick> bricks = lines.stream().map(line -> parseBrick(line)).toList();
      part1(bricks);
      part2(bricks);
    }
  }

  static void part1(List<Brick> bricks) {
    // Make a copy since we're going to be tweaking the Z-coordinates.
    bricks = bricks.stream().map(Brick::copy).toList();

    Map<Coord, NavigableSet<Brick>> map = computeMap(bricks);

    doFalls(bricks, map);

    int count = 0;
    for (Brick brick : bricks) {
      if (computeDrops(bricks, map, brick).isEmpty()) {
        count++;
      }
    }
    System.out.println("Part 1 " + count);
  }

  static void part2(List<Brick> bricks) {
    // Make a copy since we're going to be tweaking the Z-coordinates.
    bricks = bricks.stream().map(Brick::copy).toList();

    doFalls(bricks, computeMap(bricks));

    long total = 0;
    for (Brick destroy : bricks) {
      List<Brick> otherBricks =
          bricks.stream()
              .filter(b -> b != destroy)
              .map(Brick::copy)
              .collect(toCollection(ArrayList::new));
      while (true) {
        Map<Coord, NavigableSet<Brick>> map = computeMap(otherBricks);
        Map<Brick, Integer> drops = computeDrops(otherBricks, map, null);
        if (drops.isEmpty()) {
          break;
        }
        drops.forEach((b, drop) -> b.drop(drop));
        total += drops.size();
        otherBricks.removeAll(drops.keySet());
      }
    }
    System.out.println("Part 2 " + total);
  }

  static Map<Coord, NavigableSet<Brick>> computeMap(List<Brick> bricks) {
    // For each (x,y), determine a z-ordered list of bricks that are above that point on the ground.
    Map<Coord, NavigableSet<Brick>> map = new TreeMap<>();
    Comparator<Brick> zComparator = Comparator.comparing((Brick brick) -> brick.zEnd);
    for (Brick brick : bricks) {
      for (int x = brick.xStart; x <= brick.xEnd; x++) {
        for (int y = brick.yStart; y <= brick.yEnd; y++) {
          Coord coord = new Coord(x, y);
          map.computeIfAbsent(coord, unused -> new TreeSet<>(zComparator)).add(brick);
        }
      }
    }
    return map;
  }

  static Map<Brick, Integer> computeDrops(
      List<Brick> bricks, Map<Coord, NavigableSet<Brick>> map, Brick ignore) {
    Map<Brick, Integer> drops = new LinkedHashMap<>();
    for (Brick brick : bricks) {
      if (brick == ignore) {
        continue;
      }
      int topZ = 1;
      for (int x = brick.xStart; x <= brick.xEnd; x++) {
        for (int y = brick.yStart; y <= brick.yEnd; y++) {
          Coord coord = new Coord(x, y);
          for (Brick other : map.get(coord).headSet(brick)) {
            if (other != ignore) {
              topZ = max(topZ, other.zEnd + 1);
            }
          }
        }
      }
      if (topZ < brick.zStart) {
        drops.put(brick, brick.zStart - topZ);
      }
    }
    return drops;
  }

  static void doFalls(List<Brick> bricks, Map<Coord, NavigableSet<Brick>> map) {
    while (true) {
      Map<Brick, Integer> drops = computeDrops(bricks, map, null);
      if (drops.isEmpty()) {
        break;
      }
      drops.forEach((brick, drop) -> brick.drop(drop));
    }
  }

  record Coord(int x, int y) implements Comparable<Coord> {
    @Override
    public int compareTo(Coord that) {
      return Comparator.comparingInt(Coord::x).thenComparingInt(Coord::y).compare(this, that);
    }
  }

  static class Brick {
    final int xStart, yStart, xEnd, yEnd;
    int zStart, zEnd;

    Brick(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd) {
      this.xStart = xStart;
      this.yStart = yStart;
      this.zStart = zStart;
      this.xEnd = xEnd;
      this.yEnd = yEnd;
      this.zEnd = zEnd;
    }

    Brick copy() {
      return new Brick(xStart, yStart, zStart, xEnd, yEnd, zEnd);
    }

    void drop(int drop) {
      zStart -= drop;
      zEnd -= drop;
    }

    @Override
    public String toString() {
      return xStart + "," + yStart + "," + zStart + "~" + xEnd + "," + yEnd + "," + zEnd;
    }
  }

  // 2,4,37~3,4,37
  private static final Pattern BRICK_PATTERN =
      Pattern.compile("(\\d+),(\\d+),(\\d+)~(\\d+),(\\d+),(\\d+)");

  static Brick parseBrick(String line) {
    Matcher matcher = BRICK_PATTERN.matcher(line);
    boolean matches = matcher.matches();
    assert matches;
    int[] values =
        IntStream.rangeClosed(1, matcher.groupCount())
            .mapToObj(matcher::group)
            .mapToInt(Integer::parseInt)
            .toArray();
    return new Brick(values[0], values[1], values[2], values[3], values[4], values[5]);
  }
}
