package advent2021;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle8 {
  private static final String SAMPLE =
      """
      be cfbegad cbdgef fgaecd cgeb fdcge agebfd fecdb fabcd edb | fdgacbe cefdb cefbgd gcbe
      edbfga begcd cbg gc gcadebf fbgde acbgfd abcde gfcbed gfec | fcgedb cgb dgebacf gc
      fgaebd cg bdaec gdafb agbcfd gdcbef bgcad gfac gcb cdgabef | cg cg fdcagb cbg
      fbegcd cbd adcefb dageb afcb bc aefdc ecdab fgdeca fcdbega | efabcd cedba gadfec cb
      aecbfdg fbg gf bafeg dbefa fcge gcbea fcaegb dgceab fcbdga | gecf egdcabf bgf bfgea
      fgeab ca afcebg bdacfeg cfaedg gcfdb baec bfadeg bafgc acf | gebdcfa ecba ca fadegcb
      dbcfg fgd bdegcaf fgec aegbdf ecdfab fbedc dacgb gdcebf gf | cefg dcbef fcge gbcadfe
      bdfegc cbegaf gecbf dfcage bdacg ed bedf ced adcbefg gebcd | ed bcgafe cdgba cbgef
      egadfb cdbfeg cegd fecab cgb gbdefca cg fgcdab egfdb bfceg | gbdfcae bgc cg cgb
      gcafb gcf dcaebfg ecagb gf abcdeg gaef cafbge fdbac fegbdc | fgae cfgab fg bagce
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle8.class.getResourceAsStream("puzzle8.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Result> results =
            lines.stream()
                .map(line -> Splitter.on(" | ").splitToList(line))
                .peek(list -> checkArgument(list.size() == 2))
                .map(
                    list ->
                        new Result(
                            Splitter.on(' ').splitToList(list.get(0)),
                            Splitter.on(' ').splitToList(list.get(1))))
                .toList();

        // Part 1
        Set<Integer> uniqueSizes = Set.of(2, 3, 4, 7);
        long uniqueCount =
            results.stream()
                .flatMap(result -> result.outputs.stream())
                .filter(s -> uniqueSizes.contains(s.length()))
                .count();
        System.out.printf("For %s, unique count is %d\n", name, uniqueCount);

        // Part 2
        long total = 0;
        for (var result : results) {
          List<Segment> foundPermutation =
              PERMUTATIONS.stream()
                  .filter(
                      permutation ->
                          result.inputs.stream()
                              .map(Puzzle8::segments)
                              .map(segs -> permute(segs, permutation))
                              .allMatch(segs -> DIGIT_MAP.containsKey(segs)))
                  .collect(onlyElement());
          total +=
              Integer.valueOf(
                  result.outputs.stream()
                      .map(segs -> permute(segments(segs), foundPermutation))
                      .map(segs -> DIGIT_MAP.get(segs).toString())
                      .collect(joining()));
        }
        System.out.printf("For %s, total of decoded values is %d\n", name, total);
      }
    }
  }

  private enum Segment {
    A,
    B,
    C,
    D,
    E,
    F,
    G,
  }

  private static final ImmutableMap<Set<Segment>, Integer> DIGIT_MAP =
      ImmutableMap.of(
          segments("abcefg"), 0,
          segments("cf"), 1,
          segments("acdeg"), 2,
          segments("acdfg"), 3,
          segments("bcdf"), 4,
          segments("abdfg"), 5,
          segments("abdefg"), 6,
          segments("acf"), 7,
          segments("abcdefg"), 8,
          segments("abcdfg"), 9);

  private static Set<Segment> segments(String s) {
    return s.chars()
        .mapToObj(i -> Segment.valueOf(String.valueOf((char) i).toUpperCase()))
        .collect(toImmutableSet());
  }

  private static Set<Segment> permute(Set<Segment> input, List<Segment> permutation) {
    return input.stream().map(seg -> permutation.get(seg.ordinal())).collect(toImmutableSet());
  }

  // A permutation is a List<Segment> where element i says which segment the ordinal i is mapped to.
  // So element 0 says which segment A is mapped to, etc.
  private static final List<List<Segment>> PERMUTATIONS = permutations();

  private static List<List<Segment>> permutations() {
    List<List<Segment>> result = new ArrayList<>();
    permutations(EnumSet.allOf(Segment.class), List.of(), result);
    return result;
  }

  private static void permutations(
      Set<Segment> remaining, List<Segment> soFar, List<List<Segment>> result) {
    if (remaining.isEmpty()) {
      result.add(soFar);
    } else {
      for (Segment seg : remaining) {
        Set<Segment> newRemaining = EnumSet.copyOf(remaining);
        newRemaining.remove(seg);
        List<Segment> newSoFar = new ArrayList<>(soFar);
        newSoFar.add(seg);
        permutations(newRemaining, newSoFar, result);
      }
    }
  }

  private record Result(List<String> inputs, List<String> outputs) {}
}