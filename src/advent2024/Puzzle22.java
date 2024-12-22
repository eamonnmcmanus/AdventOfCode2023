package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.MoreCollectors.onlyElement;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle22 {
  private static final String SAMPLE1 =
      """
      1
      10
      100
      2024
      """;
  private static final String SAMPLE2 =
      """
      1
      2
      3
      2024
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample 1",
          () -> new StringReader(SAMPLE1),
          "sample 2",
          () -> new StringReader(SAMPLE2),
          "problem",
          () -> new InputStreamReader(Puzzle22.class.getResourceAsStream("puzzle22.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Long> secrets = lines.stream().map(Long::valueOf).toList();
        long sum = secrets.stream().map(s -> nthNextSecret(2000, s)).reduce(0L, Math::addExact);
        System.out.printf("For %s, sum of 2000th secret numbers is %d\n", name, sum);
        System.out.printf("For %s, max bananas is %d\n", name, part2(secrets));
      }
    }
  }

  private static long part2(List<Long> secrets) {
    Map<List<Integer>, Long> totals = new LinkedHashMap<>();
    for (long s : secrets) {
      long s0 = s;
      Map<List<Integer>, Integer> results = new LinkedHashMap<>();
      List<Integer> window = new ArrayList<>();
      int lastPrice = 0;
      for (int i = 0; i < 2000; i++) {
        int price = (int) s % 10;
        if (i > 0) {
          int diff = price - lastPrice;
          window.add(diff);
          if (window.size() > 4) {
            window.removeFirst();
          }
          if (window.size() == 4) {
            results.putIfAbsent(new ArrayList<>(window), price);
          }
        }
        lastPrice = price;
        s = nextSecret(s);
      }
      results.forEach(
          (diffList, price) -> totals.put(diffList, totals.getOrDefault(diffList, 0L) + price));
    }
    return Collections.max(totals.values());
  }

  private static long nthNextSecret(int n, long s) {
    for (int i = 0; i < n; i++) {
      s = nextSecret(s);
    }
    return s;
  }

  private static long nextSecret(long s) {
    long mask = (1 << 24) - 1;
    s = ((s << 6) ^ s) & mask;
    s = (s >> 5) ^ s;
    s = ((s << 11) ^ s) & mask;
    return s;
  }
}