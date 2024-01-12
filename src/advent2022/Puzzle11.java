package advent2022;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toCollection;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.math.IntMath;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle11 {
  private static final String SAMPLE =
      """
      Monkey 0:
        Starting items: 79, 98
        Operation: new = old * 19
        Test: divisible by 23
          If true: throw to monkey 2
          If false: throw to monkey 3

      Monkey 1:
        Starting items: 54, 65, 75, 74
        Operation: new = old + 6
        Test: divisible by 19
          If true: throw to monkey 2
          If false: throw to monkey 0

      Monkey 2:
        Starting items: 79, 60, 97
        Operation: new = old * old
        Test: divisible by 13
          If true: throw to monkey 1
          If false: throw to monkey 3

      Monkey 3:
        Starting items: 74
        Operation: new = old + 3
        Test: divisible by 17
          If true: throw to monkey 0
          If false: throw to monkey 1
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle11.class.getResourceAsStream("puzzle11.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<String> groups = new ArrayList<>();
        while (true) {
          int blank = lines.indexOf("");
          if (blank < 0) {
            groups.add(String.join(";", lines));
            break;
          }
          groups.add(String.join(";", lines.subList(0, blank)));
          lines = lines.subList(blank + 1, lines.size());
        }
        List<Monkey> monkeys = groups.stream().map(line -> parseMonkey(line)).toList();
        long monkeyBusiness1 = solve(monkeys, false);
        System.out.println(STR."Monkey business for \{name}, Part 1 = \{monkeyBusiness1}");
        monkeys = groups.stream().map(line -> parseMonkey(line)).toList();
        long monkeyBusiness2 = solve(monkeys, true);
        System.out.println(STR."Monkey business for \{name}, Part 2 = \{monkeyBusiness2}");
      }
    }
  }

  private static long solve(List<Monkey> monkeys, boolean part2) {
    int modulus = monkeys.stream()
        .mapToInt(Monkey::divisibleBy)
        .reduce(1, (a, b) -> {
          assert IntMath.isPrime(b) && IntMath.gcd(a, b) == 1;
          return Math.multiplyExact(a, b);
        });
    Map<Monkey, Long> countMap = new HashMap<>();
    int rounds = part2 ? 10_000 : 20;
    for (int round = 1; round <= rounds; round++) {
      for (Monkey monkey : monkeys) {
        while (!monkey.items.isEmpty()) {
          long item = monkey.items.removeFirst();
          item = monkey.operation.apply(item);
          if (part2) {
            item %= modulus;
          } else {
            item /= 3;
          }
          int target = (item % monkey.divisibleBy == 0) ? monkey.trueTarget : monkey.falseTarget;
          monkeys.get(target).items.addLast(item);
          countMap.put(monkey, Math.addExact(countMap.getOrDefault(monkey, 0L), 1));
        }
      }
    }
    List<Long> counts = countMap.values().stream().sorted(Comparator.reverseOrder()).limit(2).toList();
    System.out.println(STR."Multiply \{counts}");
    return Math.multiplyExact(counts.get(0), counts.get(1));
  }

  // Monkey 0:;  Starting items: 79, 98;  Operation: new = old * 19;  Test: divisible by 23;\
  //     If true: throw to monkey 2;    If false: throw to monkey 3
  private static final Pattern MONKEY_PATTERN = Pattern.compile(
      """
      .*Starting items: ([^;]*);.*Operation: new = ([^;]*);.*Test: divisible by (\\d+);\
      .*If true: throw to monkey (\\d+);.*If false: throw to monkey (\\d+)\
      """);

  private static Monkey parseMonkey(String line) {
    Matcher matcher = MONKEY_PATTERN.matcher(line);
    checkArgument(matcher.matches(), "%s does not match /%s/", line, MONKEY_PATTERN);
    Deque<Long> items = Splitter.on(", ")
        .splitToStream(matcher.group(1))
        .map(Long::valueOf)
        .collect(toCollection(ArrayDeque::new));
    Function<Long, Long> operation = parseOperation(matcher.group(2));
    int divisibleBy = Integer.parseInt(matcher.group(3));
    int trueTarget = Integer.parseInt(matcher.group(4));
    int falseTarget = Integer.parseInt(matcher.group(5));
    return new Monkey(items, operation, divisibleBy, trueTarget, falseTarget);
  }

  // These are the only patterns that appear in the sample or the puzzle input.
  private static final Pattern PLUS = Pattern.compile("old \\+ (\\d+)");
  private static final Pattern TIMES = Pattern.compile("old \\* (\\d+)");
  private static final Pattern SQUARE = Pattern.compile("old \\* old");

  private static Function<Long, Long> parseOperation(String op) {
    Matcher matcher;
    if ((matcher = PLUS.matcher(op)).matches()) {
      long rhs = Integer.parseInt(matcher.group(1));
      return old -> old + rhs;
    } else if ((matcher = TIMES.matcher(op)).matches()) {
      long rhs = Integer.parseInt(matcher.group(1));
      return old -> Math.multiplyExact(old, rhs);
    } else if (SQUARE.matcher(op).matches()) {
      return old -> Math.multiplyExact(old, old);
    } else {
      throw new AssertionError(op);
    }
  }

  record Monkey(
      Deque<Long> items,
      Function<Long, Long> operation,
      int divisibleBy,
      int trueTarget,
      int falseTarget) {}
}