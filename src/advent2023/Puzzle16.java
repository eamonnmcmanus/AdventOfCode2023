package advent2023;

import static java.lang.Integer.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toCollection;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Éamonn McManus
 */
public class Puzzle16 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle16.class.getResourceAsStream("puzzle16.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      int tiles = solve(new Beam(0, 0, Dir.RIGHT), lines);
      System.out.println(STR."Total \{tiles}");

      int best = -1;
      for (int x = 0; x < lines.get(0).length(); x++) {
        int t1 = solve(new Beam(x, 0, Dir.DOWN), lines);
        best = max(best, t1);
        int t2 = solve(new Beam(x, lines.size() - 1, Dir.UP), lines);
        best = max(best, t2);
      }
      for (int y = 0; y < lines.size(); y++) {
        int t1 = solve(new Beam(0, y, Dir.RIGHT), lines);
        best = max(best, t1);
        int t2 = solve(new Beam(lines.get(0).length() - 1, y, Dir.LEFT), lines);
        best = max(best, t2);
      }
      System.out.println(STR."Best total \{best}");
    }
  }

  private static int solve(Beam startBeam, List<String> lines) {
    Set<Beam> beams = new HashSet<>();
    Deque<Beam> queue = new ArrayDeque<>();
    queue.add(startBeam);
    while (!queue.isEmpty()) {
      Beam beam = queue.remove();
      if (!beams.add(beam)) {
        continue;
      }
      queue.addAll(advance(beam, lines));
    }
    Set<Coord> tiles = beams.stream().map(b -> new Coord(b.x, b.y)).collect(toCollection(TreeSet::new));

    return tiles.size();
  }

  private static List<Beam> advance(Beam beam, List<String> lines) {
    char c = lines.get(beam.y).charAt(beam.x);
    List<Beam> beams = switch (c) {
      case '.' -> List.of(beam.advance());
      case '-' -> {
        if (beam.dir == Dir.LEFT || beam.dir == Dir.RIGHT) {
          yield List.of(beam.advance());
        } else {
          yield List.of(beam.moveLeft(), beam.moveRight());
        }
      }
      case '|' -> {
        if (beam.dir == Dir.UP || beam.dir == Dir.DOWN) {
          yield List.of(beam.advance());
        } else {
          yield List.of(beam.moveUp(), beam.moveDown());
        }
      }
      case '/' -> List.of(
          switch (beam.dir) {
            case LEFT -> beam.moveDown();
            case RIGHT -> beam.moveUp();
            case UP -> beam.moveRight();
            case DOWN -> beam.moveLeft();
          });
      case '\\' -> List.of(
          switch (beam.dir) {
            case LEFT -> beam.moveUp();
            case RIGHT -> beam.moveDown();
            case UP -> beam.moveLeft();
            case DOWN -> beam.moveRight();
          });
      default -> throw new AssertionError(c);
    };
    return beams.stream()
        .filter(b -> b.x >= 0 && b.x < lines.get(0).length() && b.y >= 0 && b.y < lines.size())
        .toList();
  }

  record Beam(int x, int y, Dir dir) {
    Beam advance() {
      return new Beam(x + dir.deltaX, y + dir.deltaY, dir);
    }

    Beam moveUp() {
      return new Beam(x, y - 1, Dir.UP);
    }

    Beam moveDown() {
      return new Beam(x, y + 1, Dir.DOWN);
    }

    Beam moveLeft() {
      return new Beam(x - 1, y, Dir.LEFT);
    }

    Beam moveRight() {
      return new Beam(x + 1, y, Dir.RIGHT);
    }
  }

  record Coord(int x, int y) implements Comparable<Coord> {
    private static final Comparator<Coord> COMPARATOR = Comparator.comparing(Coord::y).thenComparing(Coord::x);

    @Override
    public int compareTo(Coord that) {
      return COMPARATOR.compare(this, that);
    }
  }

  enum Dir {
    LEFT(-1, 0), RIGHT(+1, 0), UP(0, -1), DOWN(0, +1);

    final int deltaX;
    final int deltaY;

    private Dir(int deltaX, int deltaY) {
      this.deltaX = deltaX;
      this.deltaY = deltaY;
    }
  }
}
