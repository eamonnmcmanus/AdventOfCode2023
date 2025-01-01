package advent2020;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle2 {
  private static final String SAMPLE =
      """
      1-3 a: abcde
      1-3 b: cdefg
      2-9 c: ccccccccc
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle2.class.getResourceAsStream("puzzle2.txt")));

  private static final Pattern PATTERN = Pattern.compile("(\\d+)-(\\d+) (.): (.*)");

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int part1Count = 0;
        int part2Count = 0;
        for (String line : lines) {
          var matcher = PATTERN.matcher(line);
          checkState(matcher.matches(), line);
          int lower = Integer.parseInt(matcher.group(1));
          int upper = Integer.parseInt(matcher.group(2));
          char c = matcher.group(3).charAt(0);
          String password = matcher.group(4);
          long freq = password.chars().filter(i -> i == c).count();
          if (lower <= freq && freq <= upper) {
            part1Count++;
          }
          if ((password.charAt(lower - 1) == c) != (password.charAt(upper - 1) == c)) {
            part2Count++;
          }
        }
        System.out.printf("For %s, Part 1 count is %d\n", name, part1Count);
        System.out.printf("For %s, Part 2 count is %d\n", name, part2Count);
      }
    }
  }
}