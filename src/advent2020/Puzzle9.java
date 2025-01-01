package advent2020;

import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle9 {
  public static void main(String[] args) throws Exception {
    try (Reader r = new InputStreamReader(Puzzle9.class.getResourceAsStream("puzzle9.txt"))) {
      List<Long> numbers = CharStreams.readLines(r).stream().map(Long::parseLong).toList();

      // Part 1
      long target = -1;
      for (int i = 25; i < numbers.size(); i++) {
        long ni = numbers.get(i);
        boolean found = false;
        for (int j = i - 25; j < i; j++) {
          long nj = numbers.get(j);
          if (nj * 2 == ni) {
            continue;
          }
          for (int k = j + 1; k < i; k++) {
            long nk = numbers.get(k);
            if (nj + nk == ni) {
              found = true;
              break;
            }
          }
        }
        if (!found) {
          System.out.printf("No numbers sum to %d\n", ni);
          target = ni;
          break;
        }
      }

      // Part 2. I initially summed the first and last numbers of the span rather than the minimum
      // and maximum. D'oh!
      for (int i = 0; i < numbers.size(); i++) {
        long sum = 0;
        int j;
        for (j = i; j < numbers.size() && (sum += numbers.get(j)) < target; j++) {}
        if (sum == target) {
          List<Long> span = numbers.subList(i, j + 1);
          System.out.printf(
              "Encryption weakness is %d\n", Collections.min(span) + Collections.max(span));
          break;
        }
      }
    }
  }
}
