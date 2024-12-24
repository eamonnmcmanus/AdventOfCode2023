package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static java.util.Map.entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Éamonn McManus
 */
// PARTIAL SOLUTION for Part 2. This still involved some manual steps.
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
        ImmutableMap<String, Boolean> initialWireValues =
            parseInitialWireValues(lines.subList(0, blank));
        ImmutableList<Gate> gates = parseGates(lines.subList(blank + 1, lines.size()));
        ImmutableSetMultimap<String, Gate> wireToInputs =
            gates.stream()
                .flatMap(
                    gate ->
                        Stream.of(entry(gate.inputs.get(0), gate), entry(gate.inputs.get(1), gate)))
                .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.printf(
            "For %s, computed number is %d\n", name, compute(wireToInputs, initialWireValues));
        if (name.equals("problem")) {
          part2(wireToInputs);
        }
      }
    }
  }

  private static long compute(
      SetMultimap<String, Gate> wireToInputs, ImmutableMap<String, Boolean> initialWireValues) {
    NavigableMap<String, Boolean> wireValues = new TreeMap<>();
    Deque<Map.Entry<String, Boolean>> queue = new ArrayDeque<>(initialWireValues.entrySet());
    while (!queue.isEmpty()) {
      var value = queue.remove();
      var wire = value.getKey();
      var bit = value.getValue();
      var old = wireValues.put(wire, bit);
      if (old != null) {
        return -1;
      }
      for (Gate gate : wireToInputs.get(wire)) {
        Boolean input0 = wireValues.get(gate.inputs.get(0));
        Boolean input1 = wireValues.get(gate.inputs.get(1));
        if (input0 != null && input1 != null) {
          var outputValue = gate.op.op(input0, input1);
          queue.add(entry(gate.output, outputValue));
        }
      }
    }
    return wireValues.tailMap("z00").entrySet().stream()
        .mapToLong(
            e -> {
              int bit = Integer.parseInt(e.getKey().substring(1));
              checkState(bit >= 0 && bit < 64);
              return e.getValue() ? 1L << bit : 0;
            })
        .reduce(0L, (a, b) -> a | b);
  }

  private static String bitToWire(String prefix, int bit) {
    if (bit >= 10) {
      return prefix + bit;
    } else {
      return prefix + "0" + bit;
    }
  }

  private static final ImmutableSet<String> X_INPUTS =
      IntStream.range(0, 45).mapToObj(i -> bitToWire("x", i)).collect(toImmutableSet());
  private static final ImmutableSet<String> Y_INPUTS =
      IntStream.range(0, 45).mapToObj(i -> bitToWire("y", i)).collect(toImmutableSet());
  private static final ImmutableMap<String, Boolean> ZERO_INITIAL_WIRE_VALUES =
      Stream.concat(X_INPUTS.stream(), Y_INPUTS.stream())
          .collect(toImmutableMap(s -> s, s -> false));

  private sealed interface Node {}

  private record Intermediate(Node a, Op op, Node b) implements Node {
    @Override
    public String toString() {
      List<String> operands = List.of(a.toString(), b.toString());
      if (operands.get(0).compareTo(operands.get(1)) > 0) {
        operands = List.of(operands.get(1), operands.get(0));
      }
      return "(" + operands.get(0) + " " + op + " " + operands.get(1) + ")";
    }

    @Override
    public boolean equals(Object x) {
      return x instanceof Intermediate that
          && this.op == that.op
          && ((this.a.equals(that.a) && this.b.equals(that.b))
              || (this.a.equals(that.b) && this.b.equals(that.a)));
    }

    @Override
    public int hashCode() {
      return 1001 * op.hashCode() + 31 * (a.hashCode() + b.hashCode());
    }
  }

  private record Input(String wire) implements Node {
    Input(String prefix, int bit) {
      this(bitToWire(prefix, bit));
    }

    @Override
    public String toString() {
      return wire;
    }
  }

  // Work in progress. I figured out what the tree for each output bit should be, and then searched
  // for subtrees of that tree. The output wire of the largest subtree needs to be swapped with
  // the output wire of another tree. I worked out which one manually. I think it's probably
  // possible to complete the automation but I am stopping here for now.
  private static void part2(ImmutableSetMultimap<String, Gate> wireToInputs) {
    for (int i = 0; i < 4; i++) {
      System.out.printf("%d: %s\n", i, expected(i));
    }
    ImmutableMap<String, String> exchanges =
        ImmutableMap.of(
            "z07", "vmv",
            "vmv", "z07",
            "z20", "kfm",
            "kfm", "z20",
            "hnv", "z28",
            "z28", "hnv",
            "tqr", "hth",
            "hth", "tqr");
    wireToInputs =
        wireToInputs.entries().stream()
            .map(
                e -> {
                  Gate gate1 = e.getValue();
                  String output1 = gate1.output;
                  String output2 = exchanges.get(output1);
                  if (output2 == null) {
                    return e;
                  } else {
                    Gate gate2 = new Gate(gate1.inputs, gate1.op, output2);
                    return entry(e.getKey(), gate2);
                  }
                })
            .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
    ImmutableMap<String, Gate> outputWireToGate =
        wireToInputs.values().stream().distinct().collect(toImmutableMap(g -> g.output, g -> g));
    ImmutableMap<String, Node> outputWireToNode =
        outputWireToGate.entrySet().stream()
            .map(e -> entry(e.getKey(), makeNode(e.getValue(), outputWireToGate)))
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    // This is the manual step: I put in each output bit that had the wrong tree of gates, looked
    // at the subtrees found below, and worked out what swaps were needed. Kind of laborious.
    Node expected1 = expected(35);
    System.out.printf("expected: %s\n", expected1);
    findSubtrees(expected1, outputWireToNode);
    for (int z = 0; z < 46; z++) {
      String wire = bitToWire("z", z);
      Node expected = expected(z);
      Node actual = makeNode(outputWireToGate.get(wire), outputWireToGate);
      if (!expected.equals(actual)) {
        System.out.printf("Mismatch for %s:\n", wire);
        System.out.printf("Expected: %s\n", expected);
        System.out.printf("Actual  : %s\n", actual);
      }
    }
    System.out.println(String.join(",", new TreeSet<>(exchanges.keySet())));
  }

  private static void findSubtrees(Node expected, ImmutableMap<String, Node> outputWireToNode) {
    switch (expected) {
      case Input i -> {
        return;
      }
      case Intermediate(Node a, Op op, Node b) -> {
        for (var entry : outputWireToNode.entrySet()) {
          if (entry.getValue().equals(expected)) {
            System.out.printf("found %s = %s\n", entry.getKey(), expected);
            return;
          }
        }
        findSubtrees(a, outputWireToNode);
        findSubtrees(b, outputWireToNode);
      }
    }
  }

  private static Node expected(int bit) {
    if (bit == 0) {
      return new Intermediate(new Input("x00"), Op.XOR, new Input("y00"));
    }
    Node addBit = new Intermediate(new Input("x", bit), Op.XOR, new Input("y", bit));
    Node carry = carry(bit);
    if (bit == 45) {
      return carry;
    } else {
      return new Intermediate(carry, Op.XOR, addBit);
    }
  }

  private static Node carry(int intoBit) {
    if (intoBit == 1) {
      return new Intermediate(new Input("x00"), Op.AND, new Input("y00"));
    }
    int prevBit = intoBit - 1;
    Node prevCarry = carry(prevBit);
    Node prevWasZero = new Intermediate(new Input("x", prevBit), Op.XOR, new Input("y", prevBit));
    Node carryFromPrev = new Intermediate(prevCarry, Op.AND, prevWasZero);
    Node prevBothOnes = new Intermediate(new Input("x", prevBit), Op.AND, new Input("y", prevBit));
    return new Intermediate(carryFromPrev, Op.OR, prevBothOnes);
  }

  private static Node makeNode(Gate gate, ImmutableMap<String, Gate> wireToOutput) {
    List<Node> inputs = new ArrayList<>();
    for (String wire : gate.inputs) {
      Gate inGate = wireToOutput.get(wire);
      Node in = (inGate == null) ? new Input(wire) : makeNode(inGate, wireToOutput);
      inputs.add(in);
    }
    return new Intermediate(inputs.get(0), gate.op, inputs.get(1));
  }

  // My idea here was to check each bit separately to see whether addition of just that bit
  // works correctly. That should limit the number of gates whose outputs we need to consider
  // swapping. Unfortunately it doesn't limit it enough.
  // I also tried swapping each pair of gates and seeing whether that reduced the number of wrong
  // additions, but that isn't sound because a swap can reduce that number coincidentally without
  // being a correct swap.
  private static void oldPart2(ImmutableSetMultimap<String, Gate> wireToInputs) {
    checkState(!wireToInputs.containsKey("x45"));
    checkState(!wireToInputs.containsKey("y45"));
    Set<String> suspectWires = new TreeSet<>();
    Set<String> suspectOutputs = new TreeSet<>();
    for (int shift = 0; shift < 44; shift++) {
      for (long x = 0; x < 2; x++) {
        for (long y = 0; y < 2; y++) {
          var xyInputs =
              ImmutableMap.of(
                  bitToWire("x", shift), (x & 1) == 1,
                  bitToWire("y", shift), (y & 1) == 1);
          var initialWireValues =
              ImmutableMap.<String, Boolean>builder()
                  .putAll(ZERO_INITIAL_WIRE_VALUES)
                  .putAll(xyInputs)
                  .buildKeepingLast();
          long expected = (x << shift) + (y << shift);
          long actual = compute(wireToInputs, initialWireValues);
          if (expected != actual) {
            System.out.printf(
                "Differed for %x + %x, expected %x, actual %x\n",
                x << shift, y << shift, expected, actual);
            suspectWires.addAll(xyInputs.keySet());
            long thisBit = 1L << shift;
            if ((expected & thisBit) != (actual & thisBit)) {
              suspectOutputs.add(bitToWire("z", shift));
            }
            long nextBit = 1L << (shift + 1);
            if ((expected & nextBit) != (actual & nextBit)) {
              suspectOutputs.add(bitToWire("z", shift + 1));
            }
          }
        }
      }
    }
    System.out.printf("%d suspect wires: %s\n", suspectWires.size(), suspectWires);
    System.out.printf("%d suspect outputs: %s\n", suspectOutputs.size(), suspectOutputs);
    Set<Gate> suspectGates = new LinkedHashSet<>();
    for (String wire : suspectWires) {
      suspectGates.addAll(gatesReached(wire, wireToInputs));
    }
    System.out.printf(
        "%d suspect gates from suspect wires: %s\n", suspectGates.size(), suspectGates);
    suspectGates.clear();
    for (String wire : suspectOutputs) {
      suspectGates.addAll(gatesLeadingTo(wire, wireToInputs));
    }
    System.out.printf(
        "%d suspect gates from suspect result wires: %s\n", suspectGates.size(), suspectGates);
    for (Gate gate : new LinkedHashSet<>(wireToInputs.values())) {
      if (new LinkedHashSet<>(gate.inputs).equals(Set.of("x07", "y07"))) {
        System.out.println(gate);
      }
    }
    ImmutableSetMultimap<String, Gate> modifiedWireToInputs = wireToInputs;
    long currentIncorrect = incorrectCount(modifiedWireToInputs);
    outer:
    while (currentIncorrect > 0) {
      System.out.printf("@@@ looping with currentIncorrect=%d\n", currentIncorrect);
      ImmutableMap<String, Gate> wireToOutput =
          modifiedWireToInputs.values().stream()
              .distinct()
              .collect(toImmutableMap(g -> g.output, g -> g));
      ImmutableList<String> wires = wireToOutput.keySet().asList();
      for (int i = 0; i < wires.size(); i++) {
        String wire1 = wires.get(i);
        System.out.printf("%d of %d (%s)\n", i, wires.size(), wires.get(i));
        for (int j = i + 1; j < wires.size(); j++) {
          String wire2 = wires.get(j);
          Gate gate1 = wireToOutput.get(wire1);
          Gate gate2 = wireToOutput.get(wire2);
          Gate newGate1 = new Gate(gate1.inputs, gate1.op, gate2.output);
          Gate newGate2 = new Gate(gate2.inputs, gate2.op, gate1.output);
          ImmutableSetMultimap<String, Gate> newWireToInputs =
              modifiedWireToInputs.entries().stream()
                  .map(
                      e ->
                          entry(
                              e.getKey(),
                              e.getValue() == gate1
                                  ? newGate1
                                  : e.getValue() == gate2 ? newGate2 : e.getValue()))
                  .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
          long newIncorrect = incorrectCount(newWireToInputs);
          if (newIncorrect < currentIncorrect) {
            System.out.printf("@@@ swapped %s and %s\n", newGate1, newGate2);
            currentIncorrect = newIncorrect;
            modifiedWireToInputs = newWireToInputs;
            continue outer;
          }
        }
      }
      throw new AssertionError("Did not progress from currentIncorrect=" + currentIncorrect);
    }
  }

  private static long incorrectCount(SetMultimap<String, Gate> wireToInputs) {
    long count = 0;
    for (int shift = 0; shift < 44; shift++) {
      for (long x = 0; x < 2; x++) {
        for (long y = 0; y < 2; y++) {
          var xyInputs =
              ImmutableMap.of(
                  bitToWire("x", shift), (x & 1) == 1,
                  bitToWire("y", shift), (y & 1) == 1);
          var initialWireValues =
              ImmutableMap.<String, Boolean>builder()
                  .putAll(ZERO_INITIAL_WIRE_VALUES)
                  .putAll(xyInputs)
                  .buildKeepingLast();
          long expected = (x << shift) + (y << shift);
          long actual = compute(wireToInputs, initialWireValues);
          if (expected != actual) {
            long thisBit = 1L << shift;
            if ((expected & thisBit) != (actual & thisBit)) {
              count++;
            }
            long nextBit = 1L << (shift + 1);
            if ((expected & nextBit) != (actual & nextBit)) {
              count++;
            }
          }
        }
      }
    }
    return count;
  }

  private static ImmutableSet<Gate> gatesLeadingTo(
      String endWire, ImmutableSetMultimap<String, Gate> wireToInputs) {
    ImmutableMap<String, Gate> wireToOutput =
        wireToInputs.values().stream().distinct().collect(toImmutableMap(g -> g.output, g -> g));
    Set<Gate> gatesLeadingTo = new LinkedHashSet<>();
    gatesLeadingTo(gatesLeadingTo, endWire, wireToOutput);
    return ImmutableSet.copyOf(gatesLeadingTo);
  }

  private static void gatesLeadingTo(
      Set<Gate> gatesLeadingTo, String endWire, ImmutableMap<String, Gate> wireToOutput) {
    Gate gate = wireToOutput.get(endWire);
    if (gate != null) {
      gatesLeadingTo.add(gate);
      gatesLeadingTo(gatesLeadingTo, gate.inputs.get(0), wireToOutput);
      gatesLeadingTo(gatesLeadingTo, gate.inputs.get(1), wireToOutput);
    }
  }

  private static ImmutableSet<Gate> gatesReached(
      String startWire, ImmutableSetMultimap<String, Gate> wireToInputs) {
    Set<Gate> gatesReached = new LinkedHashSet<>();
    gatesReached(gatesReached, Set.of(startWire), wireToInputs);
    return ImmutableSet.copyOf(gatesReached);
  }

  private static void gatesReached(
      Set<Gate> gatesReached,
      Set<String> toVisit,
      ImmutableSetMultimap<String, Gate> wireToInputs) {
    if (toVisit.isEmpty()) {
      return;
    }
    Set<String> newToVisit = new TreeSet<>();
    for (String wire : toVisit) {
      for (Gate gate : wireToInputs.get(wire)) {
        if (gatesReached.add(gate)) {
          newToVisit.add(gate.output);
        }
      }
    }
    gatesReached(gatesReached, newToVisit, wireToInputs);
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

  private static ImmutableList<Gate> parseGates(List<String> lines) {
    return lines.stream().map(Gate::parse).collect(toImmutableList());
  }

  private static final Pattern WIRE_VALUE_PATTERN = Pattern.compile("(\\w+): ([01])");

  private static ImmutableMap<String, Boolean> parseInitialWireValues(List<String> lines) {
    return lines.stream()
        .map(WIRE_VALUE_PATTERN::matcher)
        .peek(Matcher::matches)
        .collect(toImmutableMap(m -> m.group(1), m -> m.group(2).equals("1")));
  }
}