package advent2019;

import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle1 {
  public static void main(String[] args) throws Exception {
    try (Reader r = new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle1.txt"))) {
      List<Long> numbers = CharStreams.readLines(r).stream().map(Long::valueOf).toList();
      long part1 = numbers.stream().mapToLong(n -> n / 3 - 2).sum();
      System.out.printf("Part 1 sum is %d\n", part1);
      long part2 = 0;
      for (long n : numbers) {
        for (long m = n; (m = m / 3 - 2) > 0; ) {
          part2 += m;
        }
      }
      System.out.printf("Part 2 sum is %d\n", part2);
    }
  }
}
