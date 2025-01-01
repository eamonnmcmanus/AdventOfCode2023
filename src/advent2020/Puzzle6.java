package advent2020;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toSet;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle6 {
  private static final String SAMPLE =
      """
      abc

      a
      b
      c

      ab
      ac

      a
      a
      a
      a

      b
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle6.class.getResourceAsStream("puzzle6.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        String text = CharStreams.toString(r);
        List<Set<Character>> groups =
            Splitter.on("\n\n")
                .splitToStream(text)
                .map(s -> s.chars().filter(c -> c != '\n').mapToObj(c -> (char) c).collect(toSet()))
                .toList();
        int sum = groups.stream().mapToInt(Set::size).sum();
        System.out.printf("For %s, Part 1 sum is %d\n", name, sum);
        Set<Character> letters =
            IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).collect(toSet());
        List<Set<Character>> groups2 =
            Splitter.on("\n\n")
                .splitToStream(text)
                .map(
                    s ->
                        Splitter.on('\n')
                            .omitEmptyStrings()
                            .splitToStream(s)
                            .map(line -> line.chars().mapToObj(c -> (char) c).collect(toSet()))
                            .reduce(letters, Sets::intersection))
                .toList();
        int sum2 = groups2.stream().mapToInt(Set::size).sum();
        System.out.printf("For %s, Part 2 sum is %d\n", name, sum2);
      }
    }
  }
}