package advent2024;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Math.toIntExact;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle17 {
  private static final ImmutableList<String> SAMPLES =
      ImmutableList.of(
          // Tests
          """
          Register C: 9

          Program: 2,6
          """,
          """
          Register A: 10

          Program: 5,0,5,1,5,4
          """,
          """
          Register A: 2024

          Program: 0,1,5,4,3,0
          """,
          """
          Register B: 29

          Program: 1,7
          """,
          """
          Register B: 2024
          Register C: 43690

          Program: 4,0
          """,
          // Part 1 Sample
          """
          Register A: 729
          Register B: 0
          Register C: 0

          Program: 0,1,5,4,3,0
          """,
          // Part 2 Sample
          """
          Register A: 117440
          Register B: 0
          Register C: 0

          Program: 0,3,5,4,3,0
          """);

  private static final ImmutableMap<String, Callable<Reader>> SAMPLE_INPUT_PRODUCERS =
      IntStream.range(0, SAMPLES.size())
          .boxed()
          .collect(toImmutableMap(i -> "Sample " + i, i -> () -> new StringReader(SAMPLES.get(i))));

  private static final ImmutableMap<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.<String, Callable<Reader>>builder()
          .putAll(SAMPLE_INPUT_PRODUCERS)
          .put(
              "problem",
              () -> new InputStreamReader(Puzzle17.class.getResourceAsStream("puzzle17.txt")))
          .build();

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        checkArgument(lines.size() >= 3);
        int blank = lines.indexOf("");
        checkArgument(blank == lines.size() - 2);
        Pattern registerPattern = Pattern.compile("Register ([ABC]): (\\d+)");
        long[] reg = new long[3];
        for (int i = 0; i < blank; i++) {
          var matcher = registerPattern.matcher(lines.get(i));
          checkState(matcher.matches());
          int regI = matcher.group(1).charAt(0) - 'A';
          checkState(regI >= 0 && regI < 3);
          reg[regI] = Integer.parseInt(matcher.group(2));
        }
        String programString = Splitter.on(' ').splitToList(lines.get(blank + 1)).get(1);
        List<Integer> program =
            Splitter.on(',').splitToStream(programString).map(Integer::valueOf).toList();

        // Part 1
        List<Integer> output = run(program, reg.clone());
        System.out.printf(
            "For %s, program output is %s\n",
            name, output.isEmpty() ? "empty" : Joiner.on(',').join(output));

        // Part 2
        if (name.equals("problem")) {
          if (false) {
            disassemble(program).forEach(System.out::println);
          }
          int len = program.size();
          // The last instruction should be jnz 0, encoded as [3, 0].
          checkState(program.get(len - 2) == 3 && program.get(len - 1) == 0);
          List<Integer> subprogram = program.subList(0, len - 2);
          OptionalLong quineA = search(0, program, subprogram);
          if (quineA.isPresent()) {
            System.out.printf("Quine A is %d\n", quineA.getAsLong());
          }
        }
      }
    }
  }

  /*

  I found Part 2 much more difficult than previous days.

  This was my problem input:
  2,4,1,5,7,5,0,3,4,1,1,6,5,5,3,0

  Disassembled to this (with explanatory comments):
  bst A: b = a & 7
  bxl 5: b ^= 5
  cdv B: c = a >> b
  adv 3: a >>= 3
  bxc 1: b ^= c
  bxl 6: b ^= 6
  out B: b & 7
  jnz 0: back to top unless a is 0

  So each loop produces an output value, then shifts register A 3 bits right and loops,
  until A is 0. The value to output depends on the bottom 3 bits of A, which form a shift amount
  from which the 3 output bits will be selected. (I presume everybody's problem looked like this,
  with the xor values, the unused operand to the bxc instruction, and probably instruction order
  differing.)

  We can work backwards from the end of the list. At each point, we have a target A value that we
  want to end up with. At the end of the list it must be 0 so that the loop will exit. Before then,
  it must be some value that will produce the remaining output digits. The two key insights, which
  I needed forum help to get, were (1) at each step we are only looking at the possible values for
  the bottom 3 bits, since the target A value is fixed; and (2) several such possible values will
  produce the correct output digit. Not every 3-bit combination will work as we proceed backwards
  through the desired output, so we must be prepared to backtrack.

  */

  private static OptionalLong search(
      long targetA, List<Integer> targetOutput, List<Integer> subprogram) {
    if (targetOutput.isEmpty()) {
      return OptionalLong.of(targetA);
    }
    int target = targetOutput.getLast();
    List<Integer> shorterOutput = targetOutput.subList(0, targetOutput.size() - 1);
    for (int bottom = 0; bottom < 8; bottom++) {
      long a = (targetA << 3) | bottom;
      List<Integer> out = run(subprogram, new long[] {a, 0, 0});
      checkState(out.size() == 1);
      if (out.get(0) == target) {
        OptionalLong newA = search(a, shorterOutput, subprogram);
        if (newA.isPresent()) {
          return newA;
        }
      }
    }
    return OptionalLong.empty();
  }

  private static final int A = 0;
  private static final int B = 1;
  private static final int C = 2;

  /** Runs the given program with the given register array. */
  private static List<Integer> run(List<Integer> program, long[] reg) {
    checkArgument(reg.length == 3);
    List<Integer> out = new ArrayList<>();
    for (int pc = 0; pc < program.size(); pc += 2) {
      int opcode = program.get(pc);
      int opd = program.get(pc + 1);
      switch (opcode) {
        case 0, 6, 7 -> {
          long opd2 = combo(opd, reg);
          int r =
              switch (opcode) {
                case 0 -> A;
                case 6 -> B;
                case 7 -> C;
                default -> throw new AssertionError(opcode);
              };
          reg[r] = (opd2 >= 64) ? 0 : (reg[A] >> (int) opd2);
        }
        case 1 -> reg[B] ^= opd;
        case 2 -> reg[B] = combo(opd, reg) & 7;
        case 3 -> {
          if (reg[A] != 0) {
            pc = opd - 2;
          }
        }
        case 4 -> reg[B] ^= reg[C];
        case 5 -> out.add(toIntExact(combo(opd, reg) & 7));
      }
    }
    return out;
  }

  private static long combo(int opd, long[] reg) {
    return switch (opd) {
      case 0, 1, 2, 3 -> opd;
      case 4, 5, 6 -> reg[opd - 4];
      default -> throw new AssertionError(opd);
    };
  }

  private static final ImmutableList<String> OPCODES =
      ImmutableList.of("adv", "bxl", "bst", "jnz", "bxc", "out", "bdv", "cdv");
  private static final ImmutableSet<String> COMBO_OPCODES =
      ImmutableSet.of("adv", "bdv", "cdv", "bst", "out");

  private static List<String> disassemble(List<Integer> program) {
    List<String> out = new ArrayList<>();
    for (int i = 0; i < program.size(); i += 2) {
      int op = program.get(i);
      String opcode = OPCODES.get(op);
      int opd = program.get(i + 1);
      String opdString =
          COMBO_OPCODES.contains(opcode) ? disassembleCombo(opd) : String.valueOf(opd);
      out.add(op + " " + opd + ": " + opcode + " " + opdString);
    }
    return out;
  }

  private static String disassembleCombo(int opd) {
    return switch (opd) {
      case 0, 1, 2, 3 -> String.valueOf(opd);
      case 4, 5, 6 -> String.valueOf((char) ('A' + opd - 4));
      default -> throw new AssertionError(opd);
    };
  }
}