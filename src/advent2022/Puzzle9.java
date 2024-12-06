package advent2022;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle9 {
  private static final String SAMPLE1 =
      """
      R 4
      U 4
      L 3
      D 1
      R 4
      D 1
      L 5
      R 2
      """;

  private static final String SAMPLE2 =
      """
      R 5
      U 8
      L 8
      D 3
      R 17
      D 10
      L 25
      U 20
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample 1", () -> new StringReader(SAMPLE1),
          "sample 2", () -> new StringReader(SAMPLE2),
          "problem", () -> new InputStreamReader(Puzzle9.class.getResourceAsStream("puzzle9.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Move> moves = lines.stream().map(line -> parseMove(line)).toList();

        part1(name, moves);
        part2(name, moves);
      }
    }
  }

  private static void part1(String name, List<Move> moves) {
    Coord head = new Coord(0, 0);
    Coord tail = head;
    Set<Coord> tailCoords = new HashSet<>(Set.of(tail));
    for (Move move : moves) {
      Dir dir = move.dir;
      for (int i = 1; i <= move.amount; i++) {
        head = new Coord(head.x + dir.deltaX, head.y + dir.deltaY);
        tail = newTail(head, tail);
        tailCoords.add(tail);
      }
    }
    System.out.println("Number of Part 1 tail positions for " + name + " is " + tailCoords.size());
  }

  private static void part2(String name, List<Move> moves) {
    Coord[] knots = new Coord[10];
    Arrays.fill(knots, new Coord(0, 0));
    Set<Coord> tailCoords = new HashSet<>();
    for (Move move : moves) {
      Dir dir = move.dir;
      for (int i = 1; i <= move.amount; i++) {
        knots[0] = new Coord(knots[0].x + dir.deltaX, knots[0].y + dir.deltaY);
        for (int k = 1; k <= 9; k++) {
          knots[k] = newTail(knots[k - 1], knots[k]);
        }
        tailCoords.add(knots[9]);
      }
    }
    System.out.println("Number of Part 2 tail positions for " + name + " is " + tailCoords.size());
  }

  private static Coord newTail(Coord head, Coord tail) {
    Coord newTail;
    if (abs(head.x - tail.x) > 1 || abs(head.y - tail.y) > 1) {
      if (head.x == tail.x) {
        if (head.y > tail.y) {
          newTail = new Coord(tail.x, tail.y + 1);
        } else {
          newTail = new Coord(tail.x, tail.y - 1);
        }
      } else if (head.y == tail.y) {
        if (head.x > tail.x) {
          newTail = new Coord(tail.x + 1, tail.y);
        } else {
          newTail = new Coord(tail.x - 1, tail.y);
        }
      } else {
        int deltaX = (head.x > tail.x) ? +1 : -1;
        int deltaY = (head.y > tail.y) ? +1 : -1;
        newTail = new Coord(tail.x + deltaX, tail.y + deltaY);
      }
      assert abs(head.x - newTail.x) <= 1 && abs(head.y - newTail.y) <= 1
          : "head " + head + " tail " + tail + " newTail " + newTail;
    } else {
      newTail = tail;
    }
    return newTail;
  }

  private static final Pattern MOVE_PATTERN = Pattern.compile("([RLUD]) (\\d+)");

  private static Move parseMove(String line) {
    Matcher matcher = MOVE_PATTERN.matcher(line);
    checkState(matcher.matches(), line);
    return new Move(Dir.NAME_TO_DIR.get(matcher.group(1)), Integer.parseInt(matcher.group(2)));
  }

  record Coord(int x, int y) {}

  record Move(Dir dir, int amount) {}

  enum Dir {
    R(+1, 0),
    L(-1, 0),
    U(0, -1),
    D(0, +1);

    static final ImmutableMap<String, Dir> NAME_TO_DIR =
        stream(values()).collect(toImmutableMap(Dir::name, dir -> dir));

    final int deltaX;
    final int deltaY;

    private Dir(int deltaX, int deltaY) {
      this.deltaX = deltaX;
      this.deltaY = deltaY;
    }
  }
}
