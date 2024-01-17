package advent2022;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.max;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle19 {
  private static final String SAMPLE =
      """
      Blueprint 1:\
        Each ore robot costs 4 ore.\
        Each clay robot costs 2 ore.\
        Each obsidian robot costs 3 ore and 14 clay.\
        Each geode robot costs 2 ore and 7 obsidian.
      Blueprint 2:\
        Each ore robot costs 2 ore.\
        Each clay robot costs 3 ore.\
        Each obsidian robot costs 3 ore and 8 clay.\
        Each geode robot costs 3 ore and 12 obsidian.
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle19.class.getResourceAsStream("puzzle19.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Blueprint> blueprints = lines.stream().map(line -> parseBlueprint(line)).toList();
        System.out.println(blueprints);
      }
    }
  }

  record Resources(int ore, int clay, int obsidian) {
    boolean contains(Resources that) {
      return this.ore >= that.ore && this.clay >= that.clay && this.obsidian >= that.obsidian;
    }

    Resources minus(Resources that) {
      return new Resources(this.ore - that.ore, this.clay - that.clay, this.obsidian - that.obsidian);
    }
  }

  record Blueprint(
      int id, Resources oreCost, Resources clayCost, Resources obsidianCost, Resources geodeCost) {
    int evaluate(
        int remaining, int geodes,
        int oreRobots, int clayRobots, int obsidianRobots, int geodeRobots,
        Resources resources) {
      if (remaining == 0) {
        return geodes;
      }
      int best = geodes;
      if (resources.contains(oreCost)) {
        best = max(best,
            evaluate(
                remaining - 1, geodes,
                oreRobots + 1, clayRobots, obsidianRobots, geodeRobots,
                resources.minus(oreCost)));
      }
      throw new AssertionError();
    }
  }

  private static final Pattern BLUEPRINT_PATTERN =
      Pattern.compile(
          """
          Blueprint (\\d+):\\s*\
          Each ore robot costs (\\d+) ore\\.\\s*\
          Each clay robot costs (\\d+) ore\\.\\s*\
          Each obsidian robot costs (\\d+) ore and (\\d+) clay\\.\\s*\
          Each geode robot costs (\\d+) ore and (\\d+) obsidian\\.\
          """);

  private static final Blueprint parseBlueprint(String line) {
    Matcher m = BLUEPRINT_PATTERN.matcher(line);
    checkArgument(m.matches(), line);
    int[] ints = IntStream.rangeClosed(1, m.groupCount())
        .mapToObj(m::group)
        .mapToInt(Integer::parseInt)
        .toArray();
    return new Blueprint(
        ints[0],
        new Resources(ints[1], 0, 0),
        new Resources(ints[2], 0, 0),
        new Resources(ints[3], ints[4], 0),
        new Resources(ints[5], 0, ints[6]));
  }
}
