package advent2024;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.lang.Integer.min;
import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;

/**
 * @author Éamonn McManus
 */
public class Puzzle9 {
  private static final String SAMPLE =
      """
      2333133121414131402
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle9.txt")));

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        List<String> lines = CharStreams.readLines(r);
        String input = getOnlyElement(lines);
        long checksum1 = part1(input);
        System.out.printf("Part 1 checksum for %s is %d\n", name, checksum1);
        long checksum2 = part2(input);
        System.out.printf("Part 2 checksum for %s is %d\n", name, checksum2);
      }
    }
  }

  private static long part1(String input) {
    List<Span> spans = parseSpans(input);
    for (int i = 0; i < spans.size(); ) {
      Span span = spans.get(i);
      if (span.id != null) {
        i++;
        continue;
      }
      // Now we are going to replace the free span at i with spans containing blocks from the
      // end. We want to leave i with the right value for continuing. We're going to remove the
      // free span, so if its size is 0 then i should not change. If we insert spans, we want to
      // increment i each time, so other spans get inserted afterwards.
      int free = span.size();
      spans.remove(i);
      while (free > 0 && spans.size() > i) {
        Span last = spans.removeLast();
        if (last.id != null) {
          int moving = min(last.size, free);
          int remaining = last.size - moving;
          spans.add(i, new Span(moving, last.id));
          i++;
          free -= moving;
          if (remaining > 0) {
            spans.add(new Span(remaining, last.id));
          }
        }
      }
    }
    return checksum(spans);
  }

  private static long part2(String input) {
    // The approach here is quadratic because I didn't think it worthwhile to make the fancier data
    // structures that would be needed to be more efficient. There are 20,000 spans in the problem
    // data, so we're talking on the order of 400,000,000 operations. That's small enough to be
    // tractable, and in fact the time taken is well under a second.
    // The approach is that we scan backwards from the end, and for each file we scan fowards for
    // a free block that is big enough. If we find one, we move the file there and insert a new
    // smaller free span after the position where we just moved the file. We have to adjust the
    // outer index in this case to account for the extra span. If the moved file fills the free
    // space exactly, we will end up inserting a free span of size 0. That should have no effect
    // and is a bit simpler than treating this case separately.
    // After moving a file we replace it with a free span of the same size. That span will never be
    // used for future moves, since moves are only to the left, but it ensures that the block
    // numbers are correct for the checksum calculation.
    List<Span> spans = parseSpans(input);
    for (int i = spans.size() - 1; i > 0; i--) {
      Span span = spans.get(i);
      if (span.id != null) {
        int size = span.size;
        for (int j = 0; j < i; j++) {
          Span freeSpan = spans.get(j);
          if (freeSpan.id == null && freeSpan.size >= size) {
            spans.set(i, new Span(span.size, null));
            spans.remove(j);
            spans.add(j, span);
            spans.add(j + 1, new Span(freeSpan.size - size, null));
            i++;
            break;
          }
        }
      }
    }
    return checksum(spans);
  }

  private static List<Span> parseSpans(String input) {
    List<Span> spans = new ArrayList<>();
    for (int i = 0, nextId = 0; i < input.length(); i++) {
      int size = input.charAt(i) - '0';
      Integer id = (i % 2 == 0) ? nextId++ : null;
      spans.add(new Span(size, id));
    }
    return spans;
  }

  private static long checksum(List<Span> spans) {
    long checksum = 0;
    int blockIndex = 0;
    for (Span span : spans) {
      if (span.id == null) {
        blockIndex += span.size;
      } else {
        // Could do a little algebra to avoid looping here.
        for (int i = 0; i < span.size; i++) {
          checksum = addExact(checksum, multiplyExact(span.id, blockIndex++));
        }
      }
    }
    return checksum;
  }

  record Span(int size, @Nullable Integer id) {}
}