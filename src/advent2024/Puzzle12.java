package advent2024;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle12 {
  private static final String SAMPLE =
      """
      RRRRIICCFF
      RRRRIICCCF
      VVRRRCCFFF
      VVRCCCJFFF
      VVVVCJJCFE
      VVIVCCJJEE
      VVIIICJJEE
      MIIIIIJJEE
      MIIISIJEEE
      MMMISSJEEE
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle12.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        CharGrid grid = new CharGrid(lines);
        // Map from grid coord to a canonical member of the region containing that coord.
        Map<Coord, Coord> regions = new LinkedHashMap<>();
        Map<Coord, Size> sizes = new LinkedHashMap<>();
        for (Coord coord : grid.coords()) {
          if (!regions.containsKey(coord)) {
            Size size = fill(grid, regions, coord, coord);
            sizes.put(coord, size);
          }
        }
        int part1Price = sizes.values().stream().map(Size::price).reduce(0, Math::addExact);
        System.out.printf("For %s, total fence price for part 1 is %d\n", name, part1Price);

        Map<Coord, Integer> sideCounts = computeSideCounts(grid, regions);
        int part2Price = 0;
        for (Coord region : ImmutableSet.copyOf(regions.values())) {
          part2Price =
              addExact(part2Price, multiplyExact(sizes.get(region).area(), sideCounts.get(region)));
        }
        System.out.printf("For %s, total fence price for part 2 is %d\n", name, part2Price);
      }
    }
  }

  record Size(int area, int perimeter) {
    static final Size PERIMETER_ONE = new Size(0, 1);

    Size plus(Size that) {
      return new Size(this.area + that.area, this.perimeter + that.perimeter);
    }

    int price() {
      return multiplyExact(area, perimeter);
    }
  }

  private static Size fill(CharGrid grid, Map<Coord, Coord> regions, Coord canonical, Coord start) {
    Coord old = regions.put(start, canonical);
    checkState(old == null);
    Size size = new Size(1, 0);
    char plant = grid.get(canonical);
    checkState(grid.get(start) == plant);
    for (Dir dir : Dir.NEWS) {
      Coord adjacent = dir.move(start, 1);
      if (grid.get(adjacent) == plant) {
        if (!regions.containsKey(adjacent)) {
          size = size.plus(fill(grid, regions, canonical, adjacent));
        }
      } else {
        size = size.plus(Size.PERIMETER_ONE);
      }
    }
    return size;
  }

  // To compute the side counts, we scan each row looking for horizontal sides above and below that
  // row. An upper horizontal side is a maximal span of plots from the same region where the plot
  // above is from a different region. (This includes the empty plots above the first row.)
  // Similarly for lower horizontal sides. We also scan each column similarly looking for left
  // vertical and right vertical sides. Spelling out the code for the four kinds of sides is a bit
  // repetitive, but it would be easy to get things wrong trying to do something cleverer.
  private static Map<Coord, Integer> computeSideCounts(CharGrid grid, Map<Coord, Coord> regions) {
    Map<Coord, Integer> sideCounts = new LinkedHashMap<>();
    regions.keySet().forEach(region -> sideCounts.put(region, 0));

    // Horizontal sides
    for (int row = 0; row < grid.height(); row++) {
      // Upper horizontal sides
      for (int col = 0; col < grid.width(); ) {
        Coord coord = new Coord(row, col);
        char plant = grid.get(coord);
        if (grid.get(row - 1, col) != plant) {
          Coord region = regions.get(coord);
          sideCounts.put(region, sideCounts.get(region) + 1);
          do {
            col++;
          } while (grid.get(row, col) == plant && grid.get(row - 1, col) != plant);
        } else {
          col++;
        }
      }
      // Lower horizontal sides
      for (int col = 0; col < grid.width(); ) {
        Coord coord = new Coord(row, col);
        char plant = grid.get(coord);
        if (grid.get(row + 1, col) != plant) {
          Coord region = regions.get(coord);
          sideCounts.put(region, sideCounts.get(region) + 1);
          do {
            col++;
          } while (grid.get(row, col) == plant && grid.get(row + 1, col) != plant);
        } else {
          col++;
        }
      }
    }

    // Vertical sides
    for (int col = 0; col < grid.width(); col++) {
      // Left vertical sides
      for (int row = 0; row < grid.height(); ) {
        Coord coord = new Coord(row, col);
        char plant = grid.get(coord);
        if (grid.get(row, col - 1) != plant) {
          Coord region = regions.get(coord);
          sideCounts.put(region, sideCounts.get(region) + 1);
          do {
            row++;
          } while (grid.get(row, col) == plant && grid.get(row, col - 1) != plant);
        } else {
          row++;
        }
      }
      // Right vertical sides
      for (int row = 0; row < grid.height(); ) {
        Coord coord = new Coord(row, col);
        char plant = grid.get(coord);
        if (grid.get(row, col + 1) != plant) {
          Coord region = regions.get(coord);
          sideCounts.put(region, sideCounts.get(region) + 1);
          do {
            row++;
          } while (grid.get(row, col) == plant && grid.get(row, col + 1) != plant);
        } else {
          row++;
        }
      }
    }
    return sideCounts;
  }
}