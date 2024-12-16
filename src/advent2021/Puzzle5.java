package advent2021;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.signum;
import static java.util.Arrays.stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.primitives.Ints;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle5 {
  private static final String SAMPLE =
      """
      0,9 -> 5,9
      8,0 -> 0,8
      9,4 -> 3,4
      2,2 -> 2,1
      7,0 -> 7,4
      6,4 -> 2,0
      0,9 -> 2,9
      3,4 -> 1,4
      0,0 -> 8,8
      5,5 -> 8,2
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle5.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> inputLines = CharStreams.readLines(r);
        List<Line> lines = parseLines(inputLines);
        int[][] orthogonalCounts = new int[1000][1000];
        int[][] allCounts = new int[1000][1000];
        for (Line line : lines) {
          int deltaX = signum(line.end.x - line.start.x);
          int deltaY = signum(line.end.y - line.start.y);
          boolean orthogonal = deltaX == 0 || deltaY == 0;
          int x = line.start.x;
          int y = line.start.y;
          while (true) {
            allCounts[y][x]++;
            if (orthogonal) {
              orthogonalCounts[y][x]++;
            }
            if (x == line.end.x && y == line.end.y) {
              break;
            }
            x += deltaX;
            y += deltaY;
          }
        }
        System.out.printf("Part 1 count for %s is %d\n", name, countMoreThanOne(orthogonalCounts));
        System.out.printf("Part 2 count for %s is %d\n", name, countMoreThanOne(allCounts));
      }
    }
  }

  private static long countMoreThanOne(int[][] counts) {
    return stream(counts)
        .flatMap(countLine -> Ints.asList(countLine).stream())
        .filter(i -> i > 1)
        .count();
  }

  private record Coord(int x, int y) {}

  private record Line(Coord start, Coord end) {}

  private static List<Line> parseLines(List<String> inputLines) {
    return inputLines.stream().map(Puzzle5::parseLine).toList();
  }

  private static final Pattern LINE_PATTERN = Pattern.compile("(\\d+),(\\d+) -> (\\d+),(\\d+)");

  private static Line parseLine(String inputLine) {
    var matcher = LINE_PATTERN.matcher(inputLine);
    checkState(matcher.matches(), "No match for %s", inputLine);
    checkState(matcher.groupCount() == 4, "Unexpected match count %s", matcher.groupCount());
    int[] numbers =
        IntStream.range(1, 5).mapToObj(matcher::group).mapToInt(Integer::parseInt).toArray();
    return new Line(new Coord(numbers[0], numbers[1]), new Coord(numbers[2], numbers[3]));
  }
}