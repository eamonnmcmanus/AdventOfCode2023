package advent2021;

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
      199
      200
      208
      210
      200
      207
      240
      269
      260
      263
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
        List<Integer> readings = lines.stream().map(Integer::valueOf).toList();
        int increases = 0;
        for (int i = 1; i < readings.size(); i++) {
          if (readings.get(i) > readings.get(i - 1)) {
            increases++;
          }
        }
        System.out.println("Increases for " + name + ": " + increases);
        int windowIncreases = 0;
        int windowSum = readings.get(0) + readings.get(1) + readings.get(2);
        for (int i = 3; i < readings.size(); i++) {
          int newWindowSum = windowSum - readings.get(i - 3) + readings.get(i);
          if (newWindowSum > windowSum) {
            windowIncreases++;
          }
        }
        System.out.println("Window increases for " + name + ": " + windowIncreases);
      }
    }
  }
}
