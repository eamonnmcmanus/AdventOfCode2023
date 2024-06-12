package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;

import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Éamonn McManus
 */
public class Puzzle12 {
  /*
   * Straightforward recursive solution. The only wrinkle is that Part 2 requires memoization to be
   * tractable.
   */
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle12.class.getResourceAsStream("puzzle12.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      long total = 0;
      long bigTotal = 0;
      for (String line : lines) {
        String[] parts = line.split("\\s+");
        assert parts.length == 2;

        // Part 1 of the puzzle.
        String springs = parts[0];
        List<Integer> spans = stream(parts[1].split(",")).map(Integer::parseInt).toList();
        total += combinations(springs, spans);

        // Part 2 of the puzzle.
        String bigSprings = String.join("?", Collections.nCopies(5, springs));
        List<Integer> bigSpans = Collections.nCopies(5, spans).stream().flatMap(List::stream).toList();
        System.out.println("Trying " + bigSprings + " with " + bigSpans);
        bigTotal += combinations(bigSprings, bigSpans);
      }
      System.out.println("Combinations " + total);
      System.out.println("Big combinations " + bigTotal);
      // Cache is a bit clunky. Dynamic programming would have been more elegant.
    }
  }

  private static final List<Integer> ZERO_LIST = ImmutableList.of(0);

  record State(String line, List<Integer> spans, boolean inSpan) {}

  private static long combinations(String line, List<Integer> spans) {
    return combinations(line, spans, false, new HashMap<>());
  }

  private static long combinations(String line, List<Integer> spans, boolean inSpan, Map<State, Long> cache) {
    State state = new State(line, spans, inSpan);
    Long cached = cache.get(state);
    if (cached != null) {
      return cached;
    }
    Long count = combinationsUncached(line, spans, inSpan, cache);
    cache.put(state, count);
    return count;
  }

  private static long combinationsUncached(String line, List<Integer> spans, boolean inSpan, Map<State, Long> cache) {
    if (line.isEmpty()) {
      boolean success = inSpan ? spans.equals(ZERO_LIST) : spans.isEmpty();
      return success ? 1 : 0;
    }
    switch (line.charAt(0)) {
      case '#' -> {
        if (spans.isEmpty() || (inSpan && spans.getFirst() == 0)) {
          return 0;
        }
        List<Integer> newSpans = ImmutableList.<Integer>builder()
            .add(spans.get(0) - 1)
            .addAll(spans.subList(1, spans.size()))
            .build();
        return combinations(line.substring(1), newSpans, true, cache);
      }
      case '.' -> {
        if (inSpan) {
          if (spans.getFirst() != 0) {
            return 0;
          }
          return combinations(line.substring(1), spans.subList(1, spans.size()), false, cache);
        } else {
          return combinations(line.substring(1), spans, false, cache);
        }
      }
      case '?' -> {
        return combinations('#' + line.substring(1), spans, inSpan, cache)
            + combinations('.' + line.substring(1), spans, inSpan, cache);
      }
      default -> throw new AssertionError(line.charAt(0));
    }
  }
}
