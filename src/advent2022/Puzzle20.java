package advent2022;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Éamonn McManus
 */
public class Puzzle20 {
  private static final String SAMPLE =
      """
      1
      2
      -3
      3
      -2
      0
      4
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem",
              () -> new InputStreamReader(Puzzle20.class.getResourceAsStream("puzzle20.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        part1(name, lines);
        part2(name, lines);
      }
    }
  }

  private static void part1(String name, List<String> lines) {
    solve(name + " part 1", lines, 1, 1);
  }

  private static void part2(String name, List<String> lines) {
    solve(name + " part 2", lines, 811589153, 10);
  }

  // The logic for moves is a little tricky. First, we can reduce the move amount so its absolute
  // value is less than the list size N. For once, Java's % operator does what we want here: a shift
  // of -8 with N=7 is the same as a shift of -1, and indeed -8 % 7 = -1.
  // There's a gotcha that I wasted a lot of time on, though, which is that the modulus is one less
  // than the list size. If you have a list of 7 elements then moving an element right 6 times is
  // the same as not moving it at all:
  // a b c d e f g  // 0
  // b a c d e f g  // 1
  // b c a d e f g  // 2
  // b c d a e f g  // 3
  // b c d e a f g  // 4
  // b c d e f a g  // 5
  // b c d e f g a  // 6  (since the list is circular, this is the same as the original list).
  // This didn't show up with the sample data but obviously did with the problem data.
  //
  // Now let's consider a move as deleting from one position and inserting after another.
  // Given this list:
  // a b c d e f g
  // If we want to move d right by 1 then it goes after the originally-next element e:
  // a b c   edf g
  // This is also what happens if it moves left by 5 (N - 1 - shift).
  // But if want to move it left by 1 then it goes after the doubly-previous element b:
  // a bdc   e f g
  // This is also what happens if it moves right by 5 (N - 1 - shift).
  private static void solve(String what, List<String> lines, int multiplier, int iterations) {
    List<Node> nodes = new ArrayList<>();
    Node prev = null;
    Node zero = null;
    for (String line : lines) {
      long value = Long.parseLong(line) * multiplier;
      Node node = new Node(value);
      if (value == 0) {
        zero = node;
      }
      if (prev != null) {
        node.prev = prev;
        prev.next = node;
      }
      nodes.add(node);
      prev = node;
    }
    assert zero != null;
    nodes.getFirst().prev = nodes.getLast();
    nodes.getLast().next = nodes.getFirst();

    // Mix
    int n = nodes.size();
    int halfN = n >> 1;
    for (int iter = 0; iter < iterations; iter++) {
      for (Node node : nodes) {
        int shift = Math.toIntExact(node.value % (nodes.size() - 1));
        if (shift == 0) {
          continue;
        }
        Node insertAfter = node;
        if (shift > halfN) { // if N = 7, +5 is equivalent to -1
          shift = -n + 1 + shift;
        } else if (shift < -halfN) { // if N = 7, -5 is equivalent to +1
          shift = n - 1 + shift;
        }
        // This optimization of going in the shorter direction isn't really needed. At best it
        // makes things twice as fast, but it turns out that the whole thing executes in less than
        // a second anyway.
        if (shift > 0) {
          // Go right to insertion point. If shift is 1, we want insert after node.next, so the loop
          // should execute once.
          for (int i = 0; i < shift; i++) {
            insertAfter = insertAfter.next;
          }
        } else {
          // Go left to the insertion point. If shift is -1, we want to go left two places to insert
          // after the original node.prev.prev.
          for (int i = shift; i <= 0; i++) {
            insertAfter = insertAfter.prev;
          }
        }
        // Unlink the node from its old location.
        node.prev.next = node.next;
        node.next.prev = node.prev;
        // Insert the node after `insertAfter`.
        node.next = insertAfter.next;
        node.prev = insertAfter;
        insertAfter.next.prev = node;
        insertAfter.next = node;
      }
    }

    // Record new order.
    List<Long> order = new ArrayList<>();
    Node node = zero;
    do {
      order.add(node.value);
      node = node.next;
    } while (node != zero);

    long n1000 = order.get(1000 % order.size());
    long n2000 = order.get(2000 % order.size());
    long n3000 = order.get(3000 % order.size());
    System.out.println(
        "For "
            + what
            + ", N="
            + order.size()
            + ", numbers are "
            + n1000
            + ","
            + n2000
            + ","
            + n3000
            + " summing to "
            + n1000
            + n2000
            + n3000);
  }

  private static class Node {
    final long value;
    Node prev;
    Node next;

    Node(long value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return Long.toString(value);
    }
  }
}
