package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Long.min;
import static java.lang.Math.abs;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.util.Collections.nCopies;

import adventlib.Dir;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        for (boolean part2 : new boolean[] {false, true}) {
          long totalComplexity = 0;
          for (String code : lines) {
            checkArgument(code.endsWith("A"));
            long len = 0;
            char prevChar = 'A';
            for (char c : code.toCharArray()) {
              len += numericKeypadCost(prevChar, c, part2 ? 26 : 3);
              prevChar = c;
            }
            long complexity =
                multiplyExact(len, Long.parseLong(code.substring(0, code.length() - 1)));
            totalComplexity = addExact(totalComplexity, complexity);
          }
          System.out.printf(
              "For %s, Part %d complexity is %d\n", name, part2 ? 2 : 1, totalComplexity);
        }
      }
    }
  }

  // The relative simplicity of the solution here belies how hard it was for me to find it. This was
  // the last 2024 puzzle that I solved. For Part 1, I just made a graph where each node represented
  // a set of states, one for each keyboard. Then I used a shortest-path algorithm. But the size of
  // the graph increases exponentially with the number of keyboards, so this approach was not viable
  // for Part 2.
  //
  // There are two key ideas I needed to get to a better solution. The first is that we don't need
  // to consider every possible path between two keys. Switching directions incurs a penalty, since
  // on the preceding keyboard you need to move from one direction key to another. So no path should
  // switch directions more than once. Just move horizontally as far as needed, then vertically as
  // far as needed, or vice versa. The only catch then is that you have to avoid the empty spaces,
  // which may mean that only one of the two alternatives will work.
  //
  // The second key idea is that, when computing the shortest path at any keyboard stage, we can
  // assume at every step that all preceding keyboards start at A, because every keypress on the
  // keyboard we are looking at is triggered by an A on the immediately preceding keyboard, which is
  // in turn triggered by an A on each keyboard preceding that one.
  //
  // Even with these ideas, getting the recursive calculation right took me a surprising amount of
  // effort. And then of course I realized that I needed memoization too.
  //
  // Say we want to push 0 starting from A. Here's what that looks like with different numbers of
  // intervening directional keypads:
  //
  // < || A  [2]
  //
  // [A to <]      || [< to A]
  // v | < | < | A || > | > | ^ | A  [8]
  //
  // [A to v] | [v to <] | [<] | [< to A] || [A to >] | [>] | [> to ^] | [^ to A]
  // v < A    | < A      | A   | > > ^ A  || v A      | A   | < ^ A    | > A      [18]
  private static long numericKeypadCost(char fromChar, char toChar, int nDirectionals) {
    Map<CostArgs, Long> cache = new LinkedHashMap<>();
    Coord from = NUMERIC_MAP.get(fromChar);
    Coord to = NUMERIC_MAP.get(toChar);
    long best = Long.MAX_VALUE;
    var paths = allPaths(NUMERIC_MAP, from, to);
    for (var path : paths) {
      long cost = 0;
      Dir lastMove = null;
      for (Dir move : path) {
        cost +=
            directionalKeypadCost(
                new CostArgs(
                    (lastMove == null) ? 'A' : DIR_TO_CHAR.get(lastMove),
                    DIR_TO_CHAR.get(move),
                    nDirectionals),
                cache);
        lastMove = move;
      }
      cost +=
          directionalKeypadCost(
              new CostArgs(
                  (lastMove == null) ? 'A' : DIR_TO_CHAR.get(lastMove), 'A', nDirectionals),
              cache);
      best = min(best, cost);
    }
    return best;
  }

  private record CostArgs(char fromChar, char toChar, int nDirectionals) {}

  private static long directionalKeypadCost(CostArgs costArgs, Map<CostArgs, Long> cache) {
    int nDirectionals = costArgs.nDirectionals;
    if (nDirectionals == 1) {
      return 1; // human just pushes toChar
    }
    Long cached = cache.get(costArgs);
    if (cached != null) {
      return cached;
    }
    Coord from = DIRECTIONAL_MAP.get(costArgs.fromChar);
    Coord to = DIRECTIONAL_MAP.get(costArgs.toChar);
    long best = Long.MAX_VALUE;
    var paths = allPaths(DIRECTIONAL_MAP, from, to);
    for (var path : paths) {
      long cost = 0;
      Dir lastMove = null;
      for (Dir move : path) {
        cost +=
            directionalKeypadCost(
                new CostArgs(
                    (lastMove == null) ? 'A' : DIR_TO_CHAR.get(lastMove),
                    DIR_TO_CHAR.get(move),
                    nDirectionals - 1),
                cache);
        lastMove = move;
      }
      cost +=
          directionalKeypadCost(
              new CostArgs(
                  (lastMove == null) ? 'A' : DIR_TO_CHAR.get(lastMove), 'A', nDirectionals - 1),
              cache);
      best = min(best, cost);
    }
    cache.put(costArgs, best);
    return best;
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

  private record Coord(int row, int col) {}

  private static final ImmutableBiMap<Character, Coord> NUMERIC_MAP = buildMap(NUMERIC_KEYPAD);
  private static final ImmutableBiMap<Character, Coord> DIRECTIONAL_MAP =
      buildMap(DIRECTIONAL_KEYPAD);
  private static final ImmutableBiMap<Dir, Character> DIR_TO_CHAR =
      ImmutableBiMap.of(Dir.N, '^', Dir.S, 'v', Dir.E, '>', Dir.W, '<');

  private static ImmutableSet<ImmutableList<Dir>> allPaths(
      ImmutableBiMap<Character, Coord> map, Coord from, Coord to) {
    if (from.row() == to.row()) {
      Dir dir = to.col() > from.col() ? Dir.E : Dir.W;
      return ImmutableSet.of(ImmutableList.copyOf(nCopies(abs(to.col() - from.col()), dir)));
    } else if (from.col() == to.col()) {
      Dir dir = to.row() > from.row() ? Dir.S : Dir.N;
      return ImmutableSet.of(ImmutableList.copyOf(nCopies(abs(to.row() - from.row()), dir)));
    } else {
      ImmutableSet.Builder<ImmutableList<Dir>> builder = ImmutableSet.builder();
      Coord verticalFirstCorner = new Coord(to.row(), from.col());
      if (map.inverse().containsKey(verticalFirstCorner)) {
        var verticalPart = getOnlyElement(allPaths(map, from, verticalFirstCorner));
        var horizontalPart = getOnlyElement(allPaths(map, verticalFirstCorner, to));
        var path = ImmutableList.<Dir>builder().addAll(verticalPart).addAll(horizontalPart).build();
        builder.add(path);
      }
      Coord horizontalFirstCorner = new Coord(from.row(), to.col());
      if (map.inverse().containsKey(horizontalFirstCorner)) {
        var horizontalPart = getOnlyElement(allPaths(map, from, horizontalFirstCorner));
        var verticalPart = getOnlyElement(allPaths(map, horizontalFirstCorner, to));
        var path = ImmutableList.<Dir>builder().addAll(horizontalPart).addAll(verticalPart).build();
        builder.add(path);
      }
      return builder.build();
    }
  }

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
}