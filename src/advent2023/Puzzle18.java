package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toCollection;

import com.google.common.collect.ImmutableMap;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle18 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle18.class.getResourceAsStream("puzzle18.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<Step> steps = lines.stream()
          .map(LINE_PATTERN::matcher)
          .peek(m -> {
            if (!m.matches()) {
              throw new AssertionError(m);
            }
          }).
          map(m -> new Step(
              NAME_TO_DIR.get(m.group(1)),
              Integer.parseInt(m.group(2)),
              Integer.parseInt(m.group(3), 16)))
          .toList();
      Point point = new Point(0, 0, 0);
      Set<Point> points = new TreeSet<>(Set.of(point));
      for (Step step : steps) {
        for (int i = 0; i < step.n; i++) {
          point = point.moved(step.dir).withColour(step.colour);
          points.add(point);
        }
      }
      int minX = points.stream().mapToInt(Point::x).min().getAsInt();
      int minY = points.stream().mapToInt(Point::y).min().getAsInt();
      points = points.stream().map(q -> q.shift(-minX, -minY)).collect(toCollection(TreeSet::new));
      int maxX = points.stream().mapToInt(Point::x).max().getAsInt();
      int maxY = points.stream().mapToInt(Point::y).max().getAsInt();
      char[][] grid = new char[maxY + 1][maxX + 1];
      for (char[] line : grid) {
        Arrays.fill(line, '.');
      }
      for (Point p : points) {
        grid[p.y][p.x] = '#';
      }
      // By construction we know there is at least one edge with y=0. We find a cell with y=1 that
      // is empty and below a full cell with y=0. (Not completely accurate, but works for our
      // example cases.) Then we fill starting from there.
      int foundX = -1;
      for (int x = 0; x < grid[0].length; x++) {
        if (grid[0][x] == '#' && grid[1][x] == '.') {
          foundX = x;
          break;
        }
      }
      assert foundX > 0;
      Deque<Point> queue = new ArrayDeque<>(List.of(new Point(foundX, 1, 0)));
      while (!queue.isEmpty()) {
        Point p = queue.remove();
        if (grid[p.y][p.x] == '.') {
          grid[p.y][p.x] = '#';
          if (p.x > 0) {
            queue.add(p.moved(Dir.LEFT));
          }
          if (p.x + 1 < grid[0].length) {
            queue.add(p.moved(Dir.RIGHT));
          }
          if (p.y > 0) {
            queue.add(p.moved(Dir.UP));
          }
          if (p.y + 1 < grid.length) {
            queue.add(p.moved(Dir.DOWN));
          }
        }
      }
      for (char[] line : grid) {
        System.out.println(new String(line));
      }
      int count = 0;
      for (char[] line : grid) {
        count += new String(line).chars().filter(c -> c == '#').count();
      }
      System.out.println(STR."Filled cells \{count}");
    }
  }

  record Point(int x, int y, int colour) implements Comparable<Point> {
    private static final Comparator<Point> COMPARATOR =
        Comparator.comparingInt(Point::y).thenComparingInt(Point::x);

    Point moved(Dir dir) {
      return new Point(x + dir.deltaX, y + dir.deltaY, colour);
    }

    Point shift(int deltaX, int deltaY) {
      return new Point(x + deltaX, y + deltaY, colour);
    }

    Point withColour(int newColour) {
      return new Point(x, y, newColour);
    }

    @Override
    public int compareTo(Point that) {
      return COMPARATOR.compare(this, that);
    }
  }

  private static final Pattern LINE_PATTERN = Pattern.compile("([LRUD]) ([0-9]+) \\(#([0-9a-f]{6})\\)");

  record Step(Dir dir, int n, int colour) {}

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
