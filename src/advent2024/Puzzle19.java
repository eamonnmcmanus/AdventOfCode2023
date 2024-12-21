package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.addExact;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle19 {
  private static final String SAMPLE =
      """
      r, wr, b, g, bwu, rb, gb, br

      brwrr
      bggr
      gbbr
      rrbgbr
      ubwu
      bwurrg
      brgr
      bbrgwb
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle19.class.getResourceAsStream("puzzle19.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        checkArgument(lines.get(1).isEmpty());

        List<String> patterns = Splitter.on(", ").splitToList(lines.get(0));
        List<String> strings = lines.subList(2, lines.size());
        int possibleCount = 0;
        long combinationCount = 0;
        for (String string : strings) {
          long count = possible(patterns, string, new LinkedHashMap<>());
          if (count > 0) {
            possibleCount++;
            combinationCount = addExact(combinationCount, count);
          }
        }
        System.out.printf(
            "For %s, possible count %d, combinations %d\n", name, possibleCount, combinationCount);
      }
    }
  }

  // I wasted a lot of time making a Trie implementation and tackling a subtle bug in the recursion
  // with it, before giving up and using this less efficient approach. It still runs in less than a
  // second.
  private static long possible(List<String> patterns, String s, Map<String, Long> cache) {
    if (s.isEmpty()) {
      return 1;
    }
    long count = 0;
    for (String pat : patterns) {
      if (s.startsWith(pat)) {
        String rest = s.substring(pat.length());
        if (!cache.containsKey(rest)) {
          cache.put(rest, possible(patterns, rest, cache));
        }
        count += cache.get(rest);
      }
    }
    return count;
  }
}