package advent2024;

import static java.lang.Integer.signum;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle2 {
  private static final String SAMPLE =
      """
      7 6 4 2 1
      1 2 7 8 9
      9 7 6 2 1
      1 3 2 4 5
      8 6 4 4 1
      1 3 6 7 9
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle2.class.getResourceAsStream("puzzle2.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<List<Integer>> readings =
            lines.stream()
                .map(line -> Splitter.on(" ").splitToStream(line).map(Integer::valueOf).toList())
                .toList();
        long count = readings.stream().filter(Puzzle2::safe).count();
        System.out.println("Count for " + name + " is " + count);
        long countWithDampener = readings.stream().filter(Puzzle2::safeWithDampener).count();
        System.out.println("Count with dampener for " + name + " is " + countWithDampener);
      }
    }
  }

  private static boolean safe(List<Integer> reading) {
    int prevDelta = 0;
    for (int i = 1; i < reading.size(); i++) {
      int delta = reading.get(i) - reading.get(i - 1);
      if (prevDelta != 0 && signum(delta) != signum(prevDelta)) {
        return false;
      }
      if (delta == 0 || Math.abs(delta) > 3) {
        return false;
      }
      prevDelta = delta;
    }
    return true;
  }

  private static boolean safeWithDampener(List<Integer> reading) {
    if (safe(reading)) {
      return true;
    }
    // Try removing each number to see if the remaining list is safe. This is inefficient, and I
    // originally modified `safe` to return the index of the first failing number, if any. But that
    // proved fiddly to get right. The lists in the problem input are short enough to make this
    // brute-force approach essentially immediate.
    for (int i = 0; i < reading.size(); i++) {
      List<Integer> withRemoval = new ArrayList<>(reading.subList(0, i));
      withRemoval.addAll(reading.subList(i + 1, reading.size()));
      if (safe(withRemoval)) {
        return true;
      }
    }
    return false;
  }
}
