package advent2024;

import static adventlib.GraphAlgorithms.shortestPath;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;

import adventlib.GraphAlgorithms;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle21 {
  private static final String SAMPLE =
      """
      029A
      980A
      179A
      456A
      379A
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle21.class.getResourceAsStream("puzzle21.txt")));

  public static void main(String[] args) throws Exception {
    var graph = buildStateGraph();
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        long totalComplexity = 0;
        for (String code : lines) {
          checkArgument(code.endsWith("A"));
          int len = 0;
          State cur = new State('A', 'A', 'A');
          for (char c : code.toCharArray()) {
            State next = new State('A', 'A', c);
            var path = shortestPath(graph, cur, next);
            len += path.size() + 1;
            cur = next;
          }
          System.out.printf("For code %s, len=%d\n", code, len);
          long complexity =
              multiplyExact(len, Long.parseLong(code.substring(0, code.length() - 1)));
          totalComplexity = addExact(totalComplexity, complexity);
        }
        System.out.printf("For %s, Part 1 complexity is %d\n", name, totalComplexity);
      }
    }
  }

  private record State(char firstDir, char secondDir, char numer) {
    @Override
    public String toString() {
      return "[" + firstDir + "][" + secondDir + "][" + numer + "]";
    }
  }

  private static ImmutableValueGraph<State, Character> buildStateGraph() {
    MutableValueGraph<State, Character> graph = ValueGraphBuilder.directed().build();
    buildStateGraph(graph, new State('A', 'A', 'A'), new LinkedHashSet<>());
    return ImmutableValueGraph.copyOf(graph);
  }

  private static final ImmutableSet<Character> directionalButtons =
      "<>^vA".chars().mapToObj(c -> (char) c).collect(toImmutableSet());

  private static void buildStateGraph(
      MutableValueGraph<State, Character> graph, State state, Set<State> seen) {
    if (!seen.add(state)) {
      return;
    }
    for (char button : directionalButtons) {
      push(state, button)
          .ifPresent(
              newState -> {
                graph.putEdgeValue(state, newState, button);
                buildStateGraph(graph, newState, seen);
              });
    }
  }

  private static Optional<State> push(State start, char button) {
    if (button != 'A') {
      // Move the position of the first directional keypad.
      Optional<Character> newFirstDir =
          DIRECTIONAL_GRAPH.successors(start.firstDir).stream()
              .filter(c -> DIRECTIONAL_GRAPH.edgeValueOrDefault(start.firstDir, c, '?') == button)
              .findFirst();
      return newFirstDir.map(c -> new State(c, start.secondDir, start.numer));
    }
    // Apply the button from the first directional keypad to the second one.
    if (start.firstDir != 'A') {
      Optional<Character> newSecondDir =
          DIRECTIONAL_GRAPH.successors(start.secondDir).stream()
              .filter(
                  c ->
                      DIRECTIONAL_GRAPH.edgeValueOrDefault(start.secondDir, c, '?')
                          == start.firstDir)
              .findFirst();
      return newSecondDir.map(c -> new State(start.firstDir, c, start.numer));
    }
    // Apply the button from the second directional keypad to move to a number
    if (start.secondDir != 'A') {
      Optional<Character> newNumer =
          NUMERIC_GRAPH.successors(start.numer).stream()
              .filter(c -> NUMERIC_GRAPH.edgeValueOrDefault(start.numer, c, '?') == start.secondDir)
              .findFirst();
      return newNumer.map(c -> new State(start.firstDir, start.secondDir, c));
    }
    // We are pressing all the A buttons. This is the point of all the activity, but it doesn't
    // change the state.
    return Optional.empty();
  }

  private static final String NUMERIC_KEYPAD =
      """
      789
      456
      123
       0A
      """;

  private static final String DIRECTIONAL_KEYPAD =
      """
       ^A
      <v>
      """;

  private static final ImmutableValueGraph<Character, Character> NUMERIC_GRAPH =
      buildKeypad(NUMERIC_KEYPAD);

  private static final ImmutableValueGraph<Character, Character> DIRECTIONAL_GRAPH =
      buildKeypad(DIRECTIONAL_KEYPAD);

  private record Coord(int row, int col) {}

  private static final ImmutableBiMap<Character, Coord> NUMERIC_MAP = buildMap(NUMERIC_KEYPAD);
  private static final ImmutableBiMap<Character, Coord> DIRECTIONAL_MAP =
      buildMap(DIRECTIONAL_KEYPAD);

  private static ImmutableBiMap<Character, Coord> buildMap(String map) {
    List<String> mapLines = Splitter.on('\n').omitEmptyStrings().splitToList(map);
    int width = mapLines.get(0).length();
    checkArgument(mapLines.stream().allMatch(line -> line.length() == width));
    return IntStream.range(0, mapLines.size())
        .boxed()
        .flatMap(
            row ->
                IntStream.range(0, width)
                    .mapToObj(col -> Map.entry(mapLines.get(row).charAt(col), new Coord(row, col)))
                    .filter(e -> e.getKey() != ' '))
        .collect(toImmutableBiMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static ImmutableValueGraph<Character, Character> buildKeypad(String map) {
    List<String> mapLines = Splitter.on('\n').omitEmptyStrings().splitToList(map);
    int width = mapLines.get(0).length();
    checkArgument(mapLines.stream().allMatch(line -> line.length() == width));
    ImmutableValueGraph.Builder<Character, Character> builder =
        ValueGraphBuilder.directed().<Character, Character>immutable();
    for (int i = 0; i < mapLines.size(); i++) {
      String line = mapLines.get(i);
      for (int col = 0; col < width; col++) {
        char c = line.charAt(col);
        if (col > 0) {
          add(builder, c, line.charAt(col - 1), '<');
        }
        if (col + 1 < width) {
          add(builder, c, line.charAt(col + 1), '>');
        }
        if (i > 0) {
          add(builder, c, mapLines.get(i - 1).charAt(col), '^');
        }
        if (i + 1 < mapLines.size()) {
          add(builder, c, mapLines.get(i + 1).charAt(col), 'v');
        }
      }
    }
    return builder.build();
  }

  private static void add(
      ImmutableValueGraph.Builder<Character, Character> builder, char from, char to, char label) {
    if (from == ' ' || to == ' ') {
      return;
    }
    builder.putEdgeValue(from, to, label);
  }
}