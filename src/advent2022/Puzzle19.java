package advent2022;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.max;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
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
        for (Blueprint blueprint : blueprints) {
          check(blueprint, 24);
        }
      }
    }
  }

  private static void check(Blueprint blueprint, int minutes) {
    List<Status> statuses = List.of(Status.INITIAL);
    for (int i = 1; i <= minutes; i++) {
      List<Status> newStatuses = new ArrayList<>();
      for (Status status : statuses) {
        Status newStatus = status.next();
        // Consider doing nothing.
        addStatus(newStatuses, newStatus);
        // Consider building an ore robot.
        if (status.resources.contains(blueprint.oreRobotCost)) {
          addStatus(
              newStatuses,
              new Status(
                  newStatus.resources.minus(blueprint.oreRobotCost),
                  newStatus.oreRobots + 1,
                  newStatus.clayRobots,
                  newStatus.obsidianRobots,
                  newStatus.geodeRobots,
                  newStatus.geodes));
        }
        // Consider building a clay robot.
        if (status.resources.contains(blueprint.clayRobotCost)) {
          addStatus(
              newStatuses,
              new Status(
                  newStatus.resources.minus(blueprint.clayRobotCost),
                  newStatus.oreRobots,
                  newStatus.clayRobots + 1,
                  newStatus.obsidianRobots,
                  newStatus.geodeRobots,
                  newStatus.geodes));
        }
        // Consider building an obsidian robot.
        if (status.resources.contains(blueprint.obsidianRobotCost)) {
          addStatus(
              newStatuses,
              new Status(
                  newStatus.resources.minus(blueprint.obsidianRobotCost),
                  newStatus.oreRobots,
                  newStatus.clayRobots,
                  newStatus.obsidianRobots + 1,
                  newStatus.geodeRobots,
                  newStatus.geodes));
        }
        // Consider building a geode robot.
        if (status.resources.contains(blueprint.geodeRobotCost)) {
          addStatus(
              newStatuses,
              new Status(
                  newStatus.resources.minus(blueprint.geodeRobotCost),
                  newStatus.oreRobots,
                  newStatus.clayRobots,
                  newStatus.obsidianRobots,
                  newStatus.geodeRobots + 1,
                  newStatus.geodes));
        }
      }
      statuses = newStatuses;
      System.out.println(STR."After minute \{i}, number of statuses is \{statuses.size()}");
    }
  }

  private static void addStatus(List<Status> statuses, Status status) {
    for (Iterator<Status> it = statuses.iterator(); it.hasNext(); ) {
      Status existing = it.next();
      if (existing.contains(status)) {
        return;
      }
      if (status.contains(existing)) {
        it.remove();
      }
    }
    statuses.add(status);
  }

  record Resources(int ore, int clay, int obsidian) {
    static final Resources ZERO = new Resources(0, 0, 0);
    boolean contains(Resources that) {
      return this.ore >= that.ore && this.clay >= that.clay && this.obsidian >= that.obsidian;
    }

    Resources minus(Resources that) {
      return new Resources(this.ore - that.ore, this.clay - that.clay, this.obsidian - that.obsidian);
    }
  }

  record Blueprint(
      int id,
      Resources oreRobotCost,
      Resources clayRobotCost,
      Resources obsidianRobotCost,
      Resources geodeRobotCost) {
  }

  record Status(
      Resources resources,
      int oreRobots, int clayRobots, int obsidianRobots, int geodeRobots,
      int geodes) {
    static final Status INITIAL = new Status(Resources.ZERO, 1, 0, 0, 0, 0);

    Status next() {
      Resources nextResources = new Resources(
          resources.ore + oreRobots,
          resources.clay + clayRobots,
          resources.obsidian + obsidianRobots);
      return new Status(
          nextResources,
          oreRobots, clayRobots, obsidianRobots, geodeRobots,
          geodes + geodeRobots);
    }

    boolean contains(Status that) {
      return resources.contains(that.resources)
          && oreRobots >= that.oreRobots
          && clayRobots >= that.clayRobots
          && obsidianRobots >= that.obsidianRobots
          && geodeRobots >= that.geodeRobots
          && geodes >= that.geodes;
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
