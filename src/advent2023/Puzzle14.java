package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Éamonn McManus
 */
public class Puzzle14 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle14.class.getResourceAsStream("puzzle14.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      char[][] chars = new char[lines.size()][];
      for (int i = 0; i < lines.size(); i++) {
        chars[i] = lines.get(i).toCharArray();
      }

      // Part 1
      tiltNorth(chars);
      System.out.println("Load " + load(chars));

      // Part 2
      // The assumption here is that there is a cycle, such that the state repeats, possibly
      // after an initial sequence of states that don't repeat. So we just need to find the length
      // of that initial sequence and the length of the cycle, and be a bit careful about the exact
      // calculation.
      Set<CharWrapper> seen = new LinkedHashSet<>();
      CharWrapper seenWrapper;
      while (true) {
        cycle(chars);
        CharWrapper wrapper = CharWrapper.copyOf(chars);
        if (!seen.add(wrapper)) {
          seenWrapper = wrapper;
          break;
        }
      }
      List<CharWrapper> seenList = new ArrayList<>(seen);
      int cycleStart = seenList.indexOf(seenWrapper);
      // For the small case, we see this:
      // 1  2  3  4  5  6  7  8  9
      //      10 11 12 13 14 15 16
      //      17 18 19 20 21 22 23
      // seen.size() is 9 and cycleStart is 2. So the zero-origin index of the ith iteration is
      // 2 + (i - 2 - 1) % 7. For example, for the 11th it is 2 + 1. In general it is
      // cycleStart + (i - cycleStart - 1) % (seen.size() - cycleStart).
      System.out.println("Cycle after " + seen.size() + " iterations, starting at " + cycleStart);
      int billionI = cycleStart + (1_000_000_000 - cycleStart - 1) % (seen.size() - cycleStart);
      System.out.println(
          "Load for billionth same as for i="
              + billionI
              + " = "
              + load(seenList.get(billionI).chars));
    }
  }

  static class CharWrapper {
    private final char[][] chars;
    private int hashCode;

    private CharWrapper(char[][] chars) {
      this.chars = chars;
    }

    static CharWrapper copyOf(char[][] chars) {
      char[][] copy = new char[chars.length][];
      for (int i = 0; i < chars.length; i++) {
        copy[i] = chars[i].clone();
      }
      return new CharWrapper(copy);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof CharWrapper that) {
        if (this.chars.length != that.chars.length) {
          return false;
        }
        for (int i = 0; i < chars.length; i++) {
          if (!Arrays.equals(this.chars[i], that.chars[i])) {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    @Override
    public int hashCode() {
      if (hashCode == 0) {
        hashCode = Arrays.deepHashCode(new Object[] {chars});
      }
      return hashCode;
    }

    @Override
    public String toString() {
      List<String> strings = new ArrayList<>();
      for (char[] line : chars) {
        strings.add(new String(line));
      }
      return String.join("\n", strings);
    }
  }

  private static void show(char[][] chars) {
    for (char[] line : chars) {
      System.out.println(new String(line));
    }
  }

  private static long load(char[][] chars) {
    long load = 0;
    for (int i = 1; i <= chars.length; i++) {
      char[] line = chars[chars.length - i];
      for (int j = 0; j < line.length; j++) {
        if (line[j] == 'O') {
          load += i;
        }
      }
    }
    return load;
  }

  private static void cycle(char[][] chars) {
    tiltNorth(chars);
    tiltWest(chars);
    tiltSouth(chars);
    tiltEast(chars);
  }

  // Could probably have unified these four methods into a single one but I was lazy.

  private static void tiltNorth(char[][] chars) {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 1; i < chars.length; i++) {
        for (int j = 0; j < chars[i].length; j++) {
          if (chars[i][j] == 'O' && chars[i - 1][j] == '.') {
            changed = true;
            chars[i - 1][j] = 'O';
            chars[i][j] = '.';
          }
        }
      }
    }
  }

  private static void tiltWest(char[][] chars) {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < chars.length; i++) {
        for (int j = 1; j < chars[i].length; j++) {
          if (chars[i][j] == 'O' && chars[i][j - 1] == '.') {
            changed = true;
            chars[i][j - 1] = 'O';
            chars[i][j] = '.';
          }
        }
      }
    }
  }

  private static void tiltSouth(char[][] chars) {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < chars.length - 1; i++) {
        for (int j = 0; j < chars[i].length; j++) {
          if (chars[i][j] == 'O' && chars[i + 1][j] == '.') {
            changed = true;
            chars[i + 1][j] = 'O';
            chars[i][j] = '.';
          }
        }
      }
    }
  }

  private static void tiltEast(char[][] chars) {
    boolean changed = true;
    while (changed) {
      changed = false;
      for (int i = 0; i < chars.length; i++) {
        for (int j = 0; j < chars[i].length - 1; j++) {
          if (chars[i][j] == 'O' && chars[i][j + 1] == '.') {
            changed = true;
            chars[i][j + 1] = 'O';
            chars[i][j] = '.';
          }
        }
      }
    }
  }
}
