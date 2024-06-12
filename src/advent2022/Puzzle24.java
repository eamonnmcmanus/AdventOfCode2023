package advent2022;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.math.IntMath;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle24 {
  private static final String SAMPLE =
      """
      #.######
      #>>.<^<#
      #.<..<<#
      #>v.><>#
      #<^v^^>#
      ######.#
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle24.class.getResourceAsStream("puzzle24.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        BlizzardMap blizzardMap = parseBlizzardMap(lines);
        Coord start = new Coord(0, -1);
        Coord end = new Coord(blizzardMap.maxX - 1, blizzardMap.maxY);
        var stepsPart1 = steps(blizzardMap, start, end);
        System.out.println("Part 1 steps for " + name + ": " + stepsPart1.steps);
        var stepsPart2a = steps(stepsPart1.blizzardMap, end, start);
        System.out.println("Part 2a steps for " + name + ": " + stepsPart2a.steps);
        var stepsPart2b = steps(stepsPart2a.blizzardMap, start, end);
        System.out.println("Part 2b steps for " + name + ": " + stepsPart2b.steps);
        System.out.println("Total part 2 steps for " + name + ": " + stepsPart1.steps + stepsPart2a.steps + stepsPart2b.steps);
      }
    }
  }

  // A fairly simple solution with Dynamic Programming. (Well, simple once I figured it out.)
  // The idea is to consider every possible position we can be in after n steps, and also compute
  // the positions of the blizzards at that point. Then for step n+1, each of our positions
  // leads to a new possible position in a given direction only if the new position is in bounds and
  // there is no blizzard there. Each set of all our possible positions is small since it is less
  // than the size of the grid, 100×35 in the problem, and actually much smaller than that since
  // most of the grid is filled with blizzards. The whole thing runs in less than a second.

  record StepsAndBlizzardMap(int steps, BlizzardMap blizzardMap) {}

  private static StepsAndBlizzardMap steps(BlizzardMap blizzardMap, Coord start, Coord end) {
    int steps = 0;
    ImmutableSet<Coord> possible = ImmutableSet.of(start);
    while (!possible.contains(end)) {
      steps++;
      blizzardMap = blizzardMap.step();
      ImmutableSet.Builder<Coord> newPossible = ImmutableSet.builder();
      for (Coord coord : possible) {
        if (blizzardMap.isFree(coord)) {
          newPossible.add(coord); // standing still
        }
        for (Dir dir : Dir.ALL) {
          Coord newCoord = coord.step(dir);
          if (blizzardMap.isFree(newCoord)) {
            newPossible.add(newCoord);
          }
        }
      }
      possible = newPossible.build();
    }
    return new StepsAndBlizzardMap(steps, blizzardMap);
  }

  private static BlizzardMap parseBlizzardMap(List<String> lines) {
    assert lines.getFirst().charAt(1) == '.';
    assert lines.getLast().charAt(lines.getLast().length() - 2) == '.';
    int maxX = lines.getFirst().length() - 2;
    int maxY = lines.size() - 2;
    assert lines.stream().allMatch(line -> line.length() == maxX + 2);
    ImmutableList.Builder<Blizzard> blizzards = ImmutableList.builder();
    for (int y = 0; y < maxY; y++) {
      String line = lines.get(y + 1);
      for (int x = 0; x < maxX; x++) {
        char c = line.charAt(x + 1);
        if (c != '.') {
          blizzards.add(new Blizzard(new Coord(x, y), Dir.of(c)));
        }
      }
    }
    return new BlizzardMap(blizzards.build(), maxX, maxY);
  }

  private static class BlizzardMap {
    final ImmutableList<Blizzard> blizzards;
    final int maxX;
    final int maxY;
    final boolean[][] occupied;

    BlizzardMap(ImmutableList<Blizzard> blizzards, int maxX, int maxY) {
      this.blizzards = blizzards;
      this.maxX = maxX;
      this.maxY = maxY;
      this.occupied = new boolean[maxX][maxY];
      for (Blizzard b : blizzards) {
        occupied[b.coord.x][b.coord.y] = true;
      }
    }

    boolean isFree(Coord coord) {
      return switch (coord) {
        case Coord(int x, int y) when y == -1 -> x == 0;  // entrance
        case Coord(int x, int y) when y == maxY -> x == maxX - 1;  // exit
        case Coord(int x, int y) -> x >= 0 && y >= 0 && x < maxX && y < maxY && !occupied[x][y];
      };
    }

    BlizzardMap step() {
      ImmutableList<Blizzard> newBlizzards = blizzards.stream()
          .map(b -> b.step(maxX, maxY))
          .collect(toImmutableList());
      return new BlizzardMap(newBlizzards, maxX, maxY);
    }
  }

  record Blizzard(Coord coord, Dir dir) {
    Blizzard step(int maxX, int maxY) {
      Coord newCoord = coord.step(dir);
      Coord wrapped = new Coord(IntMath.mod(newCoord.x, maxX), IntMath.mod(newCoord.y, maxY));
      return new Blizzard(wrapped, dir);
    }
  }

  record Coord(int x, int y) {
    Coord step(Dir dir) {
      return new Coord(x + dir.deltaX, y + dir.deltaY);
    }
  }

  enum Dir {
    UP('^', 0, -1),
    DOWN('v', 0, +1),
    LEFT('<', -1, 0),
    RIGHT('>', +1, 0);

    static final ImmutableSet<Dir> ALL = ImmutableSet.copyOf(EnumSet.allOf(Dir.class));

    static Dir of(char c) {
      return ALL.stream().filter(dir -> dir.code == c).findFirst().get();
    }

    final char code;
    final int deltaX;
    final int deltaY;

    private Dir(char code, int deltaX, int deltaY) {
      this.code = code;
      this.deltaX = deltaX;
      this.deltaY = deltaY;
    }
  }
}
