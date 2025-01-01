package advent2020;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle8 {
  private static final String SAMPLE =
      """
      nop +0
      acc +1
      jmp +4
      acc +3
      jmp -3
      acc -99
      acc +1
      jmp -4
      acc +6
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle8.class.getResourceAsStream("puzzle8.txt")));

  private static final Pattern PATTERN = Pattern.compile("(acc|jmp|nop) ([-+][0-9]+)");

  record Instr(String op, int arg) {}

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        List<Instr> instrs =
            lines.stream()
                .map(PATTERN::matcher)
                .peek(m -> checkState(m.matches()))
                .map(m -> new Instr(m.group(1), Integer.parseInt(m.group(2))))
                .toList();
        var result1 = execute(instrs);
        if (result1 instanceof Loops(int acc)) {
          System.out.printf("For %s, loop detected with acc=%d\n", name, acc);
        } else {
          throw new AssertionError("Program did not loop");
        }
        for (int i = 0; i < instrs.size(); i++) {
          Instr instr = instrs.get(i);
          Instr alt =
              switch (instr.op) {
                case "nop" -> new Instr("jmp", instr.arg);
                case "jmp" -> new Instr("nop", instr.arg);
                default -> null;
              };
          if (alt != null) {
            List<Instr> altInstrs = new ArrayList<>(instrs);
            altInstrs.set(i, alt);
            if (execute(altInstrs) instanceof Terminates(int acc1)) {
              System.out.printf("For %s, loop fixed with acc=%d\n", name, acc1);
              break;
            }
          }
        }
      }
    }
  }

  private sealed interface Result {
    int acc();
  }

  private record Loops(int acc) implements Result {}

  private record Terminates(int acc) implements Result {}

  private static Result execute(List<Instr> instrs) {
    Set<Integer> seen = new LinkedHashSet<>();
    int pc = 0;
    int acc = 0;
    while (pc < instrs.size() && seen.add(pc)) {
      Instr instr = instrs.get(pc);
      switch (instr.op) {
        case "nop" -> pc++;
        case "acc" -> {
          acc += instr.arg;
          pc++;
        }
        case "jmp" -> pc += instr.arg;
        default -> throw new AssertionError(instr.op);
      }
    }
    return pc < instrs.size() ? new Loops(acc) : new Terminates(acc);
  }
}