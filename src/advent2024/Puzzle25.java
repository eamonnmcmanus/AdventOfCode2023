package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle25 {
  private static final String SAMPLE =
      """
      #####
      .####
      .####
      .####
      .#.#.
      .#...
      .....

      #####
      ##.##
      .#.##
      ...##
      ...#.
      ...#.
      .....

      .....
      #....
      #....
      #...#
      #.#.#
      #.###
      #####

      .....
      .....
      #.#..
      ###..
      ###.#
      ###.#
      #####

      .....
      .....
      .....
      #....
      #.#..
      #.#.#
      #####
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle25.class.getResourceAsStream("puzzle25.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        ImmutableList.Builder<Lock> locksBuilder = ImmutableList.builder();
        ImmutableList.Builder<Key> keysBuilder = ImmutableList.builder();
        for (int i = 0; i < lines.size(); i += 8) {
          checkArgument(i == 0 || lines.get(i - 1).isEmpty());
          List<String> sublines = lines.subList(i, i + 7);
          if (sublines.getFirst().equals("#####")) {
            locksBuilder.add(Lock.parse(sublines));
          } else if (sublines.getLast().equals("#####")) {
            keysBuilder.add(Key.parse(sublines));
          } else {
            throw new AssertionError(sublines.toString());
          }
        }
        int count = 0;
        for (Lock lock : locksBuilder.build()) {
          for (Key key : keysBuilder.build()) {
            if (key.fits(lock)) {
              count++;
            }
          }
        }
        System.out.printf("For %s, count is %d\n", name, count);
      }
    }
  }

  private record Lock(ImmutableList<Integer> heights) {
    static Lock parse(List<String> lines) {
      ImmutableList.Builder<Integer> heightsBuilder = ImmutableList.builder();
      for (int col = 0; col < 5; col++) {
        for (int row = 1; row < 7; row++) {
          if (lines.get(row).charAt(col) == '.') {
            heightsBuilder.add(row - 1);
            break;
          }
        }
      }
      var heights = heightsBuilder.build();
      checkState(heights.size() == 5);
      return new Lock(heights);
    }
  }

  private record Key(ImmutableList<Integer> heights) {
    boolean fits(Lock lock) {
      return IntStream.range(0, 5).allMatch(i -> lock.heights.get(i) + this.heights.get(i) < 6);
    }

    static Key parse(List<String> lines) {
      ImmutableList.Builder<Integer> heightsBuilder = ImmutableList.builder();
      for (int col = 0; col < 5; col++) {
        for (int row = 5; row >= 0; row--) {
          if (lines.get(row).charAt(col) == '.') {
            heightsBuilder.add(5 - row);
            break;
          }
        }
      }
      var heights = heightsBuilder.build();
      checkState(heights.size() == 5);
      return new Key(heights);
    }
  }
}