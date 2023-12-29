package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle9 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle9.class.getResourceAsStream("puzzle9.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      long sum = 0;
      long backwardsSum = 0;
      for (String line : lines) {
        List<Long> numbers = Arrays.stream(line.split("\\s+")).map(Long::parseLong).toList();
        long extra = extrapolate(numbers);
        System.out.println(STR."Extrapolate \{numbers} to \{extra}");
        sum += extra;
        long backwardsExtra = extrapolateBackwards(numbers);
        System.out.println(STR."Extrapolate \{numbers} backwards to \{backwardsExtra}");
        backwardsSum += backwardsExtra;
      }
      System.out.println(STR."Sum \{sum}");
      System.out.println(STR."Backwards sum \{backwardsSum}");
    }
  }

  private static long extrapolate(List<Long> numbers) {
    List<Long> diffs = new ArrayList<>();
    boolean allZero = true;
    for (int i = 1; i < numbers.size(); i++) {
      long diff = numbers.get(i) - numbers.get(i - 1);
      if (diff != 0) {
        allZero = false;
      }
      diffs.add(diff);
    }
    if (allZero) {
      return numbers.getLast();
    } else {
      return numbers.getLast() + extrapolate(diffs);
    }
  }

  private static long extrapolateBackwards(List<Long> numbers) {
    List<Long> diffs = new ArrayList<>();
    boolean allZero = true;
    for (int i = 1; i < numbers.size(); i++) {
      long diff = numbers.get(i) - numbers.get(i - 1);
      if (diff != 0) {
        allZero = false;
      }
      diffs.add(diff);
    }
    if (allZero) {
      return numbers.getFirst();
    } else {
      return numbers.getFirst() - extrapolateBackwards(diffs);
    }
  }
}
