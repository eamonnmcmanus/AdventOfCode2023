package advent2021;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle10 {
  private static final String SAMPLE =
      """
      [({(<(())[]>[[{[]{<()<>>
      [(()[<>])]({[<{<<[]>>(
      {([(<{}[<>[]}>{[]{[(<()>
      (((({<>}<{<{<>}{[]{[]{}
      [[<[([]))<([[{}[[()]]]
      [{[{({}]{}}([{[{{{}}([]
      {<[[]]>}<{[{[{[]{()[[[]
      [<(<(<(<{}))><([]([]()
      <{([([[(<>()){}]>(<<{{
      <{([{{}}[<[[[<>{}]]]>[]]
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle10.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int totalPart1Score = 0;
        List<Long> part2Scores = new ArrayList<>();
        for (String line : lines) {
          int bad = part1(line);
          if (bad > 0) {
            totalPart1Score += bad;
          } else {
            part2Scores.add(part2(line));
          }
        }
        System.out.printf("Part 1 score for %s is %d\n", name, totalPart1Score);
        Collections.sort(part2Scores);
        System.out.printf(
            "Part 2 score for %s is %d\n", name, part2Scores.get(part2Scores.size() / 2));
      }
    }
  }

  private static int part1(String line) {
    List<Character> expected = new ArrayList<>();
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      switch (c) {
        case '(' -> expected.add(')');
        case '[' -> expected.add(']');
        case '{' -> expected.add('}');
        case '<' -> expected.add('>');
        default -> {
          if (expected.isEmpty() || expected.removeLast() != c) {
            return SCORES.get(c);
          }
        }
      }
    }
    return 0;
  }

  private static long part2(String line) {
    List<Character> expected = new ArrayList<>();
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      switch (c) {
        case '(' -> expected.add(')');
        case '[' -> expected.add(']');
        case '{' -> expected.add('}');
        case '<' -> expected.add('>');
        default -> expected.removeLast();
      }
    }
    long total = 0;
    for (char closing : expected.reversed()) {
      int closingScore = ")]}>".indexOf(closing) + 1;
      checkState(closingScore > 0);
      total = total * 5 + closingScore;
    }
    return total;
  }

  private static class ParseException extends RuntimeException {
    final char end;

    ParseException(char end) {
      this.end = end;
    }
  }

  private static final ImmutableMap<Character, Integer> SCORES =
      ImmutableMap.of(')', 3, ']', 57, '}', 1197, '>', 25137);

  private static final ImmutableMap<Character, Character> BRACKETS =
      ImmutableMap.of('(', ')', '[', ']', '{', '}', '<', '>');
}