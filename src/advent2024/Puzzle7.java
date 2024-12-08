package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle7 {
  private static final String SAMPLE =
      """
      190: 10 19
      3267: 81 40 27
      83: 17 5
      156: 15 6
      7290: 6 8 6 15
      161011: 16 10 13
      192: 17 8 14
      21037: 9 7 18 13
      292: 11 6 16 20
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle7.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        var equations = parseEquations(lines);
        for (boolean part1 : new boolean[] {true, false}) {
          Set<Op> ops =
              part1 ? EnumSet.of(Op.ADD, Op.MULTIPLY) : EnumSet.of(Op.ADD, Op.MULTIPLY, Op.CONCAT);
          long sum =
              equations.stream()
                  .filter(eq -> canBeTrue(eq, ops))
                  .mapToLong(Equation::value)
                  .reduce(0L, Math::addExact);
          System.out.println("Solution for " + name + " part " + (part1 ? 1 : 2) + ": " + sum);
        }
      }
    }
  }

  private static boolean canBeTrue(Equation eq, Set<Op> ops) {
    return canBeTrue(eq, eq.operands.get(0), 1, ops);
  }

  private static boolean canBeTrue(Equation eq, long accum, int index, Set<Op> ops) {
    if (index == eq.operands.size()) {
      return accum == eq.value;
    }
    long nextOperand = eq.operands.get(index);
    return ops.stream()
        .anyMatch(op -> canBeTrue(eq, op.operator.apply(accum, nextOperand), index + 1, ops));
  }

  private enum Op {
    ADD(Math::addExact),
    MULTIPLY(Math::multiplyExact),
    CONCAT(Puzzle7::concatExact);

    private final LongBinaryOperator operator;

    private Op(LongBinaryOperator operator) {
      this.operator = operator;
    }
  }

  @FunctionalInterface
  private interface LongBinaryOperator {
    long apply(long lhs, long rhs);
  }

  private static long concatExact(long lhs, long rhs) {
    return Long.parseLong(Long.toString(lhs) + Long.toString(rhs));
  }

  private static ImmutableList<Equation> parseEquations(List<String> lines) {
    return lines.stream()
        .map(line -> Splitter.on(": ").splitToList(line))
        .peek(lhsRhs -> checkState(lhsRhs.size() == 2))
        .map(lhsRhs -> new Equation(Long.parseLong(lhsRhs.get(0)), parseOperands(lhsRhs.get(1))))
        .collect(toImmutableList());
  }

  private static ImmutableList<Long> parseOperands(String s) {
    var result = Splitter.on(' ').splitToStream(s).map(Long::valueOf).collect(toImmutableList());
    checkArgument(!result.isEmpty());
    return result;
  }

  private record Equation(long value, ImmutableList<Long> operands) {}
}