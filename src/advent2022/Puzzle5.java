package advent2022;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle5 {
  private static final String SAMPLE = """
          [D]
      [N] [C]
      [Z] [M] [P]
       1   2   3

      move 1 from 2 to 1
      move 3 from 1 to 3
      move 2 from 2 to 1
      move 1 from 1 to 2
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle5.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int blank = lines.indexOf("");
        assert blank > 0;
        // Reverse the piles and build them from the bottom up.
        List<String> pileStrings = lines.subList(0, blank).reversed();
        // The first line is just the pile numbers, and we assume they are just 1 to N.
        int nPiles = Splitter.onPattern("\\s+").splitToList(pileStrings.getFirst().trim()).size();
        pileStrings = pileStrings.subList(1, pileStrings.size());
        List<? extends List<Character>> piles = parsePiles(pileStrings, nPiles);

        List<Move> moves = lines.stream().skip(blank + 1).map(line -> parseMove(line)).toList();
        for (Move move : moves) {
          for (int i = 0; i < move.n; i++) {
            piles.get(move.to - 1).add(piles.get(move.from - 1).removeLast());
          }
        }
        String tops = piles.stream().map(pile -> pile.getLast()).map(Object::toString).collect(joining(""));
        System.out.println(STR."Part 1 tops for \{name}: \{tops}");

        piles = parsePiles(pileStrings, nPiles);
        for (Move move : moves) {
          List<Character> moved = new ArrayList<>();
          for (int i = 0; i < move.n; i++) {
            moved.add(piles.get(move.from - 1).removeLast());
          }
          piles.get(move.to - 1).addAll(moved.reversed());
        }
        tops = piles.stream().map(pile -> pile.getLast()).map(Object::toString).collect(joining(""));
        System.out.println(STR."Part 2 tops for \{name}: \{tops}");
      }
    }
  }

  private static List<? extends List<Character>> parsePiles(List<String> pileStrings, int nPiles) {
    List<? extends List<Character>> piles = IntStream
        .range(0, nPiles)
        .mapToObj(_ -> new ArrayList<Character>())
        .toList();
    for (String s : pileStrings) {
      for (int i = 0; i < nPiles; i++) {
        int ii = i * 4 + 1;
        if (ii < s.length() && Character.isLetter(s.charAt(ii))) {
          piles.get(i).add(s.charAt(ii));
        }
      }
    }
    return piles;
  }

  private static final Pattern MOVE_PATTERN = Pattern.compile("move (\\d+) from (\\d+) to (\\d+)");

  static Move parseMove(String line) {
    Matcher matcher = MOVE_PATTERN.matcher(line);
    checkState(matcher.matches(), line);
    return new Move(
        Integer.parseInt(matcher.group(1)),
        Integer.parseInt(matcher.group(2)),
        Integer.parseInt(matcher.group(3)));
  }

  record Move(int n, int from, int to) {}
}
