package advent2024;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.truth.Expect;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Rule;

/**
 * Tests that the results that the puzzle solutions produce have not changed.
 *
 * @author emcmanus
 */
public class PuzzleResultsTest {
  @Rule public Expect expect = Expect.create();

  private static final ImmutableMap<Class<?>, String> PUZZLE_RESULTS =
      ImmutableMap.ofEntries(
          entry(
              Puzzle1.class,
              """
              Total for sample is 11
              Similarity for sample is 31
              Total for problem is 1941353
              Similarity for problem is 22539317
              """),
          entry(
              Puzzle2.class,
              """
              Count for sample is 2
              Count with dampener for sample is 4
              Count for problem is 472
              Count with dampener for problem is 520
              """),
          entry(
              Puzzle3.class,
              """
              Part 1 sum for sample 1 is 161
              Part 2 sum for sample 1 is 161
              Part 1 sum for sample 2 is 161
              Part 2 sum for sample 2 is 48
              Part 1 sum for problem is 168539636
              Part 2 sum for problem is 97529391
              """),
          entry(
              Puzzle4.class,
              """
              Part 1 count for sample is 18
              Part 2 count for sample is 9
              Part 1 count for problem is 2543
              Part 2 count for problem is 1930
              """),
          entry(
              Puzzle5.class,
              """
              Part 1 total for sample is 143
              Part 2 total for sample is 123
              Part 1 total for problem is 4689
              Part 2 total for problem is 6336
              """),
          entry(
              Puzzle6.class,
              """
              Part 1 result for sample is 41
              Part 2 result for sample is 6
              Part 1 result for problem is 5531
              Part 2 result for problem is 2165
              """),
          entry(
              Puzzle7.class,
              """
              Solution for sample part 1: 3749
              Solution for sample part 2: 11387
              Solution for problem part 1: 12553187650171
              Solution for problem part 2: 96779702119491
              """),
          entry(
              Puzzle8.class,
              """
              Part 1 antinode count for sample is 14
              Part 2 antinode count for sample is 34
              Part 1 antinode count for problem is 256
              Part 2 antinode count for problem is 1005
              """),
          entry(
              Puzzle9.class,
              """
              Part 1 checksum for sample is 1928
              Part 2 checksum for sample is 2858
              Part 1 checksum for problem is 6288707484810
              Part 2 checksum for problem is 6311837662089
              """),
          entry(
              Puzzle10.class,
              """
              Part 1 total for sample is 36
              Part 2 total for sample is 81
              Part 1 total for problem is 501
              Part 2 total for problem is 1017
              """));

  @Test
  public void results() throws Exception {
    for (var entry : PUZZLE_RESULTS.entrySet()) {
      var oldOut = System.out;
      var bout = new ByteArrayOutputStream();
      System.setOut(new PrintStream(bout));
      try {
        var main = entry.getKey().getMethod("main", String[].class);
        main.invoke(null, (Object) new String[0]);
        String output = bout.toString(UTF_8);
        expect
            .withMessage("Output for %s", entry.getKey().getSimpleName())
            .that(output)
            .isEqualTo(entry.getValue());
      } finally {
        System.setOut(oldOut);
      }
    }
  }
}