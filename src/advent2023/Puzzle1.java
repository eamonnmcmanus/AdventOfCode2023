package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle1 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle1.class.getResourceAsStream("puzzle1.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      System.out.printf("sum for part 1: %d\n", part1(lines));
      System.out.printf("sum for part 2: %d\n", part2(lines));
    }
  }

  private static int part1(List<String> lines) {
    int sum = 0;
    for (String line : lines) {
      int digit1 = -1;
      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (Character.isDigit(c)) {
          digit1 = c - '0';
          break;
        }
      }
      assert digit1 > 0;
      int digit2 = -1;
      for (int i = line.length() - 1; i >= 0; i--) {
        char c = line.charAt(i);
        if (Character.isDigit(c)) {
          digit2 = c - '0';
          break;
        }
      }
      assert digit2 > 0;
      int n = 10 * digit1 + digit2;
      sum += n;
    }
    return sum;
  }

  private static int part2(List<String> lines) {
    int sum = 0;
    for (String line : lines) {
      int digit1 = -1;
      for (int i = 0; i < line.length(); i++) {
        int v = valueAt(line, i);
        if (v >= 0) {
          digit1 = v;
          break;
        }
      }
      assert digit1 > 0;
      int digit2 = -1;
      for (int i = line.length() - 1; i >= 0; i--) {
        int v = valueAt(line, i);
        if (v >= 0) {
          digit2 = v;
          break;
        }
      }
      assert digit2 > 0;
      int n = 10 * digit1 + digit2;
      sum += n;
    }
    return sum;
  }

  private static final List<String> NUMBERS =
      List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine");

  private static int valueAt(String s, int i) {
    char c = s.charAt(i);
    if (Character.isDigit(c)) {
      return c - '0';
    }
    for (int j = 1; j < 10; j++) {
      String word = NUMBERS.get(j - 1);
      if (s.startsWith(word, i)) {
        return j;
      }
    }
    return -1;
  }
}
