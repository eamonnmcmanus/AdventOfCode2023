package advent2021;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Math.multiplyExact;

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
public class Puzzle3 {
  private static final String SAMPLE =
      """
      00100
      11110
      10110
      10111
      10101
      01111
      00111
      11100
      10000
      11001
      00010
      01010
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
        int nBits = lines.get(0).length();
        checkState(lines.stream().allMatch(line -> line.length() == nBits));
        checkState(nBits < 32);
        List<Integer> numbers = lines.stream().map(line -> Integer.valueOf(line, 2)).toList();

        var part1 = part1(numbers, nBits);
        System.out.printf(
            "For %s, gamma %d, epsilon %d, product %d\n",
            name, part1.get(0), part1.get(1), multiplyExact(part1.get(0), part1.get(1)));

        var part2 = part2(numbers, nBits);
        System.out.printf(
            "For %s, oxygen %d, CO₂ scrubber %d, product %d\n",
            name, part2.get(0), part2.get(1), multiplyExact(part2.get(0), part2.get(1)));
      }
    }
  }

  private static List<Integer> part1(List<Integer> numbers, int nBits) {
    int gamma = 0; // more common bitmask
    int epsilon = 0; // less common bitmask
    for (int bitmask = 1 << (nBits - 1); bitmask != 0; bitmask >>= 1) {
      if (moreZeroes(numbers, bitmask)) {
        epsilon |= bitmask;
      } else {
        gamma |= bitmask;
      }
    }
    return List.of(gamma, epsilon);
  }

  private static List<Integer> part2(List<Integer> numbers, int nBits) {
    List<Integer> results = new ArrayList<>();

    // The logic here is kind of tricky. Originally I wrote both parts separately, then boiled it
    // down to this. It hinges on whether the number of 0 bits is strictly greater than the number
    // of 1 bits. For the "oxygen generator", we will then retain 0 bits. For the "CO2 scrubber", we
    // will retain 1 bits. That corresponds to retaining 0 when "more zeroes" is the same as
    // "oxygen" and 1 bits otherwise.
    for (boolean oxygen : new boolean[] {true, false}) {
      var currentNumbers = numbers;

      for (int bit = nBits - 1; bit >= 0 && currentNumbers.size() > 1; bit--) {
        int bitmask = 1 << bit;
        int mask = (moreZeroes(currentNumbers, bitmask) == oxygen) ? 0 : bitmask;
        currentNumbers = currentNumbers.stream().filter(n -> (n & bitmask) == mask).toList();
        if (currentNumbers.size() == 1) {
          break;
        }
      }
      results.add(getOnlyElement(currentNumbers));
    }
    return results;
  }

  private static boolean moreZeroes(List<Integer> numbers, int bitmask) {
    long zeroes = numbers.stream().filter(n -> (n & bitmask) == 0).count();
    return zeroes * 2 > numbers.size();
  }
}