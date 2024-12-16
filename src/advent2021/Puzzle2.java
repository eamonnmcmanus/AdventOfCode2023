package advent2021;

import static java.lang.Math.multiplyExact;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle2 {
  private static final String SAMPLE =
      """
      forward 5
      down 5
      forward 8
      up 3
      down 8
      forward 2
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle2.class.getResourceAsStream("puzzle2.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Position part1 = part1(lines);
        System.out.printf(
            "For %s part 1, horizontal %d, depth %d, product %d\n",
            name, part1.horizontal, part1.depth, part1.product());
        Position part2 = part2(lines);
        System.out.printf(
            "For %s part 2, horizontal %d, depth %d, product %d\n",
            name, part2.horizontal, part2.depth, part2.product());
      }
    }
  }

  private record Position(long horizontal, long depth) {
    long product() {
      return multiplyExact(horizontal, depth);
    }
  }

  private static Position part1(List<String> lines) {
    long horizontal = 0;
    long depth = 0;
    for (String line : lines) {
      List<String> parts = Splitter.on(' ').splitToList(line);
      int amount = Integer.parseInt(parts.get(1));
      switch (parts.get(0)) {
        case "forward" -> horizontal += amount;
        case "down" -> depth += amount;
        case "up" -> depth -= amount;
      }
    }
    return new Position(horizontal, depth);
  }

  private static Position part2(List<String> lines) {
    long horizontal = 0;
    long depth = 0;
    long aim = 0;
    for (String line : lines) {
      List<String> parts = Splitter.on(' ').splitToList(line);
      int amount = Integer.parseInt(parts.get(1));
      switch (parts.get(0)) {
        case "forward" -> {
          horizontal += amount;
          depth += multiplyExact(amount, aim);
        }
        case "down" -> aim += amount;
        case "up" -> aim -= amount;
      }
    }
    return new Position(horizontal, depth);
  }
}
