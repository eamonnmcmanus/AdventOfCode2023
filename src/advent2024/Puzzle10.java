package advent2024;

import adventlib.CharGrid;
import adventlib.CharGrid.Coord;
import adventlib.Dir;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle10 {
  private static final String SAMPLE =
      """
      89010123
      78121874
      87430965
      96549874
      45678903
      32019012
      01329801
      10456732
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle10.txt")));

  public static void main(String[] args) throws Exception {
    // For this one, I started writing a solution for Part 1 that I realized was wrong, but when
    // I got to Part 2, I realized that the previously wrong approach was right there.
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        var grid = new CharGrid(lines);
        SetMultimap<Character, Coord> digitToCoord = HashMultimap.create();
        for (Coord coord : grid.coords()) {
          digitToCoord.put(grid.get(coord), coord);
        }
        SetMultimap<Coord, Coord> reachableNines = HashMultimap.create();
        Map<Coord, Long> routeCounts = new LinkedHashMap<>();
        for (Coord coord : digitToCoord.get('9')) {
          reachableNines.put(coord, coord);
          routeCounts.put(coord, 1L);
        }
        for (char cur = '8'; cur >= '0'; cur = (char) (cur - 1)) {
          char next = (char) (cur + 1);
          for (Coord curCoord : digitToCoord.get(cur)) {
            long routeCount = 0;
            for (Dir dir : Dir.NEWS) {
              Coord neighbour = dir.move(curCoord, 1);
              if (grid.get(neighbour) == next) {
                reachableNines.putAll(curCoord, reachableNines.get(neighbour));
                routeCount += routeCounts.get(neighbour);
              }
            }
            routeCounts.put(curCoord, routeCount);
          }
        }
        long part1Total = 0;
        long part2Total = 0;
        for (Coord coord : digitToCoord.get('0')) {
          part1Total += reachableNines.get(coord).size();
          part2Total += routeCounts.get(coord);
        }
        System.out.printf("Part 1 total for %s is %d\n", name, part1Total);
        System.out.printf("Part 2 total for %s is %d\n", name, part2Total);
      }
    }
  }
}