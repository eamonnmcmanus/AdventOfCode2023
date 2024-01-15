package advent2022;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Long.max;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle16 {
  private static final String SAMPLE =
      """
      Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
      Valve BB has flow rate=13; tunnels lead to valves CC, AA
      Valve CC has flow rate=2; tunnels lead to valves DD, BB
      Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
      Valve EE has flow rate=3; tunnels lead to valves FF, DD
      Valve FF has flow rate=0; tunnels lead to valves EE, GG
      Valve GG has flow rate=0; tunnels lead to valves FF, HH
      Valve HH has flow rate=22; tunnel leads to valve GG
      Valve II has flow rate=0; tunnels lead to valves AA, JJ
      Valve JJ has flow rate=21; tunnel leads to valve II
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle16.class.getResourceAsStream("puzzle16.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Puzzle16 puzzle = parseGraph(lines);
        puzzle.part1(name);
        puzzle.part2(name);
      }
    }
  }

  private final Graph<Valve> graph;
  private final Valve start;
  private final List<Valve> nonZeroValves;

  Puzzle16(Graph<Valve> graph, List<Valve> nonZeroValves) {
    this.graph = graph;
    this.nonZeroValves = nonZeroValves;
    this.start = graph.nodes().stream().filter(v -> v.name.equals("AA")).findFirst().get();
  }

  private void part1(String name) {
    Map<ValveSet, Long> bestSets = bestSets(30);
    long max = Collections.max(bestSets.values());
    System.out.println(STR."Max for \{name} part 1 is \{max}");
  }

  // For part 2, we compute all the possible sets of valves that can be open after 26 minutes, and
  // we look for the pair of sets from within those that have no intersection and that have the
  // highest combined total. That corresponds to the elephant and us each opening a distinct set
  // of valves. I got this idea from Martijn Pieters here:
  // https://www.reddit.com/r/adventofcode/comments/zo21au/2022_day_16_approaches_and_pitfalls_discussion/
  // My previous solution was too literal, tracing all possible states of our position and the
  // elephant's position and the total set of open valves. Even with optimization it took about
  // 8 minutes to run, versus less than a second for the solution here.
  private void part2(String name) {
    Map<ValveSet, Long> bestSets = bestSets(26);
    long max = 0;
    for (ValveSet set1 : bestSets.keySet()) {
      for (ValveSet set2 : bestSets.keySet()) {
        if (set1.disjoint(set2)) {
          long total = bestSets.get(set1) + bestSets.get(set2);
          max = max(max, total);
        }
      }
    }
    System.out.println(STR."Max for \{name} part 2 is \{max}");
  }

  private Map<ValveSet, Long> bestSets(int steps) {
    Map<State, Long> currentStates = Map.of(new State(start, new ValveSet()), 0L);
    for (int i = 1; i <= steps; i++) {
      Map<State, Long> nextStates = new HashMap<>();
      currentStates.forEach(
          (state, total) -> {
            long flowRate = state.open.flowRate();

            // We move.
            for (Valve ourNext : graph.successors(state.ourPos)) {
              nextStates.merge(new State(ourNext, state.open), total + flowRate, Long::max);
            }

            // We open.
            if (state.ourPos.flowRate > 0) {
              nextStates.merge(new State(state.ourPos, state.open.plus(state.ourPos)), total + flowRate, Long::max);
            }
          });
      currentStates = nextStates;
    }
    Map<ValveSet, Long> result = new HashMap<>();
    currentStates.forEach((state, total) -> result.merge(state.open, total, Long::max));
    return result;
  }

  record State(Valve ourPos, ValveSet open) {}

  private static final Pattern VALVE_PATTERN = Pattern.compile(
      "Valve (..) has flow rate=(\\d+); tunnels? leads? to valves? (.*)");

  private static Puzzle16 parseGraph(List<String> lines) {
    ImmutableGraph.Builder<Valve> builder = GraphBuilder.<Valve>undirected().immutable();

    // Add the nodes
    Map<String, Valve> map = new TreeMap<>();
    ImmutableList.Builder<Valve> nonZeroValves = ImmutableList.builder();
    int valveNumber = 0;
    for (String line : lines) {
      Matcher matcher = VALVE_PATTERN.matcher(line);
      checkArgument(matcher.matches(), line);
      Valve valve = new Valve(valveNumber, matcher.group(1), Integer.parseInt(matcher.group(2)));
      map.put(valve.name, valve);
      if (valve.flowRate > 0) {
        nonZeroValves.add(valve);
        valveNumber++;
      }
      builder.addNode(valve);
    }

    // Add the edges
    for (String line : lines) {
      Matcher matcher = VALVE_PATTERN.matcher(line);
      checkArgument(matcher.matches(), line);
      Valve valve = map.get(matcher.group(1));
      List<String> targets = Splitter.on(", ").splitToList(matcher.group(3));
      for (String target : targets) {
        builder.putEdge(valve, map.get(target));
      }
    }

    ImmutableGraph<Valve> graph = builder.build();
    return new Puzzle16(graph, nonZeroValves.build());
  }

  record Valve(int valveNumber, String name, int flowRate) {
    @Override public String toString() {
      return STR."\{name}(\{flowRate})";
    }

    @Override public boolean equals(Object o) {
      return o instanceof Valve that && this.valveNumber == that.valveNumber && this.name.equals(that.name);
    }

    @Override public int hashCode() {
      return valveNumber;
    }
  }

  private class ValveSet {
    private final long mask;

    ValveSet(long mask) {
      this.mask = mask;
    }

    ValveSet() {
      this(0);
    }

    ValveSet plus(Valve valve) {
      if (valve.flowRate == 0) {
        return this;
      }
      long m = mask | (1L << valve.valveNumber);
      return (m == mask) ? this : new ValveSet(m);
    }

    boolean disjoint(ValveSet that) {
      return (this.mask & that.mask) == 0;
    }

    long flowRate() {
      long total = 0;
      long m = mask;
      while (m != 0) {
        int b = Long.numberOfTrailingZeros(m);
        total += nonZeroValves.get(b).flowRate;
        m &= ~(1L << b);
      }
      return total;
    }

    @Override public boolean equals(Object o) {
      return o instanceof ValveSet that && this.mask == that.mask;
    }

    @Override public int hashCode() {
      return Long.hashCode(mask);
    }
  };
}
