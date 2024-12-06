package advent2022;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author emcmanus@google.com (Éamonn McManus)
 */
public class Puzzle8 {
  private static final String SAMPLE =
      """
      30373
      25512
      65332
      33549
      35390
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle8.class.getResourceAsStream("puzzle8.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int count = 0;
        int maxI = -1, maxJ = -1;
        long maxScore = -1;
        for (int i = 0; i < lines.size(); i++) {
          for (int j = 0; j < lines.get(i).length(); j++) {
            if (visible(lines, i, j)) {
              count++;
            }
            long score = score(lines, i, j);
            if (score > maxScore) {
              maxScore = score;
              maxI = i;
              maxJ = j;
            }
          }
        }
        System.out.println("Visible for " + name + ": " + count);
        System.out.println("Best score for " + name + ": " + maxScore + " at " + maxI + "," + maxJ);
      }
    }
  }

  private static boolean visible(List<String> lines, int i, int j) {
    return visibleUp(lines, i, j)
        || visibleDown(lines, i, j)
        || visibleLeft(lines, i, j)
        || visibleRight(lines, i, j);
  }

  private static boolean visibleUp(List<String> lines, int i, int j) {
    char c = lines.get(i).charAt(j);
    for (int k = i - 1; k >= 0; k--) {
      if (lines.get(k).charAt(j) >= c) {
        return false;
      }
    }
    return true;
  }

  private static boolean visibleDown(List<String> lines, int i, int j) {
    char c = lines.get(i).charAt(j);
    for (int k = i + 1; k < lines.size(); k++) {
      if (lines.get(k).charAt(j) >= c) {
        return false;
      }
    }
    return true;
  }

  private static boolean visibleLeft(List<String> lines, int i, int j) {
    String line = lines.get(i);
    char c = line.charAt(j);
    for (int k = j - 1; k >= 0; k--) {
      if (line.charAt(k) >= c) {
        return false;
      }
    }
    return true;
  }

  private static boolean visibleRight(List<String> lines, int i, int j) {
    String line = lines.get(i);
    char c = line.charAt(j);
    for (int k = j + 1; k < line.length(); k++) {
      if (line.charAt(k) >= c) {
        return false;
      }
    }
    return true;
  }

  private static long score(List<String> lines, int i, int j) {
    return scoreUp(lines, i, j)
        * scoreDown(lines, i, j)
        * scoreLeft(lines, i, j)
        * scoreRIght(lines, i, j);
  }

  private static long scoreUp(List<String> lines, int i, int j) {
    char c = lines.get(i).charAt(j);
    for (int k = i - 1; k >= 0; k--) {
      if (lines.get(k).charAt(j) >= c) {
        return i - k;
      }
    }
    return i;
  }

  private static long scoreDown(List<String> lines, int i, int j) {
    char c = lines.get(i).charAt(j);
    for (int k = i + 1; k < lines.size(); k++) {
      if (lines.get(k).charAt(j) >= c) {
        return k - i;
      }
    }
    return lines.size() - 1 - i;
  }

  private static long scoreLeft(List<String> lines, int i, int j) {
    String line = lines.get(i);
    char c = line.charAt(j);
    for (int k = j - 1; k >= 0; k--) {
      if (line.charAt(k) >= c) {
        return j - k;
      }
    }
    return j;
  }

  private static long scoreRIght(List<String> lines, int i, int j) {
    String line = lines.get(i);
    char c = line.charAt(j);
    for (int k = j + 1; k < line.length(); k++) {
      if (line.charAt(k) >= c) {
        return k - j;
      }
    }
    return line.length() - 1 - j;
  }
}
