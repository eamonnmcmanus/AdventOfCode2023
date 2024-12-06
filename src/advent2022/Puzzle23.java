package advent2022;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle23 {
  private static final String SAMPLE =
      """
      ....#..
      ..###.#
      #...#.#
      .#...##
      #.###..
      ##.#.##
      .#..#..
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle23.class.getResourceAsStream("puzzle23.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Grid grid = parseGrid(lines);
        part1(name, grid);
        part2(name, grid);
      }
    }
  }

  // This one was pretty easy, hugely easier than the previous day (the one about folding a cube).
  // The only glitch was when I mixed up East and West with my +1 and -1.

  private static void part1(String name, Grid grid) {
    for (int round = 1; round <= 10; round++) {
      grid = grid.nextRound();
    }
    System.out.println("For " + name + " part 1, final empty tile count " + grid.emptyCount());
  }

  private static void part2(String name, Grid grid) {
    int round = 1;
    while (true) {
      Grid nextGrid = grid.nextRound();
      if (nextGrid.coords.equals(grid.coords)) {
        break; // If no Elf needs to move from the intial grid, we will report round 1.
      }
      grid = nextGrid;
      round++;
    }
    System.out.println(
        "For "
            + name
            + " part 2, no movement on round "
            + round
            + ", final grid "
            + grid.topLeft()
            + ".."
            + grid.bottomRight());
  }

  static Grid parseGrid(List<String> lines) {
    ImmutableSet.Builder<Coord> builder = ImmutableSet.builder();
    for (int y = 0; y < lines.size(); y++) {
      String line = lines.get(y);
      for (int x = 0; x < line.length(); x++) {
        if (line.charAt(x) == '#') {
          builder.add(new Coord(x, y));
        }
      }
    }
    return new Grid(builder.build(), 0);
  }

  enum Dir {
    NORTH,
    SOUTH,
    WEST,
    EAST;

    static final Dir[] VALUES = values();
  }

  static final Coord NE = new Coord(+1, -1);
  static final Coord N = new Coord(0, -1);
  static final Coord NW = new Coord(-1, -1);
  static final Coord E = new Coord(+1, 0);
  static final Coord W = new Coord(-1, 0);
  static final Coord SE = new Coord(+1, +1);
  static final Coord S = new Coord(0, +1);
  static final Coord SW = new Coord(-1, +1);
  static final List<Coord> ADJACENT = List.of(NE, N, NW, E, W, SE, S, SW);

  static final Map<Dir, List<Coord>> ADJACENT_IN_DIR =
      ImmutableMap.of(
          Dir.NORTH, List.of(NE, N, NW),
          Dir.SOUTH, List.of(SE, S, SW),
          Dir.WEST, List.of(NW, W, SW),
          Dir.EAST, List.of(NE, E, SE));

  record Grid(Set<Coord> coords, int nextDirIndex) {
    Grid nextRound() {
      Map<Coord, Coord> proposed = new HashMap<>();

      // Any Elf with no neighbours just proposes to stay put.
      for (Coord coord : coords) {
        if (ADJACENT.stream()
            .map(c -> new Coord(coord.x + c.x, coord.y + c.y))
            .noneMatch(coords::contains)) {
          proposed.put(coord, coord);
        }
      }

      // First half: propose a step for each Elf.
      int dirIndex = nextDirIndex;
      do {
        Dir dir = Dir.VALUES[dirIndex];
        for (Coord coord : coords) {
          if (!proposed.containsKey(coord)
              && ADJACENT_IN_DIR.get(dir).stream()
                  .map(c -> new Coord(coord.x + c.x, coord.y + c.y))
                  .noneMatch(coords::contains)) {
            proposed.put(coord, coord.move(dir));
            if (proposed.size() == coords.size()) {
              break; // minor optimization
            }
          }
        }
        dirIndex = (dirIndex + 1) % 4;
      } while (dirIndex != nextDirIndex);

      // Second half: make all non-conflicting moves.
      Multiset<Coord> allProposed = ImmutableMultiset.copyOf(proposed.values());
      Set<Coord> newCoords =
          coords.stream()
              .map(
                  c ->
                      (proposed.containsKey(c) && allProposed.count(proposed.get(c)) == 1)
                          ? proposed.get(c)
                          : c)
              .collect(toImmutableSet());
      return new Grid(newCoords, (nextDirIndex + 1) % 4);
    }

    Coord topLeft() {
      int minX = coords.stream().mapToInt(Coord::x).min().getAsInt();
      int minY = coords.stream().mapToInt(Coord::y).min().getAsInt();
      return new Coord(minX, minY);
    }

    Coord bottomRight() {
      int maxX = coords.stream().mapToInt(Coord::x).max().getAsInt();
      int maxY = coords.stream().mapToInt(Coord::y).max().getAsInt();
      return new Coord(maxX, maxY);
    }

    int emptyCount() {
      Coord topLeft = topLeft();
      Coord bottomRight = bottomRight();
      int area = Math.multiplyExact(bottomRight.x - topLeft.x + 1, bottomRight.y - topLeft.y + 1);
      return area - coords.size();
    }

    @Override
    public String toString() {
      Coord topLeft = topLeft();
      Coord bottomRight = bottomRight();
      StringBuilder sb = new StringBuilder();
      for (int y = topLeft.y; y <= bottomRight.y; y++) {
        if (y > topLeft.y) {
          sb.append('\n');
        }
        for (int x = topLeft.x; x <= bottomRight.x; x++) {
          sb.append(coords.contains(new Coord(x, y)) ? '#' : '.');
        }
      }
      return sb.toString();
    }
  }

  record Coord(int x, int y) {
    Coord move(Dir dir) {
      return switch (dir) {
        case NORTH -> new Coord(x, y - 1);
        case SOUTH -> new Coord(x, y + 1);
        case WEST -> new Coord(x - 1, y);
        case EAST -> new Coord(x + 1, y);
      };
    }
  }
}
