package advent2022;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle1 {
  private static final String SAMPLE = """
      1000
      2000
      3000

      4000

      5000
      6000

      7000
      8000
      9000

      10000
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle1.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<List<String>> groups = new ArrayList<>();
        while (true) {
          int index = lines.indexOf("");
          if (index < 0) {
            break;
          }
          groups.add(lines.subList(0, index));
          lines = lines.subList(index + 1, lines.size());
        }
        groups.add(lines);
        List<Long> totals = groups.stream()
            .map(group -> group.stream().mapToLong(Long::parseLong).sum())
            .sorted()
            .toList();
        System.out.println("Max for " + name + " is " + totals.getLast());
        long topThree = totals.stream().skip(totals.size() - 3).mapToLong(Long::valueOf).sum();
        System.out.println("Sum of top three is " + topThree);
      }
    }
  }
}
