package advent2022;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle17 {
  private static final String SAMPLE = ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>";

  private static final List<String> ROCK_STRINGS = List.of(
      """
      ####
      """,

      """
      .#.
      ###
      .#.
      """,

      """
      ..#
      ..#
      ###
      """,

      """
      #
      #
      #
      #
      """,

      """
      ##
      ##
      """);

  private static final List<Rock> ROCKS = initRocks();

  private static List<Rock> initRocks() {
    Splitter splitter = Splitter.on('\n').omitEmptyStrings();
    ImmutableList.Builder<Rock> rocks = ImmutableList.builder();
    for (String rockString : ROCK_STRINGS) {
      Rock rock = Rock.forPattern(splitter.splitToList(rockString));
      rock.shiftedRight = rock;
      // This version is shifted right as far as it can go. Construct all the possible left shifts.
      Rock shifted;
      while ((shifted = rock.shiftedLeft()) != rock) {
        shifted.shiftedRight = rock;
        rock = shifted;
      }
      // Now shift right twice to get the starting position.
      rocks.add(rock.shiftedRight().shiftedRight());
    }
    return rocks.build();
  }

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle17.class.getResourceAsStream("puzzle17.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        checkArgument(lines.size() == 1, lines);
        String jets = lines.get(0);
        int jetI = 0;
        List<Integer> pile = new ArrayList<>();
        for (int i = 0; i < 2022; i++) {
          Rock rock = ROCKS.get(i % ROCKS.size());
          int pos = pile.size() + 3;
          while (true) {
            // Jet pushes rock.
            char c = jets.charAt(jetI);
            jetI = (jetI + 1) % jets.length();
            Rock shiftedRock = switch (c) {
              case '<' -> rock.shiftedLeft();
              case '>' -> rock.shiftedRight();
              default -> throw new AssertionError(c);
            };
            if (rockFits(shiftedRock, pos, pile)) {
              rock = shiftedRock;
            }

            // Rock falls if possible.
            if (pos == 0 || !rockFits(rock, pos - 1, pile)) {
              break;
            }
            pos--;
          }
          addRock(rock, pos, pile);
        }
        System.out.println(STR."Pile height for \{name} is \{pile.size()}");
      }
    }
  }

  private static boolean rockFits(Rock rock, int pos, List<Integer> pile) {
    // If h is the height of the rock, there are at most h rows of overlap between the pile and the
    // rock. Check them all.
    for (int i = 0; i < rock.bitmasks.length && pos + i < pile.size(); i++) {
      int pileBits = pile.get(pos + i);
      if ((pileBits & rock.bitmasks[i]) != 0) {
        return false;
      }
    }
    return true;
  }

  private static void addRock(Rock rock, int pos, List<Integer> pile) {
    int i;
    for (i = 0; i < rock.bitmasks.length; i++) {
      int rockBits = rock.bitmasks[i];
      if (pos + i < pile.size()) {
        int pileBits = pile.get(pos + i);
        assert (pileBits & rockBits) == 0;
        pile.set(pos + i, pileBits | rockBits);
      } else {
        pile.add(rockBits);
      }
    }
  }

  static String bitmaskString(int bitmask) {
    StringBuilder sb = new StringBuilder();
    for (int i = 6; i >= 0; i--) {
      sb.append((bitmask & (1 << i)) == 0 ? '.' : '#');
    }
    return sb.toString();
  }

  private static class Rock {
    final int[] bitmasks; // [0] is the bottom
    Rock shiftedLeft;
    Rock shiftedRight;

    Rock(int[] bitmasks) {
      this.bitmasks = bitmasks;
    }

    Rock shiftedLeft() {
      if (shiftedLeft == null) {
        int[] newBitmasks = new int[bitmasks.length];
        for (int i = 0; i < bitmasks.length; i++) {
          if ((bitmasks[i] & (1 << 6)) != 0) {
            shiftedLeft = this;
            break;
          }
          newBitmasks[i] = bitmasks[i] << 1;
        }
        if (shiftedLeft == null) {
          shiftedLeft = new Rock(newBitmasks);
        }
      }
      return shiftedLeft;
    }

    Rock shiftedRight() {
      assert shiftedRight != null;
      return shiftedRight;
    }

    static Rock forPattern(List<String> pattern) {
      int[] bitmasks = new int[pattern.size()];
      for (int i = 0; i < pattern.size(); i++) {
        int ii = pattern.size() - 1 - i;
        String s = new StringBuilder(pattern.get(ii)).reverse().toString();
        int bitmask = 0;
        for (int j = 0; j < s.length(); j++) {
          if (s.charAt(j) == '#') {
            bitmask |= 1 << j;
          }
        }
        bitmasks[i] = bitmask;
      }
      return new Rock(bitmasks);
    }

    private String cachedToString;

    @Override public String toString() {
      if (cachedToString == null) {
        cachedToString = String.join(
            "\n", Arrays.stream(bitmasks).mapToObj(m -> bitmaskString(m)).toList().reversed());
      }
      return cachedToString;
    }
  }
}
