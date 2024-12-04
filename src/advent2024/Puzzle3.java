package advent2024;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle3 {
  private static final String SAMPLE1 =
      """
      xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
      """;

  private static final String SAMPLE2 =
      """
      xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample 1",
          () -> new StringReader(SAMPLE1),
          "sample 2",
          () -> new StringReader(SAMPLE2),
          "problem",
          () -> new InputStreamReader(Puzzle3.class.getResourceAsStream("puzzle3.txt")));

  private static final Pattern MUL_PATTERN =
      Pattern.compile("mul \\( (\\d+),(\\d+) \\)", Pattern.COMMENTS);

  private static final Pattern EXT_MUL_PATTERN =
      Pattern.compile("( do\\(\\) | don't\\(\\) | mul \\( (\\d+),(\\d+) \\))", Pattern.COMMENTS);

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        long total1 = 0;
        long total2 = 0;
        boolean enabled = true;
        for (String line : lines) {
          for (Matcher matcher = MUL_PATTERN.matcher(line); matcher.find(); ) {
            total1 += Long.valueOf(matcher.group(1)) * Long.valueOf(matcher.group(2));
          }
          for (Matcher matcher = EXT_MUL_PATTERN.matcher(line); matcher.find(); ) {
            switch (matcher.group(1)) {
              case "do()" -> enabled = true;
              case "don't()" -> enabled = false;
              default -> {
                checkState(
                    matcher.group(1).startsWith("mul("), "Unexpected match %s", matcher.group(1));
                if (enabled) {
                  total2 += Long.valueOf(matcher.group(2)) * Long.valueOf(matcher.group(3));
                }
              }
            }
          }
        }
        System.out.println("Part 1 sum for " + name + " is " + total1);
        System.out.println("Part 2 sum for " + name + " is " + total2);
      }
    }
  }
}