package advent2022;

import com.google.common.collect.ImmutableBiMap;
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
public class Puzzle25 {
  private static final ImmutableBiMap<Character, Integer> SNAFU_TO_DECIMAL =
      ImmutableBiMap.of('0', 0, '1', 1, '2', 2, '=', -2, '-', -1);

  private static final String SAMPLE = """
      1=-0-2
      12111
      2=0=
      21
      2=01
      111
      20012
      112
      1=-1=
      1-12
      12
      1=
      122
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle25.class.getResourceAsStream("puzzle25.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        long sum = lines.stream().mapToLong(s -> parseSnafu(s)).sum();
        System.out.println("Decimal sum " + sum + ", SNAFU equivalent " + toSnafu(sum));
      }
    }
  }

  private static long parseSnafu(String s) {
    long n = 0;
    for (char c : s.toCharArray()) {
      n = n * 5 + SNAFU_TO_DECIMAL.get(c);
    }
    return n;
  }

  private static String toSnafu(long n) {
    int last = Math.toIntExact(n % 5);
    n /= 5;
    if (last > 2) {
      last -= 5;
      n++;
    }
    String prefix = (n > 0) ? toSnafu(n) : "";
    return prefix + SNAFU_TO_DECIMAL.inverse().get(last);
  }
}
