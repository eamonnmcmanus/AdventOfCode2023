package advent2020;

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
public class Puzzle1 {
  private static final String SAMPLE =
      """
      1721
      979
      366
      299
      675
      1456
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
        List<Integer> numbers = lines.stream().map(Integer::valueOf).toList();
        outer:
        for (int i : numbers) {
          for (int j : numbers) {
            if (i + j == 2020) {
              System.out.printf("For %s, %d * %d = %d\n", name, i, j, i * j);
              break outer;
            }
          }
        }
        outer:
        for (int i : numbers) {
          for (int j : numbers) {
            for (int k : numbers) {
              if (i + j + k == 2020) {
                System.out.printf("For %s, %d * %d * %d = %d\n", name, i, j, k, i * j * k);
                break outer;
              }
            }
          }
        }
      }
    }
  }
}