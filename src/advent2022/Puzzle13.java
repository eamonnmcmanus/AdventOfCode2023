package advent2022;

import static java.lang.StringTemplate.STR;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Éamonn McManus
 */
public class Puzzle13 {
  private static final String SAMPLE = """
      [1,1,3,1,1]
      [1,1,5,1,1]

      [[1],[2,3,4]]
      [[1],4]

      [9]
      [[8,7,6]]

      [[4,4],4,4]
      [[4,4],4,4,4]

      [7,7,7,7]
      [7,7,7]

      []
      [3]

      [[[]]]
      [[]]

      [1,[2,[3,[4,[5,6,7]]]],8,9]
      [1,[2,[3,[4,[5,6,0]]]],8,9]
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle13.class.getResourceAsStream("puzzle13.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<LList> lists = lines.stream()
            .filter(s -> !s.isEmpty())
            .map(line -> new Parser(line).parse())
            .toList();
        List<Pair> pairs = IntStream.range(0, lists.size() / 2)
            .map(i -> i * 2)
            .mapToObj(i -> new Pair(lists.get(i), lists.get(i + 1)))
            .toList();
        int sum = IntStream.range(0, pairs.size())
            .filter(i -> pairs.get(i).inOrder())
            .map(i -> i + 1)
            .reduce(0, Math::addExact);
        System.out.println(STR."Sum for \{name} is \{sum}");

        LList div1 = LList.of(LList.of(2));
        LList div2 = LList.of(LList.of(6));
        List<LList> sorted = Stream.concat(List.of(div1, div2).stream(), lists.stream())
            .sorted()
            .toList();
        int index1 = sorted.indexOf(div1);
        int index2 = sorted.indexOf(div2);
        assert index1 >= 0 && index2 >= 0;
        long key = (long) (index1 + 1) * (index2 + 1);
        System.out.println(STR."Decoder key for \{name} is \{key}");
      }
    }
  }

  record Pair(LList lhs, LList rhs) {
    boolean inOrder() {
      return lhs.compareTo(rhs) <= 0;
    }
  }

  private sealed interface ListOrInt extends Comparable<ListOrInt> permits Int, LList {}

  private record Int(int i) implements ListOrInt {
    @Override public String toString() {
      return Integer.toString(i);
    }

    @Override
    public int compareTo(ListOrInt that) {
      return switch (that) {
        case Int(int thatI) -> Integer.compare(this.i, thatI);
        case LList _ -> LList.of(i).compareTo(that);
      };
    }
  }

  private record LList(List<ListOrInt> list) implements ListOrInt {
    static LList of(int i) {
      return new LList(List.of(new Int(i)));
    }

    static LList of(ListOrInt... values) {
      return new LList(List.of(values));
    }

    @Override public String toString() {
      return list.stream().map(Object::toString).collect(joining(",", "[", "]"));
    }

    @Override
    public int compareTo(ListOrInt that) {
      return switch (that) {
        case LList(List<ListOrInt> thatList) ->
          Comparators.lexicographical(
              Comparator.<ListOrInt>naturalOrder()).compare(this.list, thatList);
        case Int(int thatI) -> compareTo(LList.of(thatI));
      };
    }
  }

  private static class Parser {
    private final String line;
    private int index;
    private static final char EOL = (char) 26;

    Parser(String line) {
      this.line = line + EOL;
    }

    LList parse() {
      LList result = parseList();
      if (line.charAt(index) != EOL) {
        throw parseError("Extra junk at end of line");
      }
      return result;
    }

    private LList parseList() {
      if (line.charAt(index++) != '[') {
        throw parseError("Expected [");
      }
      if (line.charAt(index) == ']') {
        index++;
        return new LList(List.of());
      }
      List<ListOrInt> list = new ArrayList<>();
      while (true) {
        switch ((Character) line.charAt(index)) {
          case '[' -> list.add(parseList());
          case Character c when isDigit(c) -> list.add(parseInt());
          default -> throw parseError("Expected list element");
        }
        switch (line.charAt(index++)) {
          case ']' -> {
            return new LList(list);
          }
          case ',' -> {}
          default -> throw parseError("Expected , or ]");
        }
      }
    }

    private Int parseInt() {
      assert isDigit(line.charAt(index));
      int value = 0;
      do {
        value = value * 10 + line.charAt(index++) - '0';
      } while (isDigit(line.charAt(index)));
      return new Int(value);
    }

    private static boolean isDigit(char c) {
      return '0' <= c && c <= '9';
    }

    private IllegalArgumentException parseError(String message) {
      return new IllegalArgumentException(STR."\{message}, at index \{index} of \{line}");
    }
  }
}
