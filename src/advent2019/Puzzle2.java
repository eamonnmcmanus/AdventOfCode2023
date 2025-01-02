package advent2019;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle2 {
  public static void main(String[] args) throws Exception {
    try (Reader r = new InputStreamReader(Puzzle2.class.getResourceAsStream("puzzle2.txt"))) {
      String line = CharStreams.toString(r).trim();
      ImmutableList<Integer> originalMemory =
          Splitter.on(',').splitToStream(line).map(Integer::valueOf).collect(toImmutableList());

      // Part 1
      System.out.printf("Part 1 result is %d\n", run(originalMemory, 12, 2));

      // Part 2
      outer:
      for (int i = 0; i < 100; i++) {
        for (int j = 0; j < 100; j++) {
          if (run(originalMemory, i, j) == 19690720) {
            System.out.printf("Part 2 result is %d,%d => %d\n", i, j, i * 100 + j);
            break outer;
          }
        }
      }
    }
  }

  private static int run(ImmutableList<Integer> originalMemory, int input1, int input2) {
    List<Integer> memory = new ArrayList<>(originalMemory);
    memory.set(1, input1);
    memory.set(2, input2);
    for (int pc = 0; memory.get(pc) != 99; pc += 4) {
      int lhs = memory.get(memory.get(pc + 1));
      int rhs = memory.get(memory.get(pc + 2));
      int result =
          switch (memory.get(pc)) {
            case 1 -> addExact(lhs, rhs);
            case 2 -> multiplyExact(lhs, rhs);
            default -> throw new AssertionError(memory.get(pc));
          };
      memory.set(memory.get(pc + 3), result);
    }
    return memory.get(0);
  }
}
