package advent2021;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Integer.signum;
import static java.util.Arrays.stream;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.primitives.Ints;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle6 {
  private static final String SAMPLE =
      """
      3,4,3,1,2
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle6.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Integer> startTimers =
            Splitter.on(',')
                .splitToStream(getOnlyElement(lines))
                .map(Integer::valueOf)
                .toList();
        List<Long> perTimer = new ArrayList<>(Collections.nCopies(9, 0L));
        for (var i : startTimers) {
          perTimer.set(i, perTimer.get(i) + 1);
        }
        for (int day = 0; day < 256; day++) {
          Long spawnCount = perTimer.removeFirst();
          perTimer.set(6, Math.addExact(perTimer.get(6), spawnCount));
          perTimer.add(spawnCount);
          if (day == 79) {
            System.out.printf("After 80 days, count for %s is %d\n",
                name, perTimer.stream().reduce(0L, Math::addExact));
          }
        }
        System.out.printf("After 256 days, count for %s is %d\n",
            name, perTimer.stream().reduce(0L, Math::addExact));
      }
    }
  }
}