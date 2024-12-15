package advent2021;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle14 {
  private static final String SAMPLE =
      """
      NNCB

      CH -> B
      HH -> N
      CB -> H
      NH -> C
      HB -> C
      HC -> B
      HN -> C
      NN -> C
      BH -> H
      NC -> B
      NB -> B
      BN -> B
      BB -> N
      BC -> B
      CC -> N
      CN -> C
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle14.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        String input = lines.get(0);
        checkArgument(lines.get(1).isEmpty());
        Pattern rulePattern = Pattern.compile("([A-Z][A-Z]) -> ([A-Z])");
        ImmutableMap<String, Character> substitutions =
            lines.stream()
                .skip(2)
                .map(rulePattern::matcher)
                .peek(m -> checkState(m.matches()))
                .collect(toImmutableMap(m -> m.group(1), m -> m.group(2).charAt(0)));

        System.out.printf(
            "Result for %s after 10 steps is is %d\n", name, polymerize(input, 10, substitutions));
        System.out.printf(
            "Result for %s after 40 steps is is %d\n", name, polymerize(input, 40, substitutions));
      }
    }
  }

  // For Part 1, a naïve string-building approach is fine, but the sizes are obviously much too big
  // in Part 2. But we don't need to know what the actual string is, only what letter pairs are in
  // it, and we only need to maintain a count for each of those. At each step, if we have a letter
  // pair AC that gets B inserted into it, and if there are N occurrences of AC, then for the next
  // step we will have N times AB and N times BC.
  private static long polymerize(String input, int steps, Map<String, Character> substitutions) {
    char first = input.charAt(0);
    char last = input.charAt(input.length() - 1);
    Map<String, Long> pairFrequencies = new LinkedHashMap<>();
    for (int j = 1; j < input.length(); j++) {
      String pair = input.substring(j - 1, j + 1);
      pairFrequencies.put(pair, pairFrequencies.getOrDefault(pair, 0L) + 1);
    }
    for (int i = 0; i < steps; i++) {
      Map<String, Long> newPairFrequencies = new LinkedHashMap<>();
      pairFrequencies.forEach(
          (pair, count) -> {
            Character insert = substitutions.get(pair);
            if (insert == null) {
              // Turns out this doesn't happen, but never mind.
              newPairFrequencies.put(pair, newPairFrequencies.getOrDefault(pair, 0L) + count);
            } else {
              String pair1 = "" + pair.charAt(0) + insert;
              newPairFrequencies.put(pair1, newPairFrequencies.getOrDefault(pair1, 0L) + count);
              String pair2 = "" + insert + pair.charAt(1);
              newPairFrequencies.put(pair2, newPairFrequencies.getOrDefault(pair2, 0L) + count);
            }
          });
      pairFrequencies = newPairFrequencies;
    }
    Map<Character, Long> letterFrequencies = new LinkedHashMap<>();
    pairFrequencies.forEach(
        (pair, count) -> {
          Character c1 = pair.charAt(0);
          letterFrequencies.put(c1, letterFrequencies.getOrDefault(c1, 0L) + count);
          Character c2 = pair.charAt(1);
          letterFrequencies.put(c2, letterFrequencies.getOrDefault(c2, 0L) + count);
        });
    // We've counted every letter twice, once when it was the start of the pair it is in and
    // once when it was the end, except for the very first and last letters of the string. Those
    // letters don't change. So we divide all the values by 2 and then add 1 for the first and
    // last letters.
    for (Character letter : letterFrequencies.keySet()) {
      letterFrequencies.put(letter, letterFrequencies.get(letter) / 2);
    }
    letterFrequencies.put(first, letterFrequencies.get(first) + 1);
    letterFrequencies.put(last, letterFrequencies.get(last) + 1);
    long max = Collections.max(letterFrequencies.values());
    long min = Collections.min(letterFrequencies.values());
    return max - min;
  }
}