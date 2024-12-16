package advent2021;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle9 {
  private static final String SAMPLE =
      """
      2199943210
      3987894921
      9856789892
      8767896789
      9899965678
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle9.class.getResourceAsStream("puzzle9.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int[][] heights = parseHeights(lines);

        int risk = part1(heights);
        System.out.printf("For %s, risk is %d\n", name, risk);

        List<Integer> basinSizes = part2(heights);
        System.out.printf(
            "For %s, product of top 3 basin sizes %d\n",
            name,
            basinSizes.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .reduce(1, Math::multiplyExact));
      }
    }
  }

  private static int part1(int[][] heights) {
    int risk = 0;
    for (int i = 0; i < heights.length; i++) {
      for (int j = 0; j < heights[i].length; j++) {
        int h = heights[i][j];
        if ((i > 0 && heights[i - 1][j] <= h)
            || (i + 1 < heights.length && heights[i + 1][j] <= h)
            || (j > 0 && heights[i][j - 1] <= h)
            || (j + 1 < heights[i].length && heights[i][j + 1] <= h)) {
          continue;
        }
        risk += h + 1;
      }
    }
    return risk;
  }

  /*
    We assume that a basin is actually a maximal collection of orthogonally-adjacent locations that
    do not include a 9. We proceed from top left to bottom right finding locations that are not 9,
    and for each one we fill that location and adjacent ones with 9s, while recording the size.
    Concretely, we find neighbours that are not 9 and recursively fill those.

    Initially I didn't think it was necessary to consider neighbours "above" (i - 1) the one being
    filled, but it is, because we might be stopped by a 9 to the "right" (j + 1) on this line but
    the basin might continue past that 9 on the line "below".
  */
  private static List<Integer> part2(int[][] heights) {
    List<Integer> basinSizes = new ArrayList<>();
    for (int i = 0; i < heights.length; i++) {
      for (int j = 0; j < heights[i].length; j++) {
        int basinSize = fill(heights, i, j);
        if (basinSize > 0) {
          basinSizes.add(basinSize);
        }
      }
    }
    return basinSizes;
  }

  private static int fill(int[][] heights, int i, int j) {
    if (heights[i][j] == 9) {
      return 0;
    }
    heights[i][j] = 9;
    int basinSize = 1;
    if (i > 0) {
      basinSize += fill(heights, i - 1, j);
    }
    if (j > 0) {
      basinSize += fill(heights, i, j - 1);
    }
    if (j + 1 < heights[i].length) {
      basinSize += fill(heights, i, j + 1);
    }
    if (i + 1 < heights.length) {
      basinSize += fill(heights, i + 1, j);
    }
    return basinSize;
  }

  private static int[][] parseHeights(List<String> lines) {
    int width = lines.getFirst().length();
    checkArgument(lines.stream().allMatch(line -> line.length() == width));
    int[][] result = new int[lines.size()][width];
    for (int i = 0; i < result.length; i++) {
      String line = lines.get(i);
      for (int j = 0; j < width; j++) {
        result[i][j] = line.charAt(j) - '0';
      }
    }
    return result;
  }
}