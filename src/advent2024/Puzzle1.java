package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.absExact;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle1 {
  private static final String SAMPLE =
      """
      3   4
      4   3
      2   5
      1   3
      3   9
      3   3
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle1.txt")));

  private static final Pattern LINE_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+)");

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (String line : lines) {
          Matcher matcher = LINE_PATTERN.matcher(line);
          checkArgument(matcher.matches());
          left.add(Integer.valueOf(matcher.group(1)));
          right.add(Integer.valueOf(matcher.group(2)));
        }
        Collections.sort(left);
        Collections.sort(right);
        var rightCounts = ImmutableMultiset.copyOf(right);
        int total = 0;
        long similarity = 0;
        for (int i = 0; i < left.size(); i++) {
          total += absExact(left.get(i) - right.get(i));
          similarity += left.get(i) * rightCounts.count(left.get(i));
        }
        System.out.println("Total for " + name + " is " + total);
        System.out.println("Similarity for " + name + " is " + similarity);
      }
    }
  }
}
