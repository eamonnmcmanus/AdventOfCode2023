package advent2023;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.math.LongMath;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle8 {
  private final Map<String, List<String>> map;
  private final String directions;

  Puzzle8(Map<String, List<String>> map, String directions) {
    this.map = map;
    this.directions = directions;
  }

  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle8.class.getResourceAsStream("puzzle8.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      assert lines.get(1).isEmpty();
      String directions = lines.getFirst();
      Map<String, List<String>> map = parseMap(lines.subList(2, lines.size()));

      // Part 1.
      long steps = 0;
      String node = "AAA";
      for (int dirIndex = 0; !node.equals("ZZZ"); dirIndex = (dirIndex + 1) % directions.length(), steps++) {
        List<String> fork = map.get(node);
        int index = indexFor(directions.charAt(dirIndex));
        node = fork.get(index);
      }
      System.out.println(STR."Steps \{steps}");

      // Part 2.
      new Puzzle8(map, directions).solve();
    }
  }

  /*
   * The solution here isn't completely general. The idea is that we're going to go through some
   * initial number of states and then each of the parallel paths is going to cycle, with different
   * cycle lengths. The code here hardcodes that initial number as 3 based on observation. (I hadn't
   * realized that everyone gets different input data, though it may be that they all have 3 initial
   * states.) Then it also happens that each of the cycles reaches its Z state 3 steps before the
   * end, though again that might not be true for all inputs. The two 3s cancel each other out so
   * the solution is just the LCM of the cycle lengths.
   */
  private void solve() {
    List<State> states = map.keySet().stream().filter(s -> s.endsWith("A")).map(s -> new State(s, 0)).toList();
    System.out.println(STR."States \{states}");
    // Pass through the initial states before cycling begins.
    states = nextStates(states);
    states = nextStates(states);
    states = nextStates(states);
    // Compute the cycle length for each of the parallel states.
    long lcm = 1;
    for (State state : states) {
      Cycle cycle = cycle(state);
      System.out.println(STR."Start at \{state}, cycle starts at \{cycle.startIndex}, length \{cycle.states.size()} - \{cycle.startIndex} = \{cycle.states.size() - cycle.startIndex}");
      System.out.println(STR."Last in cycle is \{cycle.states.getLast()}");
      int stopIndex = IntStream.range(0, Integer.MAX_VALUE).filter(i -> cycle.states.get(i).node.endsWith("Z")).findFirst().getAsInt();
      System.out.println(STR."Stop index is \{stopIndex}");
      lcm = lcm(lcm, cycle.states.size());
      System.out.println(STR."LCM now \{lcm}");
    }
    System.out.println(STR."Solution maybe \{lcm}");
    // Looks like all of the cycle lengths have pairwise GCD 269.
  }

  private static long lcm(long a, long b) {
    long gcd = LongMath.gcd(a, b);
    if (gcd != 1) {
      System.out.println(STR."GCD of \{a} and \{b} is \{gcd}");
    }
    return a / gcd * b;
  }

  record Cycle(int startIndex, List<State> states) {}

  private Cycle cycle(State state) {
    SequencedSet<State> states = new LinkedHashSet<>();
    while (true) {
      state = nextState(state);
      if (!states.add(state)) {
        List<State> stateList = new ArrayList<>(states);
        int index = stateList.indexOf(state);
        assert index >= 0;
        return new Cycle(index, stateList);
      }
    }
  }

  record State(String node, int dirIndex) {}

  private List<State> nextStates(List<State> states) {
    return states.stream().map(state -> nextState(state)).toList();
  }

  private State nextState(State state) {
    List<String> fork = map.get(state.node);
    int index = indexFor(directions.charAt(state.dirIndex));
    int newDirIndex = (state.dirIndex + 1) % directions.length();
    return new State(fork.get(index), newDirIndex);
  }

  private static int indexFor(char c) {
    return switch (c) {
      case 'L' -> 0;
      case 'R' -> 1;
      default -> throw new AssertionError(c);
    };
  }

  private static final Pattern MAP_LINE = Pattern.compile("(.*) = \\((.*), (.*)\\)");

  private static Map<String, List<String>> parseMap(List<String> lines) {
    return lines.stream()
        .map(MAP_LINE::matcher)
        .peek(matcher -> {
          if (!matcher.matches()) {
            throw new IllegalArgumentException(matcher.toString());
          }
        })
        .map(matcher -> Map.entry(matcher.group(1), List.of(matcher.group(2), matcher.group(3))))
        .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}