package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle18 {
  // For Part 1, I initially just made a char[][] containing the path, after shifting coordinates
  // so they started at (0,0). (My input map, at least, included coordinates in all four quadrants.)
  // Then I found the top left corner of the path and used a DFS to fill cells starting from the
  // one just down and right of that corner. Obviously that doesn't scale to the giant path, though.
  //
  // I made an amusing mistake when switching to Part 2. It didn't seem necessary to shift the
  // coordinates anymore, so I left them as-is. But then I used -1 as the `start` value when we are
  // not inside the path! Oops. That's now `Integer start = null` below.

  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle18.class.getResourceAsStream("puzzle18.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      for (boolean part2 : new boolean[] {false, true}) {
        List<Step> steps = lines.stream()
            .map(LINE_PATTERN::matcher)
            .peek(m -> {
              if (!m.matches()) {
                throw new AssertionError(m);
              }
            }).
            map(m -> part2
                ? Step.fromHex(Integer.parseInt(m.group(3), 16))
                : new Step(
                    NAME_TO_DIR.get(m.group(1)),
                    Integer.parseInt(m.group(2))))
            .toList();
        solve(steps);
      }
    }
  }

  private static void solve(List<Step> steps) {
    // We calculate the internal area using the familiar notion that if you scan across from the
    // edge then every line you cross switches you between outside and inside.
    // We construct a set of vertical lines and a set of horizontal lines. Each vertical line will
    // have a horizontal line meeting it at each of its endpoints. For each y coordinate, visit
    // the vertical lines that pass through that coordinate, in increasing x order. If the middle
    // of the line passes through that y, then we switch between inside and outside. If it is an
    // endpoint, then two cases: (1) the horizontal line joins it from the left: do nothing; (2)
    // the horizontal line joins it from the right: continue from the other end of the horizontal
    // line. In case (2), if the vertical line at the other end joins in the *same direction* as
    // the vertical line at the near end, then insideness is unchanged, otherwise it flips.
    // This is still massively inefficient in that we consider each y coordinate. We could jump to
    // the next y coordinate where something changes, and multiply to fill in the gap. But this is
    // good enough.
    // As with Puzzle 10, the suggestion from @lowasser of using
    // https://en.wikipedia.org/wiki/Shoelace_formula would be much simpler. Here we almost
    // literally have the coordinates of the vertices of a polygon so we can just plug those into
    // the formula. Because the path has thickness, there's a little extra complexity but not much.
    Point point = new Point(0, 0);
    Map<Point, VLine> upLines = new TreeMap<>();    // line going up from the given point
    Map<Point, VLine> downLines = new TreeMap<>();  // line going down from the given point
    Map<Point, HLine> rightLines = new TreeMap<>(); // line going right from the given point
    for (Step step : steps) {
      Point newPoint = switch (step.dir) {
        case UP -> {
          Point p = new Point(point.x, point.y - step.n);
          VLine line = new VLine(point.x, p.y, point.y);
          upLines.put(point, line);
          downLines.put(p, line);
          yield p;
        }
        case DOWN -> {
          Point p = new Point(point.x, point.y + step.n);
          VLine line = new VLine(point.x, point.y, p.y);
          upLines.put(p, line);
          downLines.put(point, line);
          yield p;
        }
        case LEFT -> {
          Point p = new Point(point.x - step.n, point.y);
          HLine line = new HLine(p.x, point.x);
          rightLines.put(p, line);
          yield p;
        }
        case RIGHT -> {
          Point p = new Point(point.x + step.n, point.y);
          HLine line = new HLine(point.x, p.x);
          rightLines.put(point, line);
          yield p;
        }
      };
      point = newPoint;
    }
    int minY = rightLines.keySet().stream().mapToInt(Point::y).min().getAsInt();
    int maxY = rightLines.keySet().stream().mapToInt(Point::y).max().getAsInt();
    System.out.println(STR."\{minY} < y < \{maxY}");
    long count = 0;
    Comparator<VLine> xFirst = Comparator.comparing(VLine::x);
    for (int y = minY; y <= maxY; y++) {
      long thisCount = 0;
      int yy = y;
      List<VLine> vLinesAtY = downLines.values().stream()
          .filter(line -> line.includes(yy))
          .sorted(xFirst)
          .toList();
      assert !vLinesAtY.isEmpty();
      Integer start = null;
      for (int i = 0; i < vLinesAtY.size(); i++) {
        // If start, we were in an inside area. Now if we are crossing the middle of a vertical
        // line, we count the width to here and set start to null. But if this is the end of a vertical line,
        // we still count its width to here, but the rest depends on whether we are exiting the inside
        // (second vertical line going in opposite direction), or remaining in it (second vertical
        // line going in same direction). If remaining, we should set start to just after the second
        // vertical line. If leaving, we should set start to null.
        // If start is null, we were in an outside area. If we are crossing the middle of a vertical
        // line, we are now inside, and should set start to just after the line. If this is the end
        // of a vertical line, then we skip to other vertical line. If it is going in the same
        // direction, we are still outside; otherwise we are now inside starting just after the
        // second line.
        VLine vLine = vLinesAtY.get(i);
        Point p = new Point(vLine.x, y);
        LineState lineState = lineState(p, downLines, upLines);
        if (lineState == LineState.MID) {
          thisCount++;
          if (start != null) {
            thisCount += vLine.x - start;
            start = null;
          } else {
            start = vLine.x + 1;
          }
        } else {
          HLine hLine = rightLines.get(p);
          assert hLine != null;
          thisCount += hLine.xEnd - hLine.xStart + 1;
          Point otherEnd = new Point(hLine.xEnd, y);
          i++;
          LineState otherLineState = lineState(otherEnd, downLines, upLines);
          assert otherLineState != LineState.MID;
          if (lineState == otherLineState) {
            // Same direction, no effect on insideness. If we are inside, count the span up to
            // the first line and change the start to after the second line.
            if (start != null) {
              thisCount += vLine.x - start;
              start = otherEnd.x + 1;
            }
          } else {
            // Opposite direction, switching insideness. If we were inside, count the span up to
            // the first line and move outside. Otherwise, set start to after the second line.
            if (start != null) {
              thisCount += vLine.x - start;
              start = null;
            } else {
              start = otherEnd.x + 1;
            }
          }
        }
      }
      count += thisCount;
    }
    System.out.println(STR."Filled cells \{count}");
  }

  enum LineState {UP, DOWN, MID};

  static LineState lineState(Point p, Map<Point, VLine> downLines, Map<Point, VLine> upLines) {
    return downLines.containsKey(p)
        ? LineState.DOWN
        : upLines.containsKey(p)
        ? LineState.UP : LineState.MID;
  }

  record Point(int x, int y) implements Comparable<Point> {
    private static final Comparator<Point> COMPARATOR =
        Comparator.comparingInt(Point::y).thenComparingInt(Point::x);

    Point moved(Dir dir) {
      return new Point(x + dir.deltaX, y + dir.deltaY);
    }

    Point shift(int deltaX, int deltaY) {
      return new Point(x + deltaX, y + deltaY);
    }

    @Override
    public int compareTo(Point that) {
      return COMPARATOR.compare(this, that);
    }
  }

  record HLine(int xStart, int xEnd) {
    HLine {
      assert xStart < xEnd;
    }
  }

  record VLine(int x, int yStart, int yEnd) {
    VLine {
      assert yStart < yEnd;
    }

    boolean includes(int y) {
      return yStart <= y && y <= yEnd;
    }
  }

  private static final Pattern LINE_PATTERN = Pattern.compile("([LRUD]) ([0-9]+) \\(#([0-9a-f]{6})\\)");

  record Step(Dir dir, int n) {
    static Step fromHex(int hex) {
      int n = hex >> 4;
      Dir dir = switch (hex & 15) {
        case 0 -> Dir.RIGHT;
        case 1 -> Dir.DOWN;
        case 2 -> Dir.LEFT;
        case 3 -> Dir.UP;
        default -> throw new AssertionError(hex & 15);
      };
      return new Step(dir, n);
    }
  }

  private static final ImmutableMap<String, Dir> NAME_TO_DIR =
      ImmutableMap.of("L", Dir.LEFT, "R", Dir.RIGHT, "U", Dir.UP, "D", Dir.DOWN);

  enum Dir {
    LEFT(-1, 0), RIGHT(+1, 0), UP(0, -1), DOWN(0, +1);

    private final int deltaX;
    private final int deltaY;

    private Dir(int deltaX, int deltaY) {
      this.deltaX = deltaX;
      this.deltaY = deltaY;
    }
  }
}
