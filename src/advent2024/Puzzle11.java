package advent2024;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle11 {
  private static final String SAMPLE =
      """
      125 17
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle11.class.getResourceAsStream("puzzle11.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Long> numbers =
            Splitter.on(' ').splitToStream(getOnlyElement(lines)).map(Long::valueOf).toList();
        List<Long> newNumbers = new ArrayList<>(numbers);
        for (int i = 0; i < 25; i++) {
          newNumbers = blink(newNumbers);
        }
        System.out.printf(
            "For %s, after 25 blinks, number of stones %d\n", name, newNumbers.size());
        System.out.printf(
            "For %s, after 75 blinks, number of stones %d\n", name, count(numbers, 75));
      }
    }
  }

  // This naïve approach is sufficient for Part 1 but of course it explodes exponentially in Part 2.
  private static List<Long> blink(List<Long> numbers) {
    List<Long> result = new ArrayList<>();
    for (long number : numbers) {
      if (number == 0) {
        result.add(1L);
      } else {
        String s = Long.toString(number);
        if (s.length() % 2 == 0) {
          int half = s.length() / 2;
          result.add(Long.valueOf(s.substring(0, half)));
          result.add(Long.valueOf(s.substring(half)));
        } else {
          result.add(multiplyExact(number, 2024L));
        }
      }
    }
    return result;
  }

  // The idea here is that 25 blinks for [125,17] is count(125, 25) + count(17, 25), which is
  // count(253000, 24) + count(1, 24) + count(7, 24), etc, and that as the recursion proceeds
  // we will see many of the same calls and we can therefore reuse the saved values. With the
  // problem input, there were 186,424 stones after 25 blinks but only 503 distinct values.
  private record Result(long number, int blinks) {}

  private static long count(List<Long> numbers, int blinks) {
    var resultCache = new HashMap<Result, Long>();
    long count = 0;
    for (long number : numbers) {
      count = addExact(count, count(number, blinks, resultCache));
    }
    return count;
  }

  private static long count(long number, int blinks, Map<Result, Long> resultCache) {
    if (blinks == 0) {
      return 1;
    }
    var key = new Result(number, blinks);
    Long cached = resultCache.get(key);
    if (cached != null) {
      return cached;
    }
    long result;
    if (number == 0) {
      result = count(1, blinks - 1, resultCache);
    } else {
      String s = Long.toString(number);
      if (s.length() % 2 == 0) {
        int half = s.length() / 2;
        result =
            addExact(
                count(Long.parseLong(s.substring(0, half)), blinks - 1, resultCache),
                count(Long.parseLong(s.substring(half)), blinks - 1, resultCache));
      } else {
        result = count(multiplyExact(number, 2024L), blinks - 1, resultCache);
      }
    }
    resultCache.put(key, result);
    return result;
  }
}