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
        part1(name, blueprints);
        part2(name, blueprints);
      }
    }
  }

  private static void part1(String name, List<Blueprint> blueprints) {
    long sum = blueprints.stream().mapToLong(bp -> bp.id * max(name, bp, 24)).sum();
    System.out.println("For " + name + ", sum is " + sum);
  }

  private static void part2(String name, List<Blueprint> blueprints) {
    if (!name.equals("problem")) {
      // The samples actually provoke a much longer run time than the problems.
      return;
    }
    long product = blueprints.stream().limit(3).mapToLong(bp -> max(name, bp, 32)).reduce(1L, (a, b) -> a * b);
    System.out.println("For " + name + ", product is " + product);
  }

  // This is pretty hokey and literal, but it gets the right result. We basically track all the
  // possible states after each minute, with two optimizations: (1) if a state has fewer resources
  // of every type than another state in the same minute, there is no point in keeping it; (2)
  // there is no point in having more ore robots than the maximum amount of ore that any maufacture
  // needs, and so on for the other robot types. We handle (1) in an ugly quadratic way, though it
  // is fairly easy to imagine optimized data structures that would be at least somewhat better.
  private static long max(String name, Blueprint blueprint, int minutes) {
    // There is no point in manufacturing more ore robots than the maximum ore cost of any robot
    // kind, and so on for the others. So determine what those maxima are.
    List<Resources> costs = List.of(
        blueprint.oreRobotCost, blueprint.clayRobotCost, blueprint.obsidianRobotCost, blueprint.geodeRobotCost);
    int maxOreCost = costs.stream().mapToInt(Resources::ore).max().getAsInt();
    int maxClayCost = costs.stream().mapToInt(Resources::clay).max().getAsInt();
    int maxObsidianCost = costs.stream().mapToInt(Resources::obsidian).max().getAsInt();
    List<Status> statuses = List.of(Status.INITIAL);
    for (int i = 1; i <= minutes; i++) {
      List<Status> newStatuses = new ArrayList<>();
      for (Status status : statuses) {
        Status newStatus = status.next();
        // Consider doing nothing.
        addStatus(newStatuses, newStatus);
        // Consider building an ore robot.
        if (status.oreRobots < maxOreCost && status.resources.contains(blueprint.oreRobotCost)) {
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
        if (status.clayRobots < maxClayCost && status.resources.contains(blueprint.clayRobotCost)) {
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
        if (status.obsidianRobots < maxObsidianCost && status.resources.contains(blueprint.obsidianRobotCost)) {
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
      System.out.println("After minute " + i + ", number of statuses is " + statuses.size());
    }
    return statuses.stream().mapToInt(Status::geodes).max().getAsInt();
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
