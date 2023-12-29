package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle5 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle5.class.getResourceAsStream("puzzle5.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      String seedsLine = lines.get(0);
      assert seedsLine.startsWith("seeds: ");
      List<String> seedStrings = Arrays.stream(seedsLine.split(" ")).skip(1).toList();
      List<Long> seedNumbers = seedStrings.stream().map(Long::parseLong).toList();
      assert lines.get(1).isEmpty();
      int index = 2;
      Map<String, NumberRangeMap> nameToMap = new LinkedHashMap<>();
      while (index < lines.size()) {
        ParseResult result = parseMap(lines, index);
        nameToMap.put(result.rangeMap.from, result.rangeMap);
        index = result.nextIndex;
      }
      long minLocation = seedNumbers.stream()
          .map(seedNumber -> lookup(seedNumber, nameToMap))
          .min(Comparator.naturalOrder())
          .get();
      System.out.println(STR."Min location is \{minLocation}");
      long max = 0;
      for (int i = 1; i < seedNumbers.size(); i += 2) {
        long v = seedNumbers.get(i);
        max = Long.max(v, max);
      }
      System.out.printf("max %,d\n", max);
      minLocation = Long.MAX_VALUE;
      for (int i = 0; i < seedNumbers.size(); i += 2) {
        long start = seedNumbers.get(i);
        long len = seedNumbers.get(i + 1);
        System.out.println(STR."Looking at \{start} with len \{len}");
        for (long j = 0; j <= len; j++) {
          // Rather than this brute-force search, we could change NumberRangeMap so that it can
          // look up an input range and return a list of output ranges. Then flatmap that through
          // the remaining maps.
          long seedNumber = start + j;
          if (false && (j & 1048575) == 0) {
            System.out.printf("  ...%,d\n", seedNumber);
          }
          minLocation = Long.min(minLocation, lookup(seedNumber, nameToMap));
        }
      }
      System.out.println(STR."Min location now is \{minLocation}");
    }
  }

  private static long lookup(long seedNumber, Map<String, NumberRangeMap> nameToMap) {
    long value = seedNumber;
    String mapName = "seed";
    while (!mapName.equals("location")) {
      NumberRangeMap rangeMap = nameToMap.get(mapName);
      value = rangeMap.get(value);
      mapName = rangeMap.to;
    }
    return value;
  }

  private static final Pattern MAP_HEADER = Pattern.compile("(.*)-to-(.*) map:");
  private static final Pattern ENTRY = Pattern.compile("([0-9]+) ([0-9]+) ([0-9]+)");

  private static ParseResult parseMap(List<String> lines, int index) {
    Matcher matcher = MAP_HEADER.matcher(lines.get(index));
    if (!matcher.matches()) {
      throw new AssertionError(lines.get(index));
    }
    String from = matcher.group(1);
    String to = matcher.group(2);
    ImmutableRangeMap.Builder<Long, NumberRange> builder = ImmutableRangeMap.builder();
    while (++index < lines.size() && !lines.get(index).isEmpty()) {
      Matcher entryMatcher = ENTRY.matcher(lines.get(index));
      if (!entryMatcher.matches()) {
        throw new AssertionError(lines.get(index));
      }
      List<Long> numbers =
          List.of(entryMatcher.group(1), entryMatcher.group(2), entryMatcher.group(3)).stream()
              .map(Long::parseLong)
              .toList();
      NumberRange range = new NumberRange(numbers.get(0), numbers.get(1), numbers.get(2));
      builder.put(Range.closedOpen(range.sourceStart, range.sourceStart + range.len), range);
    }
    if (index < lines.size()) {
      ++index;
    }
    NumberRangeMap rangeMap = new NumberRangeMap(from, to, builder.build());
    return new ParseResult(rangeMap, index);
  }

  record ParseResult(NumberRangeMap rangeMap, int nextIndex) {}

  record NumberRangeMap(String from, String to, RangeMap<Long, NumberRange> rangeMap) {
    long get(long value) {
      NumberRange range = rangeMap.get(value);
      return (range == null) ? value : range.get(value);
    }
  }

  record NumberRange(long destStart, long sourceStart, long len) {
    long get(long value) {
      if (value >= sourceStart && value <= sourceStart + len) {
        return destStart + (value - sourceStart);
      } else {
        return -1;
      }
    }
  }
}
