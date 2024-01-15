package advent2022;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final List<Valve> valves;

  Puzzle16(Graph<Valve> graph, List<Valve> valves) {
    this.graph = graph;
    this.valves = valves;
    this.start = graph.nodes().stream().filter(v -> v.name.equals("AA")).findFirst().get();
  }

  private void part1(String name) {
    Map<State, Long> currentStates = Map.of(new State(start, start, new ValveSet()), 0L);
    for (int i = 1; i <= 30; i++) {
      Map<State, Long> nextStates = new HashMap<>();
      currentStates.forEach(
          (state, total) -> {
            long flowRate = state.open.flowRate();

            // We move.
            for (Valve ourNext : graph.successors(state.ourPos)) {
              nextStates.merge(new State(ourNext, state.elephantPos, state.open), total + flowRate, Long::max);
            }

            // We open.
            if (state.ourPos.flowRate > 0) {
              nextStates.merge(new State(state.ourPos, state.elephantPos, state.open.plus(state.ourPos)), total + flowRate, Long::max);
            }
          });
      currentStates = nextStates;
    }
    long max = currentStates.values().stream().max(Comparator.naturalOrder()).get();
    System.out.println(STR."Max for \{name} part 1 is \{max}");
  }

  // This stupidly literal implementation takes about 8 minutes to run. Looking at forum discussion
  // https://www.reddit.com/r/adventofcode/comments/zo21au/2022_day_16_approaches_and_pitfalls_discussion/
  // there are a couple of fairly easy things that could be done to make it much better if I had the patience.
  // First, we can eliminate valves from the graph that have zero flowrate, and instead link interesting
  // valves with edges that have a cost equal to the shortest path between them. We'd then have to run
  // through every path in the graph rather than using the Dynamic Programming approach here, but many
  // paths would be eliminated because the overall time would be too long. (This is not obviously true
  // but apparently is the case for the problem graph.)
  // Second, we could have the algorithm determine the best total flowrate for each set of valves
  // operated by a *single* agent, then find a combination of two sets from this result that are
  // complementary and that have the highest total. The two sets represent valves opened by us and
  // by the elephant.
  private void part2(String name) {
    Map<State, Long> currentStates = Map.of(new State(start, start, new ValveSet()), 0L);
    for (int i = 1; i <= 26; i++) {
      System.out.println(STR."Step \{i}, size \{currentStates.size()}");
      Map<State, Long> nextStates = new HashMap<>();
      currentStates.forEach(
          (state, total) -> {
            long flowRate = state.open.flowRate();

            Set<State> newStates = new HashSet<>();

            // We move.
            for (Valve ourNext : graph.successors(state.ourPos)) {
              // We move and elephant moves.
              for (Valve elephantNext : graph.successors(state.elephantPos)) {
                newStates.add(new State(ourNext, elephantNext, state.open));
              }
              // We move and elephant opens.
              newStates.add(new State(ourNext, state.elephantPos, state.open.plus(state.elephantPos)));
            }

            // We open and elephant moves.
            for (Valve elephantNext : graph.successors(state.elephantPos)) {
              newStates.add(new State(state.ourPos, elephantNext, state.open.plus(state.ourPos)));
            }
            // We open and elephant opens.
            ValveSet newOpen = state.open.plus(state.ourPos).plus(state.elephantPos);
            newStates.add(new State(state.ourPos, state.elephantPos, newOpen));
            for (State newState : newStates) {
              nextStates.merge(newState, total + flowRate, Long::max);
            }
          });

      // Prune nextStates. For each combination of our position and the elephant's position, look
      // at all the sets of open valves. If any set contains another set and has at least as high
      // a total, then the other set can be discarded.
      // This is obviously quadratic, which is not great. We could improve it somewhat by ordering
      // the StateAndTotal values by the bitmask of the ValveSet, since if one set contains another
      // then its bitmask will be numerically greater. We could also skip the pruning at the last
      // step since we're only going to be looking at the max value anyway. But changing the overall
      // approach would obviously be much better than fiddling with those details.
      Map<Position, Set<StateAndTotal>> positionToStates = new HashMap<>();
      nextStates.forEach(
          (state, total) ->
              positionToStates
                  .computeIfAbsent(new Position(state.ourPos, state.elephantPos), _ -> new HashSet<>())
                  .add(new StateAndTotal(state, total)));
      positionToStates.forEach(
          (position, stateAndTotal) -> {
            for (var it = stateAndTotal.iterator(); it.hasNext(); ) {
              var thisOne = it.next();
              for (var thatOne : stateAndTotal) {
                if (thisOne != thatOne && thatOne.betterThan(thisOne)) {
                  it.remove();
                  break;
                }
              }
            }
          });
      // Now reconstruct the new currentStates from the pruned nextStates.
      currentStates = positionToStates.values().stream()
          .flatMap(s -> s.stream())
          .collect(toImmutableMap(StateAndTotal::state, StateAndTotal::total));
    }
    long max = currentStates.values().stream().max(Comparator.naturalOrder()).get();
    System.out.println(STR."Max for \{name} part 2 is \{max}");
  }

  record State(Valve ourPos, Valve elephantPos, ValveSet open) {}

  record StateAndTotal(State state, long total) {
    boolean betterThan(StateAndTotal that) {
      return this.total >= that.total && this.state.open.containsAll(that.state.open);
    }
  }

  record Position(Valve ourPos, Valve elephantPos) {}

  private static final Pattern VALVE_PATTERN = Pattern.compile(
      "Valve (..) has flow rate=(\\d+); tunnels? leads? to valves? (.*)");

  private static Puzzle16 parseGraph(List<String> lines) {
    ImmutableGraph.Builder<Valve> builder = GraphBuilder.<Valve>undirected().immutable();

    // Add the nodes
    Map<String, Valve> map = new TreeMap<>();
    ImmutableList.Builder<Valve> valves = ImmutableList.builder();
    int valveNumber = 0;
    for (String line : lines) {
      Matcher matcher = VALVE_PATTERN.matcher(line);
      checkArgument(matcher.matches(), line);
      Valve valve = new Valve(valveNumber++, matcher.group(1), Integer.parseInt(matcher.group(2)));
      map.put(valve.name, valve);
      valves.add(valve);
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
    return new Puzzle16(graph, valves.build());
  }

  record Valve(int valveNumber, String name, int flowRate) {
    @Override public String toString() {
      return STR."\{name}(\{flowRate})";
    }

    @Override public boolean equals(Object o) {
      return o instanceof Valve that && this.valveNumber == that.valveNumber;
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

    long flowRate() {
      long total = 0;
      long m = mask;
      while (m != 0) {
        int b = Long.numberOfTrailingZeros(m);
        total += valves.get(b).flowRate;
        m &= ~(1L << b);
      }
      return total;
    }

    boolean containsAll(ValveSet that) {
      return (that.mask & ~this.mask) == 0;
    }

    @Override public boolean equals(Object o) {
      return o instanceof ValveSet that && this.mask == that.mask;
    }

    @Override public int hashCode() {
      return Long.hashCode(mask);
    }
  };
}
