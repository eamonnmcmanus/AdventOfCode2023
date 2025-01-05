package advent2019;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle4 {
  public static void main(String[] args) throws Exception {
    int start, stop;
    try (Reader r = new InputStreamReader(Puzzle4.class.getResourceAsStream("puzzle4.txt"))) {
      var numbers =
          Splitter.on('-')
              .splitToStream(CharStreams.toString(r).trim())
              .map(Integer::valueOf)
              .toList();
      checkState(numbers.size() == 2);
      start = numbers.get(0);
      stop = numbers.get(1);
    }
    long count1 = IntStream.rangeClosed(start, stop).filter(Puzzle4::candidate1).count();
    System.out.printf("Part 1 count is %d\n", count1);
    long count2 = IntStream.rangeClosed(start, stop).filter(Puzzle4::candidate2).count();
    System.out.printf("Part 2 count is %d\n", count2);
  }

  private static boolean candidate1(int n) {
    char[] chars = Integer.toString(n).toCharArray();
    char[] copy = chars.clone();
    Arrays.sort(copy);
    if (!Arrays.equals(chars, copy)) {
      return false;
    }
    for (int i = 1; i < chars.length; i++) {
      if (chars[i - 1] == chars[i]) {
        return true;
      }
    }
    return false;
  }

  private static boolean candidate2(int n) {
    if (!candidate1(n)) {
      return false;
    }
    char[] chars = ("x" + n + "x").toCharArray();
    for (int i = 1; i < chars.length - 2; i++) {
      if (chars[i] == chars[i + 1] && chars[i] != chars[i - 1] && chars[i + 1] != chars[i + 2]) {
        return true;
      }
    }
    return false;
  }
}
