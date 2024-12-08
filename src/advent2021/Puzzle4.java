package advent2021;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle4 {
  private static final String SAMPLE =
      """
      7,4,9,5,11,17,23,2,0,14,21,24,10,16,13,6,15,25,12,22,18,20,8,19,3,26,1

      22 13 17 11  0
       8  2 23  4 24
      21  9 14 16  7
       6 10  3 18  5
       1 12 20 15 19

       3 15  0  2 22
       9 18 13 17  5
      19  8  7 25 23
      20 11 10 24  4
      14 21 16 12  6

      14 21 17 24  4
      10 16 15  9 19
      18  8 23 26 20
      22 11 13  6  5
       2  0 12  3  7
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle1.class.getResourceAsStream("puzzle4.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Integer> calls =
            Splitter.on(',').splitToStream(lines.get(0)).map(Integer::valueOf).toList();
        part1(name, calls, lines);
        part2(name, calls, lines);
      }
    }
  }

  // I originally wrote an overcomplicated thing with bitmasks and so on, and it didn't work, so I
  // dumped it in favour of this naive solution. Numbers are removed from boards as they are called
  // by setting them to null. Checking for wins happens with nested for-loops.

  private static void part1(String name, List<Integer> calls, List<String> lines) {
    List<Board> boards = parseBoards(lines);
    Board winner = null;
    int lastCall = -1;
    calls:
    for (var call : calls) {
      for (var board : boards) {
        board.remove(call);
        if (board.wins()) {
          lastCall = call;
          winner = board;
          break calls;
        }
      }
    }
    System.out.printf("Part 1 result for %s is %d\n", name, winner.sum() * lastCall);
  }

  private static void part2(String name, List<Integer> calls, List<String> lines) {
    List<Board> boards = new ArrayList<>(parseBoards(lines));
    int lastCall = -1;
    calls:
    for (var call : calls) {
      lastCall = call;
      for (int i = 0; i < boards.size(); i++) {
        var board = boards.get(i);
        board.remove(call);
        if (board.wins()) {
          if (boards.size() > 1) {
            boards.remove(i);
            i--;
          } else {
            break calls;
          }
        }
      }
    }
    System.out.printf(
        "Part 2 result for %s is %d\n", name, getOnlyElement(boards).sum() * lastCall);
  }

  private static class Board {
    static final Pattern SPACE = Pattern.compile("\\s+");

    private final List<Integer> numbers;

    Board(List<Integer> numbers) {
      this.numbers = numbers;
    }

    static Board parse(List<String> strings) {
      var numbers =
          strings.stream()
              .flatMap(
                  line ->
                      Splitter.on(SPACE)
                          .omitEmptyStrings()
                          .splitToStream(line)
                          .map(Integer::valueOf))
              .collect(toCollection(ArrayList::new));
      return new Board(numbers);
    }

    void remove(int i) {
      int index = numbers.indexOf(i);
      if (index >= 0) {
        numbers.set(index, null);
      }
    }

    int sum() {
      return numbers.stream().filter(Objects::nonNull).reduce(0, Integer::sum);
    }

    boolean wins() {
      for (int i = 0; i < 25; i += 5) {
        boolean allNull = true;
        for (int j = i; j < i + 5; j++) {
          if (numbers.get(j) != null) {
            allNull = false;
            break;
          }
        }
        if (allNull) {
          return true;
        }
      }
      for (int i = 0; i < 5; i++) {
        boolean allNull = true;
        for (int j = i; j < 25; j += 5) {
          if (numbers.get(j) != null) {
            allNull = false;
            break;
          }
        }
        if (allNull) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String toString() {
      return IntStream.iterate(0, i -> i < 25, i -> i + 5)
          .mapToObj(i -> numbers.subList(i, i + 5))
          .map(
              line ->
                  line.stream()
                      .map(i -> String.format("%2s", (i == null) ? "--" : Integer.toString(i)))
                      .collect(joining(" ")))
          .collect(joining("\n"));
    }
  }

  private static List<Board> parseBoards(List<String> lines) {
    return IntStream.iterate(1, i -> i < lines.size(), i -> i + 6)
        .mapToObj(i -> Board.parse(lines.subList(i + 1, i + 6)))
        .toList();
  }
}