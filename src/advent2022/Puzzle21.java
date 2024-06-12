package advent2022;

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle21 {
  private static final String SAMPLE = """
      root: pppw + sjmn
      dbpl: 5
      cczh: sllz + lgvd
      zczc: 2
      ptdq: humn - dvpt
      dvpt: 3
      lfqf: 4
      humn: 5
      ljgn: 2
      sjmn: drzm * dbpl
      sllz: 4
      pppw: cczh / lfqf
      lgvd: ljgn * ptdq
      drzm: hmdt - zczc
      hmdt: 32
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle21.class.getResourceAsStream("puzzle21.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        Map<String, Node> graph = lines.stream()
            .map(line -> parseNode(line))
            .collect(
                toMap(
                    node -> node.name(),
                    node -> node,
                    (a, b) -> {
                      throw new AssertionError("Duplicate " + a);
                    },
                    TreeMap::new));
        long result = evaluate(graph, "root");
        System.out.println("Result for " + name + " is " + result);
        foldConstants(graph, "root");
        System.out.println("Result for " + name + " after rewriting is " + result);
        Op root = (Op) graph.get("root");
        long constant;
        Op var;
        if (graph.get(root.lhs) instanceof Int(var unused, var n)) {
          constant = n;
          var = (Op) graph.get(root.rhs);
        } else if (graph.get(root.rhs) instanceof Int(var unused, var n)) {
          constant = n;
          var = (Op) graph.get(root.lhs);
        } else {
          throw new AssertionError("Unexpected root " + root);
        }
        long solution = solve(graph, var, constant);
        System.out.println("Solution is " + solution);
      }
    }
  }

  /*
   * Find the value of "humn" for which the given node evaluates to the target value.
   * We are basically undoing the various operations that happen to humn on its way to the root,
   * starting from the root. So if we have root = aaaa * (bbbb + (cccc - humn)) and we know root
   * is 10, then 10 = aaaa * (bbbb + (cccc - humn)) => 10/aaaa = bbbb + (cccc - humn) =>
   * 10/aaaa - bbbb = cccc - humn => humn = cccc - (10/aaaa - bbbb). The assumption is that our
   * graph is a tree, so all of the nodes here other than humn are either constants (aaaa) or an
   * operation between a constant and a subtree that includes humn. So do the reverse of that
   * operation and then look for the subtree, for example 10/aaaa then look for (bbbb + (cccc - humn)).
   */
  private static long solve(Map<String, Node> graph, Op node, long target) {
    // Every node we encounter will ultimately look like (var op const) or (const op var).
    // Translate into (var op const), using the reverse-minus or reverse-divide ops if needed.
    // This just reduces the number of cases to think about.
    Node lhs = graph.get(node.lhs);
    Node rhs = graph.get(node.rhs);
    record Simple(Node lhs, OpKind op, long rhs, boolean end) {}
    Simple rewritten = switch (lhs) {
      case Op unused ->
        switch (rhs) {
          case Int(var unused1, var n) -> new Simple(lhs, OpKind.of(node.op), n, false);
          default -> throw new AssertionError(rhs);
        };
      case Int(var lhsName, var lhsN) ->
        switch (rhs) {
          case Op unused -> new Simple(rhs, OpKind.ofReverse(node.op), lhsN, false);
          case Int(var rhsName, var rhsN) -> {
            if (lhsName.equals("humn")) {
              yield new Simple(lhs, OpKind.of(node.op), rhsN, true);
            } else if (rhsName.equals("humn")) {
              yield new Simple(rhs, OpKind.ofReverse(node.op), lhsN, true);
            } else {
              throw new AssertionError(node);
            }
          }
        };
    };
    long newTarget = invertOp(rewritten.op, rewritten.rhs, target);
    // The recursion here is a little clunky but it works.
    return rewritten.end ? newTarget : solve(graph, (Op) rewritten.lhs, newTarget);
  }

  // We have something like foo / 4 = 600, where op is /, rhs is 4, and target is 600, and we want
  // to return 2400.
  private static long invertOp(OpKind op, long rhs, long target) {
    return switch (op) {
      case PLUS -> target - rhs;            // foo + 4 = 600 => foo = 600 - 4
      case MINUS -> target + rhs;           // foo - 4 = 600 => foo = 600 + 4
      case REVERSE_MINUS -> rhs - target;   // 4 - foo = 600 => foo = 4 - 600
      case TIMES -> target / rhs;           // foo * 4 = 600 => foo = 600 / 4
      case DIVIDE -> Math.multiplyExact(target, rhs);
                                            // foo / 4 = 600 => foo = 600 * 4
      case REVERSE_DIVIDE -> rhs / target;  // 4 / foo = 600 => foo = 4 / 600
    };
  }

  private static long evaluate(Map<String, Node> graph, String root) {
    return switch (graph.get(root)) {
      case Int(var unused, var n) -> n;
      case Op(var unused, String lhs, char op, String rhs) -> op(evaluate(graph, lhs), op, evaluate(graph, rhs));
    };
  }

  private static long op(long lhs, char op, long rhs) {
    return switch (op) {
      case '+' -> lhs + rhs;
      case '-' -> lhs - rhs;
      case '*' -> Math.multiplyExact(lhs, rhs);
      case '/' -> lhs / rhs;
      default -> throw new AssertionError(op);
    };
  }

  enum OpKind {
    PLUS, MINUS, REVERSE_MINUS, TIMES, DIVIDE, REVERSE_DIVIDE;

    static OpKind of(char c) {
      return switch (c) {
        case '+' -> PLUS;
        case '-' -> MINUS;
        case '*' -> TIMES;
        case '/' -> DIVIDE;
        default -> throw new AssertionError(c);
      };
    }

    static OpKind ofReverse(char c) {
      return switch (c) {
        case '+' -> PLUS;
        case '-' -> REVERSE_MINUS;
        case '*' -> TIMES;
        case '/' -> REVERSE_DIVIDE;
        default -> throw new AssertionError(c);
      };
    }
  }

  private static void foldConstants(Map<String, Node> graph, String root) {
    if (graph.get(root) instanceof Op(var unused, String lhs, char op, String rhs)) {
      foldConstants(graph, lhs);
      foldConstants(graph, rhs);
      if (graph.get(lhs) instanceof Int(var unused1, var left)
          && graph.get(rhs) instanceof Int(var unused2, var right)
          && !lhs.equals("humn")
          && !rhs.equals("humn")) {
        graph.put(root, new Int(root, op(left, op, right)));
        graph.remove(lhs);
        graph.remove(rhs);
      }
    }
  }

  sealed interface Node {
    String name();
  }

  record Int(String name, long n) implements Node {
    @Override public String toString() {
      return Long.toString(n);
    }
  }

  record Op(String name, String lhs, char op, String rhs) implements Node {
    @Override public String toString() {
      return "(" + lhs + " " + op + " " + rhs + ")";
    }
  }

  private static final Pattern NUMBER = Pattern.compile("([a-z]{4}): (\\d+)");
  private static final Pattern OP = Pattern.compile("([a-z]{4}): ([a-z]{4}) ([-+*/]) ([a-z]{4})");

  private static Node parseNode(String line) {
    Matcher numberMatcher = NUMBER.matcher(line);
    if (numberMatcher.matches()) {
      return new Int(numberMatcher.group(1), Long.parseLong(numberMatcher.group(2)));
    }
    Matcher opMatcher = OP.matcher(line);
    if (opMatcher.matches()) {
      return new Op(opMatcher.group(1), opMatcher.group(2), opMatcher.group(3).charAt(0), opMatcher.group(4));
    }
    throw new IllegalArgumentException("Can't parse: line");
  }
}
