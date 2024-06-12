package advent2022;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.max;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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

        part1(name, jets);

        part2(name, jets);
      }
    }
  }

  private static void part1(String name, String jets) {
    // Part 1.
    Pile pile = new Pile(jets);
    for (int i = 0; i < 2022; i++) {
      pile.addRock();
    }
    System.out.println("Pile height for " + name + " is " + pile.height());
  }

  // For Part 2, we use a heuristic that may not be entirely sound but appears to work. For each
  // of the 7 possible columns, we record the highest row that has a rock in that column. Then we
  // examine the gap between the top of the pile and this row. The 7 gap values plus the positions
  // in the rock cycle and the jet cycle form the key that we use to detect a loop.
  // I think it's possible for this heuristic to fail, because the trajectory of a falling rock
  // doesn't depend just on the heights of these columns. You could for example have a rock blown
  // into a gap under an overlay:
  //
  // ....@..
  // ....@..
  // ..@@@..
  // ##...##
  // #....##
  // #######
  //
  // After dropping 2, this could end up like this, because it gets blown into the gap:
  // ...@...
  // ##.@.##
  // #@@@.##
  // #######
  //
  // Meanwhile this configuration would have the same gap signature, but would not produce the same
  // result:
  // ....@..
  // ....@..
  // ..@@@..
  // ##...##
  // ##...##
  // #######

  private static void part2(String name, String jets) {
    // Part 2.
    Pile pile = new Pile(jets);
    Map<CycleKey, Integer> cycleMap = new LinkedHashMap<>();
    int i;
    CycleKey state = null;
    for (i = 0; i < 100_000_000; i++) {
      state = pile.state();
      if (cycleMap.containsKey(state)) {
        System.out.println("Cycle detected at i=" + i + ": " + state);
        break;
      }
      cycleMap.put(state, pile.height());
      pile.addRock();
    }
    // We've detected a cycle. Let's say the cycle looks like this, with height deltas and totals shown:
    //   0  1  2  3  4  5  6  7  8  9 10
    //   a  b  c  d  e  c  d  e  c  d  e...
    //   2  3  3  4  5  3  4  5  3  4  5      <- height contributed
    //      2  5  8 12 17 20 24 29 32 36 41   <- height before adding
    // So the zero-origin position of the first element (x1) of the cycle is 2 and the length of the
    // cycle (cl) is 3. If h1 is the height of the pile just before x1 was added, and h2 is the
    // height of the pile when the cycle was detected (just before the repeating rock was added),
    // then each iteration of the cycle adds (h2 - h1) to the height. After i rocks, the total
    // height consists of:
    //   h1 for the first x1 rocks
    //   (h2 - h1) * ⌊(i - x1) / cl⌋ for the cycle iterations
    //   h3, the height of the incomplete cycle.
    // The incomplete cycle has length il = (i - x1) mod cl and its height h3 is the height at
    // index x1 + il minus h1.
    // In the example, say we want to know the height after i = 10 rocks, which should be 36. We have
    // x1 = 2, h1 = 5, h2 = 17, (h2 - h1) = 12, i - x1 = 8, ⌊(i - x1) / cl⌋ = ⌊8 / 3⌋ = 2, so the
    // cycle iterations contribute 2×12 = 24 to the height. The incomplete cycle has length
    // (10 - 2) mod 3 = 2 and its height is the difference between the height just before adding
    // zero-origin index x1 and just before zero-origin index x1+2, 12 - 5 = 7. For a total of
    // 5 + 24 + 7 = 36.
    List<CycleKey> states = new ArrayList<>(cycleMap.keySet());
    int x1 = states.indexOf(state);
    int h1 = cycleMap.get(state);
    int h2 = pile.height();
    int cl = i - x1;
    long target = 1_000_000_000_000L;
    long cycleIterations = (target - x1) / cl;
    long heightFromCycles = (h2 - h1) * cycleIterations;
    int incomplete = Math.toIntExact((target - x1) % cl);
    int h3 = cycleMap.get(states.get(x1 + incomplete)) - h1;
    long total = h1 + heightFromCycles + h3;
    System.out.println("Big pile height for " + name + " is " + total);
  }

  record CycleKey(int rockI, int jetI, List<Integer> columnHeights) {}

  private static class Pile {
    final String jets;
    final List<Integer> pile = new ArrayList<>();
    final List<Integer> lastRock = new ArrayList<>(Collections.nCopies(7, -1));
    int rockI = 0;
    int jetI = 0;

    Pile(String jets) {
      this.jets = jets;
    }

    CycleKey state() {
      return new CycleKey(rockI, jetI, columnHeights());
    }

    void addRock() {
      Rock rock = ROCKS.get(rockI);
      rockI = (rockI + 1) % ROCKS.size();
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
      overlayRock(rock, pos, pile);
      for (int i = pile.size() - 1; i >= 0 && i >= pile.size() - 4; i--) {
        int row = pile.get(i);
        for (int b = 0; b < 7; b++) {
          if ((row & (1 << b)) != 0) {
            lastRock.set(b, max(lastRock.get(b), i));
          }
        }
      }
    }

    List<Integer> columnHeights() {
      return lastRock.stream().map(i -> pile.size() - i).toList();
    }

    int height() {
      return pile.size();
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

  private static void overlayRock(Rock rock, int pos, List<Integer> pile) {
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
