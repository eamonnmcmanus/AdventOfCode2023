package advent2022;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
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
public class Puzzle4 {
  private static final String SAMPLE = """
      2-4,6-8
      2-3,4-5
      5-7,7-9
      2-8,3-7
      6-6,4-6
      2-6,4-8
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle4.class.getResourceAsStream("puzzle4.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<RangePair> rangePairs = lines.stream().map(line -> parseRangePair(line)).toList();
        long count1 = rangePairs.stream()
            .filter(pair -> pair.one.encloses(pair.two) || pair.two.encloses(pair.one))
            .count();
        System.out.println("For " + name + ", count of enclosing ranges is " + count1);
        long count2 = rangePairs.stream()
            .filter(pair -> pair.one.isConnected(pair.two) && !pair.one.intersection(pair.two).isEmpty())
            .count();
        System.out.println("For " + name + ", count of overlapping ranges is " + count2);
      }
    }
  }

  private static RangePair parseRangePair(String line) {
    List<String> strings = Splitter.on(",").splitToList(line);
    assert strings.size() == 2 : line;
    return new RangePair(parseRange(strings.get(0)), parseRange(strings.get(1)));
  }

  private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)-(\\d+)");

  private static Range<Integer> parseRange(String s) {
    Matcher matcher = RANGE_PATTERN.matcher(s);
    if (!matcher.matches()) {
      throw new IllegalStateException("'" + s + "'");
    }
    return Range.closed(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
  }

  record RangePair(Range<Integer> one, Range<Integer> two) {}
}
