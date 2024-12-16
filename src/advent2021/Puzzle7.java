package advent2021;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Integer.min;
import static java.lang.Math.multiplyExact;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle7 {
  private static final String SAMPLE =
      """
      16,1,2,0,4,2,7,1,2,14
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle7.class.getResourceAsStream("puzzle7.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Integer> positions =
            Splitter.on(',').splitToStream(getOnlyElement(lines)).map(Integer::valueOf).toList();
        int min = positions.stream().min(Comparator.naturalOrder()).get();
        int max = positions.stream().max(Comparator.naturalOrder()).get();
        int part1 = Integer.MAX_VALUE;
        int part2 = Integer.MAX_VALUE;
        for (int pos = min; pos <= max; pos++) {
          int pos0 = pos;
          int total1 = positions.stream().map(i -> Math.abs(i - pos0)).reduce(0, Integer::sum);
          part1 = min(part1, total1);
          int total2 =
              positions.stream().map(i -> triangle(Math.abs(i - pos0))).reduce(0, Integer::sum);
          part2 = min(part2, total2);
        }
        System.out.printf("For %s, Part 1 best is %d\n", name, part1);
        System.out.printf("For %s, Part 2 best is %d\n", name, part2);
      }
    }
  }

  private static int triangle(int n) {
    return multiplyExact(n, n + 1) / 2;
  }
}