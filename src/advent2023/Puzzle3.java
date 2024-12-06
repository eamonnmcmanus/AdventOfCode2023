package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle3 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle3.class.getResourceAsStream("puzzle3.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));

      // Part 1
      int sum = 0;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        for (int j = 0; j < line.length(); ) {
          if (Character.isDigit(line.charAt(j))) {
            int end;
            for (end = j + 1; end < line.length() && Character.isDigit(line.charAt(end)); end++) {}
            if (sym(lines, i - 1, j - 1)
                || sym(lines, i, j - 1)
                || sym(lines, i + 1, j - 1)
                || sym(lines, i - 1, end)
                || sym(lines, i, end)
                || sym(lines, i + 1, end)
                || adjacent(lines, i, j, end)) {
              sum += Integer.parseInt(line.substring(j, end));
            }
            j = end;
          } else {
            j++;
          }
        }
      }
      System.out.println("Sum is " + sum);

      // Part 2
      int ratioSum = 0;
      // For each cell in the grid, record the number that starts in that cell, or 0 if none.
      int[][] ints = new int[lines.size()][lines.get(0).length()];
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        for (int j = 0; j < line.length(); ) {
          if (Character.isDigit(line.charAt(j))) {
            int end;
            for (end = j + 1; end < line.length() && Character.isDigit(line.charAt(end)); end++) {}
            int n = Integer.parseInt(line.substring(j, end));
            for (int jj = j; jj < end; jj++) {
              ints[i][jj] = n;
            }
            j = end;
          } else {
            j++;
          }
        }
      }
      // Now scan for * symbols and find the adjacent numbers. This is a little tedious.
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        for (int j = 0; j < line.length(); j++) {
          if (line.charAt(j) == '*') {
            List<Integer> numbers = new ArrayList<>();
            if (i > 0) {
              if (ints[i - 1][j] > 0) {
                numbers.add(ints[i - 1][j]);
              } else {
                if (j > 0 && ints[i - 1][j - 1] > 0) {
                  numbers.add(ints[i - 1][j - 1]);
                }
                if (j < line.length() && ints[i - 1][j + 1] > 0) {
                  numbers.add(ints[i - 1][j + 1]);
                }
              }
            }
            if (j > 0 && ints[i][j - 1] > 0) {
              numbers.add(ints[i][j - 1]);
            }
            if (j < line.length() && ints[i][j + 1] > 0) {
              numbers.add(ints[i][j + 1]);
            }
            if (i < lines.size() - 1) {
              if (ints[i + 1][j] > 0) {
                numbers.add(ints[i + 1][j]);
              } else {
                if (j > 0 && ints[i + 1][j - 1] > 0) {
                  numbers.add(ints[i + 1][j - 1]);
                }
                if (j < line.length() && ints[i + 1][j + 1] > 0) {
                  numbers.add(ints[i + 1][j + 1]);
                }
              }
            }
            assert numbers.size() < 3;
            assert !numbers.contains(0);
            if (numbers.size() == 2) {
              ratioSum += numbers.get(0) * numbers.get(1);
            }
          }
        }
      }
      System.out.println("Ratio sum " + ratioSum);
    }
  }

  private static boolean sym(List<String> lines, int i, int j) {
    if (i < 0 || i >= lines.size() || j < 0) {
      return false;
    }
    String line = lines.get(i);
    if (j >= line.length()) {
      return false;
    }
    char c = line.charAt(j);
    return c != '.' && !Character.isDigit(c);
  }

  private static boolean adjacent(List<String> lines, int i, int start, int end) {
    for (int j = start; j < end; j++) {
      if (sym(lines, i - 1, j) || sym(lines, i + 1, j)) {
        return true;
      }
    }
    return false;
  }
}
