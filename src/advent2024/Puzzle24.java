package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Iterables.getOnlyElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Éamonn McManus
 */
public class Puzzle24 {
  private static final String SAMPLE1 =
      """
      x00: 1
      x01: 1
      x02: 1
      y00: 0
      y01: 1
      y02: 0

      x00 AND y00 -> z00
      x01 XOR y01 -> z01
      x02 OR y02 -> z02
      """;

  private static final String SAMPLE2 =
      """
      x00: 1
      x01: 0
      x02: 1
      x03: 1
      x04: 0
      y00: 1
      y01: 1
      y02: 1
      y03: 1
      y04: 1

      ntg XOR fgs -> mjb
      y02 OR x01 -> tnw
      kwq OR kpj -> z05
      x00 OR x03 -> fst
      tgd XOR rvg -> z01
      vdt OR tnw -> bfw
      bfw AND frj -> z10
      ffh OR nrd -> bqk
      y00 AND y03 -> djm
      y03 OR y00 -> psh
      bqk OR frj -> z08
      tnw OR fst -> frj
      gnj AND tgd -> z11
      bfw XOR mjb -> z00
      x03 OR x00 -> vdt
      gnj AND wpb -> z02
      x04 AND y00 -> kjc
      djm OR pbm -> qhw
      nrd AND vdt -> hwm
      kjc AND fst -> rvg
      y04 OR y02 -> fgs
      y01 AND x02 -> pbm
      ntg OR kjc -> kwq
      psh XOR fgs -> tgd
      qhw XOR tgd -> z09
      pbm OR djm -> kpj
      x03 XOR y03 -> ffh
      x00 XOR y04 -> ntg
      bfw OR bqk -> z06
      nrd XOR fgs -> wpb
      frj XOR qhw -> z04
      bqk OR frj -> z07
      y03 OR x01 -> nrd
      hwm AND bqk -> z03
      tgd XOR rvg -> z12
      tnw OR pbm -> gnj
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample 1",
          () -> new StringReader(SAMPLE1),
          "sample 2",
          () -> new StringReader(SAMPLE2),
          "problem",
          () -> new InputStreamReader(Puzzle24.class.getResourceAsStream("puzzle24.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        int blank = lines.indexOf("");
        checkArgument(blank > 0);
        InitialWireValues wireValues = parseInitialWireValues(lines.subList(0, blank));
        Circuit circuit = parseCircuit(lines.subList(blank + 1, lines.size()));
        System.out.printf(
            "For %s, computed number is %d\n", name, circuit.compute(wireValues.x, wireValues.y));
        if (name.equals("problem")) {
          part2(circuit);
        }
      }
    }
  }

  // This was a very hard problem, as befits Day 24. Like many people, I found the correct answer
  // through a combination of coding and manual inspection. By printing a textual representation of
  // each output zNN, I could see that there is a "correct" version for each bit. For example, bit 2
  // looks like this:
  //   ((((x00 AND y00) AND (x01 XOR y01)) OR (x01 AND y01)) XOR (x02 XOR y02))
  // The general pattern for all bits other than the first and last is
  //   carryBit XOR addBit
  // In the example, carryBit is (((x00 AND y00) AND (x01 XOR y01)) OR (x01 AND y01)), and
  // addBit is (x02 OR y02). Then carryBit has a recursive structure, which is
  //   carry(bit) = (carry(bit - 1) AND (x[bit - 1] XOR y[bit - 1])) OR (x[bit - 1] AND y[bit - 1])
  // This means that there is a carry into `bit` if there was a carry into `bit - 1` and exactly one
  // of the `bit - 1` inputs was 1, or if both of the `bit - 1` inputs were 1. (That's not the only
  // way to write an adder stage, but it is correct and is used consistently.)
  //
  // That insight was enough for me to be able to figure out manually what the exchanges needed to
  // be, and I initially stopped at that point. But I was dissatisfied with the manual element, and
  // later went on to automate it.
  //
  // Since the tree leading to bit i should only depend on bits i and earlier, we can start from the
  // lowest bits and check for each one whether the shape of the tree corresponds to the shape
  // expected. If not, we can simply try every possible exchange of outputs until we do find the
  // shape we want. There are 222 gates, so that's only 24,531 echanges.
  //
  // I spent a lot of time trying other approaches before hitting on this. I tried inspecting the
  // results of additions and making exchanges to see if I could improve them. If you determine that
  // the result of 11000 + 01000 is incorrect, you could try exchanges until you get the correct
  // answer, and maybe the correct answer for every xx000 + yy000, but it's not certain that an
  // exchange that gets the correct answer there is actually the right exchange.
  // I also tried looking for the largest correct subtree of the expected tree, but that didn't
  // prove helpful because it isn't necessarily the case that that the root of that subtree needs to
  // have its output wire exchanged.
  private static void part2(Circuit circuit) {
    MutableGraph<Node> actualGraph = circuit.graph;
    List<Operation> operations =
        actualGraph.nodes().stream()
            .filter(n -> n instanceof Operation)
            .map(n -> (Operation) n)
            .toList();
    Set<String> exchangedOutputs = new TreeSet<>();
    for (int bit = 0; bit < circuit.outputs.size() && exchangedOutputs.size() < 8; bit++) {
      MutableGraph<Node> expectedGraph = GraphBuilder.directed().build();
      Operation expected = expected(bit, expectedGraph);
      Node actual = getOnlyElement(actualGraph.predecessors(circuit.outputs.get(bit)));
      if (!isomorphic(expected, expectedGraph, actual, actualGraph)) {
        boolean found = false;
        exchanges:
        for (int aIndex = 0; aIndex < operations.size(); aIndex++) {
          Operation a = operations.get(aIndex);
          Set<Node> aOuts = new LinkedHashSet<>(actualGraph.successors(a));
          for (int bIndex = aIndex + 1; bIndex < operations.size(); bIndex++) {
            Operation b = operations.get(bIndex);
            Set<Node> bOuts = new LinkedHashSet<>(actualGraph.successors(b));
            if (aOuts.contains(b) || bOuts.contains(a) || !Collections.disjoint(aOuts, bOuts)) {
              continue;
            }
            exchangeOutputs(actualGraph, a, b);
            if (!Graphs.hasCycle(actualGraph)) {
              Node newActual = getOnlyElement(actualGraph.predecessors(circuit.outputs.get(bit)));
              if (isomorphic(expected, expectedGraph, newActual, actualGraph)) {
                exchangedOutputs.add(a.originalOut);
                exchangedOutputs.add(b.originalOut);
                found = true;
                break exchanges;
              }
            }
            exchangeOutputs(actualGraph, a, b);
          }
        }
        if (!found) {
          System.out.printf("Could not fix incorrect bit %d\n", bit);
          break;
        }
      }
    }
    System.out.printf("List of exchanged outputs is %s\n", String.join(",", exchangedOutputs));
  }

  private static void exchangeOutputs(MutableGraph<Node> graph, Node a, Node b) {
    Set<Node> aOuts = new LinkedHashSet<>(graph.successors(a));
    Set<Node> bOuts = new LinkedHashSet<>(graph.successors(b));
    for (Node n : aOuts) {
      graph.removeEdge(a, n);
      graph.putEdge(b, n);
    }
    for (Node n : bOuts) {
      graph.removeEdge(b, n);
      graph.putEdge(a, n);
    }
  }

  // Add to `graph` the tree of nodes that we would expect to see as the output for the given bit.
  // For example if bit=2, this is what we would expect to see connected to z02.
  private static Operation expected(int bit, MutableGraph<Node> graph) {
    if (bit == 0) {
      return operation(new Terminal("x00"), Op.XOR, new Terminal("y00"), graph);
    }
    Operation carry = carry(bit, graph);
    if (bit == 45) {
      return carry;
    }
    Operation addBit = operation(new Terminal("x", bit), Op.XOR, new Terminal("y", bit), graph);
    return operation(carry, Op.XOR, addBit, graph);
  }

  private static Operation carry(int intoBit, MutableGraph<Node> graph) {
    if (intoBit == 1) {
      return inputOperation(0, Op.AND, graph);
    }
    int prevBit = intoBit - 1;
    Operation prevCarry = carry(prevBit, graph);
    Operation prevWasZero = inputOperation(prevBit, Op.XOR, graph);
    Operation carryFromPrev = operation(prevCarry, Op.AND, prevWasZero, graph);
    Operation prevBothOnes = inputOperation(prevBit, Op.AND, graph);
    return operation(carryFromPrev, Op.OR, prevBothOnes, graph);
  }

  private static final AtomicInteger operationId = new AtomicInteger();

  private static Operation operation(Node lhs, Op op, Node rhs, MutableGraph<Node> graph) {
    Operation operation = new Operation(op, "op" + operationId.getAndIncrement());
    graph.putEdge(lhs, operation);
    graph.putEdge(rhs, operation);
    return operation;
  }

  private static Operation inputOperation(int bit, Op op, MutableGraph<Node> graph) {
    return operation(new Terminal("x", bit), op, new Terminal("y", bit), graph);
  }

  private sealed interface Node {}

  private record Terminal(String name) implements Node {
    Terminal(String prefix, int i) {
      this(i < 10 ? (prefix + "0" + i) : (prefix + i));
    }
  }

  private record Operation(Op op, String originalOut) implements Node {}

  private record Circuit(
      ImmutableList<Terminal> xInputs,
      ImmutableList<Terminal> yInputs,
      ImmutableList<Terminal> outputs,
      MutableGraph<Node> graph) {
    Circuit {
      checkArgument(xInputs.size() == yInputs.size()); // simplifies things slightly
    }

    String toString(Node node) {
      return treeString(graph, node);
    }

    long compute(long x, long y) {
      Map<Node, Boolean> nodeValues = new LinkedHashMap<>();
      record NodeValue(Node node, boolean value) {}
      Deque<NodeValue> queue = new ArrayDeque<>();
      for (int i = 0; i < xInputs.size(); i++) {
        queue.add(new NodeValue(xInputs.get(i), (x & (1L << i)) != 0));
        queue.add(new NodeValue(yInputs.get(i), (y & (1L << i)) != 0));
      }
      while (!queue.isEmpty()) {
        var nodeValue = queue.remove();
        Node node = nodeValue.node;
        boolean value = nodeValue.value;
        nodeValues.put(node, value);
        // Propagate values through the graph. When we dequeue a node and its value, we see if the
        // successors of that node now have known values. If a successor is an output terminal then
        // it does, and also if it is an operation both of whose inputs are now known.
        for (Node output : graph.successors(node)) {
          switch (output) {
            case Terminal t -> queue.add(new NodeValue(t, value));
            case Operation(Op op, String unused) -> {
              List<Boolean> inputs =
                  graph.predecessors(output).stream().map(nodeValues::get).toList();
              checkState(inputs.size() == 2);
              if (!inputs.contains(null)) {
                boolean computedValue = op.op(inputs.get(0), inputs.get(1));
                queue.add(new NodeValue(output, computedValue));
              }
            }
          }
        }
      }
      long z = 0;
      for (int i = 0; i < outputs.size(); i++) {
        // This implicitly checks that every zNN terminal has received a value; otherwise NPE.
        if (nodeValues.get(outputs.get(i))) {
          z |= 1L << i;
        }
      }
      return z;
    }
  }

  // Determines if the tree at a is isomorphic to the tree at b. Checking whether graphs are
  // isomorphic is a hard problem in general, but when the graphs are binary trees it is easy.
  static boolean isomorphic(Node a, Graph<Node> aGraph, Node b, Graph<Node> bGraph) {
    return switch (a) {
      case Terminal(String name) -> {
        // We don't need to support comparing zNN terminal nodes, so we don't.
        checkState(aGraph.predecessors(a).isEmpty());
        yield a.equals(b);
      }
      case Operation aOp -> {
        if (!(b instanceof Operation bOp) || aOp.op != bOp.op) {
          yield false;
        }
        var aIn = new ArrayList<>(aGraph.predecessors(a));
        var bIn = new ArrayList<>(bGraph.predecessors(b));
        checkState(aIn.size() == 2 && bIn.size() == 2);
        var a0 = aIn.get(0);
        var a1 = aIn.get(1);
        var b0 = bIn.get(0);
        var b1 = bIn.get(1);
        yield (isomorphic(a0, aGraph, b0, bGraph) && isomorphic(a1, aGraph, b1, bGraph))
            || (isomorphic(a0, aGraph, b1, bGraph) && isomorphic(a1, aGraph, b0, bGraph));
      }
    };
  }

  private static String treeString(Graph<Node> graph, Node node) {
    return switch (node) {
      case Terminal(String name) -> name;
      case Operation(Op op, var unused) -> {
        List<String> operands =
            graph.predecessors(node).stream().map(n -> treeString(graph, n)).sorted().toList();
        checkState(operands.size() == 2);
        yield "(" + operands.get(0) + " " + op + " " + operands.get(1) + ")";
      }
    };
  }

  private enum Op {
    AND,
    OR,
    XOR;

    boolean op(boolean a, boolean b) {
      return switch (this) {
        case AND -> a && b;
        case OR -> a || b;
        case XOR -> a ^ b;
      };
    }

    private static Op parse(String s) {
      return switch (s) {
        case "AND" -> Op.AND;
        case "OR" -> Op.OR;
        case "XOR" -> Op.XOR;
        default -> throw new AssertionError(s);
      };
    }
  }

  private record Gate(ImmutableList<String> inputs, Op op, String output) {
    // x00 AND y00 -> z00
    private static final Pattern GATE_PATTERN =
        Pattern.compile("(\\w+) (AND|OR|XOR) (\\w+) -> (\\w+)");

    static Gate parse(String s) {
      Matcher m = GATE_PATTERN.matcher(s);
      checkArgument(m.matches());
      return new Gate(ImmutableList.of(m.group(1), m.group(3)), Op.parse(m.group(2)), m.group(4));
    }
  }

  private static Circuit parseCircuit(List<String> lines) {
    List<Gate> gates = lines.stream().map(Gate::parse).toList();
    // Assumes we have the same number of xNN and yNN inputs.
    int biggestInput =
        gates.stream()
            .flatMap(g -> g.inputs.stream())
            .filter(s -> s.startsWith("x"))
            .mapToInt(s -> Integer.parseInt(s.substring(1)))
            .max()
            .getAsInt();
    int biggestOutput =
        gates.stream()
            .map(Gate::output)
            .filter(s -> s.startsWith("z"))
            .mapToInt(s -> Integer.parseInt(s.substring(1)))
            .max()
            .getAsInt();
    ImmutableList<Terminal> xInputs =
        IntStream.rangeClosed(0, biggestInput)
            .mapToObj(i -> new Terminal("x", i))
            .collect(toImmutableList());
    ImmutableList<Terminal> yInputs =
        IntStream.rangeClosed(0, biggestInput)
            .mapToObj(i -> new Terminal("y", i))
            .collect(toImmutableList());
    ImmutableList<Terminal> zOutputs =
        IntStream.rangeClosed(0, biggestOutput)
            .mapToObj(i -> new Terminal("z", i))
            .collect(toImmutableList());

    // Maps from a wire name to the unique node that has that wire as its output.
    Map<String, Node> wireToNode = new TreeMap<>();

    MutableGraph<Node> graph = GraphBuilder.directed().build();

    // Add the input terminals to the graph.
    Stream.concat(xInputs.stream(), yInputs.stream())
        .forEach(
            t -> {
              wireToNode.put(t.name, t);
              graph.addNode(t);
            });

    // Add the gates to the graph. If a gate is connected to an output terminal (zNN), connect the
    // corresponding graph nodes.
    for (Gate gate : gates) {
      Operation operation = new Operation(gate.op, gate.output);
      wireToNode.put(gate.output, operation);
      graph.addNode(operation);
      if (gate.output.startsWith("z")) {
        int i = Integer.parseInt(gate.output.substring(1));
        graph.putEdge(operation, zOutputs.get(i));
      }
    }

    // Connect the inputs of each gate.
    for (Gate gate : gates) {
      Node output = wireToNode.get(gate.output);
      for (String input : gate.inputs) {
        graph.putEdge(wireToNode.get(input), output);
      }
    }

    return new Circuit(xInputs, yInputs, zOutputs, graph);
  }

  private static final Pattern WIRE_VALUE_PATTERN = Pattern.compile("(\\w+): ([01])");

  record InitialWireValues(long x, long y) {}

  private static InitialWireValues parseInitialWireValues(List<String> lines) {
    ImmutableSet<String> ones =
        lines.stream()
            .map(WIRE_VALUE_PATTERN::matcher)
            .peek(Matcher::matches)
            .filter(m -> m.group(2).equals("1"))
            .map(m -> m.group(1))
            .collect(toImmutableSet());
    long x = 0;
    long y = 0;
    for (var one : ones) {
      int i = Integer.parseInt(one.substring(1));
      long bit = 1L << i;
      if (one.startsWith("x")) {
        x |= bit;
      } else {
        y |= bit;
      }
    }
    return new InitialWireValues(x, y);
  }
}