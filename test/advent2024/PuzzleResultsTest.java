package advent2024;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.truth.Expect;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests that the results that the puzzle solutions produce have not changed. When there is shared
 * code between puzzles, if we modify it for a later puzzle we don't want to invalidate earlier
 * puzzles.
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
              """),
          entry(
              Puzzle11.class,
              """
              For sample, after 25 blinks, number of stones 55312
              For sample, after 75 blinks, number of stones 65601038650482
              For problem, after 25 blinks, number of stones 186424
              For problem, after 75 blinks, number of stones 219838428124832
              """));

  @Test
  public void results() throws Exception {
    Class<?> slowest = null;
    long slowestTime = 0;
    for (var entry : PUZZLE_RESULTS.entrySet()) {
      var oldOut = System.out;
      var bout = new ByteArrayOutputStream();
      System.setOut(new PrintStream(bout));
      try {
        Class<?> puzzleClass = entry.getKey();
        var main = puzzleClass.getMethod("main", String[].class);
        long startTime = System.nanoTime();
        main.invoke(null, (Object) new String[0]);
        long elapsed = System.nanoTime() - startTime;
        if (elapsed > slowestTime) {
          slowest = puzzleClass;
          slowestTime = elapsed;
        }
        String output = bout.toString(UTF_8);
        expect
            .withMessage("Output for %s", puzzleClass.getSimpleName())
            .that(output)
            .isEqualTo(entry.getValue());
      } finally {
        System.setOut(oldOut);
      }
    }
    System.out.printf(
        "Slowest puzzle was %s with elapsed time %.3fs\n",
        slowest.getSimpleName(), slowestTime / 1e9);
  }
}