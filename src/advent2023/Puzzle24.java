package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle24 {
  public static void main(String[] args) throws Exception {
    String input = "puzzle24.txt";
    try (InputStream in = Puzzle24.class.getResourceAsStream(input)) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<Hailstone> hailstones = lines.stream().map(line -> parseHailstone(line)).toList();
      boolean small = input.contains("small");
      Bounds bounds = small ? new Bounds(7, 27) : new Bounds(200000000000000L, 400000000000000L);
      solve(hailstones, bounds);
    }
  }

  static void solve(List<Hailstone> hailstones, Bounds bounds) {
    int count = 0;
    for (int i = 0; i < hailstones.size(); i++) {
      Hailstone h1 = hailstones.get(i);
      for (int j = i + 1; j < hailstones.size(); j++) {
        Hailstone h2 = hailstones.get(j);
        Intersection intersection = intersection(h1, h2);
        if (intersection.t1 > 0 && intersection.t2 > 0 && bounds.inside(intersection.x, intersection.y)) {
          count++;
        }
      }
    }
    System.out.println(STR."Count is \{count}");
  }


  /*
  This is basically algebra. If we interpret
  19, 13, 30 @ -2,  1, -2
  as hailstone 1, we can say x1=19, y1=13, dx1=-2, dy1=1. Then given hailstones 1 and 2, we are
  looking for times t1 and t2 such that
  x1 + t1*dx1 = x2 + t2*dx2  and
  y1 + t1*dy1 = y2 + t2*dy2.

  Rearranging the first,
  t1*dx1 = x2 - x1 + t2*dx2
  t1 = (x2 - x1 + t2*dx2) / dx1.

  Substituting into the y equation,
  y1 + [(x2 - x1 + t2*dx2) / dx1] * dy1 = y2 + t2*dy2
  y1 - y2 + (x2 - x1)*(dy1/dx1) + t2*dx2*dy1/dx1 = t2*dy2
  t2*dy2 = y1 - y2 + (x2 - x1)*(dy1/dx1) + t2*dx2*dy1/dx1
  t2 * [dy2 - dx2*dy1/dx1] = y1 - y2 + (x2 - x1)*(dy1/dx1)
  t2 = [y1 - y2 + (x2 - x1)*(dy1/dx1)] / [dy2 - dx2*dy1/dx1]

  Remarkably, this algebra appears to be right first time.
  */
  static Intersection intersection(Hailstone h1, Hailstone h2) {
    double x1 = h1.startX;
    double y1 = h1.startY;
    double dx1 = h1.deltaX;
    double dy1 = h1.deltaY;
    double x2 = h2.startX;
    double y2 = h2.startY;
    double dx2 = h2.deltaX;
    double dy2 = h2.deltaY;
    double slope1 = dy1 / dx1;
    double t2 = (y1 - y2 + (x2 - x1) * slope1) / (dy2 - dx2 * slope1);
    double t1 = (x2 - x1 + t2*dx2) / dx1;
    double x = x1 + t1 * dx1;
    double y = y1 + t1 * dy1;
    double otherX = x2 + t2 * dx2;
    double otherY = y2 + t2 * dy2;
    return new Intersection(t1, t2, x, y);
  }

  record Intersection(double t1, double t2, double x, double y) {}

  record Bounds(long low, long high) {
    boolean inside(double x, double y) {
      return low <= x && x <= high && low <= y && y <= high;
    }
  }

  private static final Pattern HAILSTONE_PATTERN =
      Pattern.compile("(\\d+),\\s+(\\d+),\\s+(\\d+)\\s+@\\s+(-?\\d+),\\s+(-?\\d+),\\s+(-?\\d+)");

  static Hailstone parseHailstone(String line) {
    Matcher matcher = HAILSTONE_PATTERN.matcher(line);
    boolean matches = matcher.matches();
    assert matches : line;
    long[] group = groups(matcher).stream().mapToLong(Long::parseLong).toArray();
    return new Hailstone(group[0], group[1], group[2], group[3], group[4], group[5]);
  }

  static List<String> groups(Matcher matcher) {
    return IntStream.rangeClosed(1, matcher.groupCount()).mapToObj(matcher::group).toList();
  }

  record Hailstone(long startX, long startY, long startZ, long deltaX, long deltaY, long deltaZ) {}
}
/*
19, 13, 30 @ -2,  1, -2
18, 19, 22 @ -1, -1, -2
20, 25, 34 @ -2, -2, -4
12, 31, 28 @ -1, -2, -1
20, 19, 15 @  1, -5, -3
*/
