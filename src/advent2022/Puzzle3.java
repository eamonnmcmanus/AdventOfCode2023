package advent2022;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.intersection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
public class Puzzle3 {
  private static final String SAMPLE =
      """
      vJrwpWtwJgWrhcsFMMfFFhFp
      jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
      PmmdzqPrVvPwwTWBwg
      wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
      ttgJtRGJQctTZtZT
      CrZsJsPPZsGzwwsLwLmpwMDw
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle3.class.getResourceAsStream("puzzle3.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);

        // Part 1
        long sumPart1 = 0;
        for (String line : lines) {
          String left = line.substring(0, line.length() / 2);
          String right = line.substring(line.length() / 2);
          assert left.length() == right.length() : line;
          var leftSet = classify(left);
          var rightSet = classify(right);
          var intersection = intersection(leftSet, rightSet);
          char c = Iterables.getOnlyElement(intersection);
          sumPart1 += priority(c);
        }
        System.out.println(STR."Part 1 sum for \{name} is \{sumPart1}");

        // Part 2
        long sumPart2 = 0;
        assert lines.size() % 3 == 0;
        for (int i = 0; i < lines.size(); i += 3) {
          var intersection = intersection(
              classify(lines.get(i)),
              intersection(classify(lines.get(i + 1)),
                  classify(lines.get(i + 2))));
          var c = Iterables.getOnlyElement(intersection);
          sumPart2 += priority(c);
        }
        System.out.println(STR."Part 2 sum for \{name} is \{sumPart2}");
      }
    }
  }

  private static int priority(char c) {
    if (c >= 'a' && c <= 'z') {
      return c - 'a' + 1;
    } else if (c >= 'A' && c <= 'Z') {
      return c - 'A' + 27;
    } else {
      throw new AssertionError(c);
    }
  }

  private static ImmutableSet<Character> classify(String string) {
    return string.chars().mapToObj(i -> (char) i).collect(toImmutableSet());
  }
}
