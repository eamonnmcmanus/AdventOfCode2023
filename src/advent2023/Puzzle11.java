package advent2023;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle11 {
  /*
   * For Part 1, I naively inserted horizontal and vertical gaps into the input. That's extravagant
   * when the extra gap is 999,999 units, so I recoded for Part 2.
   *
   * The killer here was that it took me a really long time to realize that the sum for Part 2
   * doesn't fit in an int. So I was getting the wrong answer because of silent int overflow.
   */
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle11.class.getResourceAsStream("puzzle11.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = new ArrayList<>(List.of(lineString.split("\n")));
      new Puzzle11(2).solve(lines);
      new Puzzle11(1_000_000).solve(lines);
    }
  }

  private final int gapMultiplier;

  Puzzle11(int gapMultiplier) {
    this.gapMultiplier = gapMultiplier;
  }

  private void solve(List<String> lines) {
    List<Coord> galaxies = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      for (int j = 0; j < line.length(); j++) {
        if (line.charAt(j) == '#') {
          galaxies.add(new Coord(i, j));
        }
      }
    }

    Set<Integer> iGaps = IntStream.range(0, lines.size())
        .filter(i -> !lines.get(i).contains("#"))
        .boxed().collect(toImmutableSet());
    Set<Integer> jGaps = IntStream.range(0, lines.get(0).length())
        .filter(j -> lines.stream().noneMatch(line -> line.charAt(j) == '#'))
        .boxed().collect(toImmutableSet());
    System.out.println(STR."i gaps \{iGaps}");
    System.out.println(STR."j gaps \{jGaps}");

    long total = 0;
    for (int i = 0; i < galaxies.size(); i++) {
      for (int j = i + 1; j < galaxies.size(); j++) {
        total += distance(galaxies.get(i), galaxies.get(j), iGaps, jGaps);
      }
    }
    System.out.println(STR."With gap multiplier \{gapMultiplier}, total distance \{total} between \{galaxies.size()} galaxies");
    // Not: 396735318
    // Coded a second way below before realizing that I was being foiled by silent int overflow.

    Set<Coord> galaxySet = new LinkedHashSet<>(galaxies);
    List<Coord> adjustedGalaxies = new ArrayList<>();
    for (int i = 0, adjustedI = 0; i < lines.size(); i++) {
      for (int j = 0, adjustedJ = 0; j < lines.get(i).length(); j++) {
        if (galaxySet.contains(new Coord(i, j))) {
          adjustedGalaxies.add(new Coord(adjustedI, adjustedJ));
        }
        if (jGaps.contains(j)) {
          adjustedJ += gapMultiplier;
        } else {
          adjustedJ++;
        }
      }
      if (iGaps.contains(i)) {
        adjustedI += gapMultiplier;
      } else {
        adjustedI++;
      }
    }
    long newTotal = 0;
    for (int i = 0; i < adjustedGalaxies.size(); i++) {
      for (int j = i + 1; j < adjustedGalaxies.size(); j++) {
        newTotal += adjustedGalaxies.get(i).dist(adjustedGalaxies.get(j));
      }
    }
    System.out.println(STR."Alternative total distance \{newTotal} between \{adjustedGalaxies.size()} galaxies");
  }

  private int distance(Coord a, Coord b, Set<Integer> iGaps, Set<Integer> jGaps) {
    // This is kind of stupid. Using bitsets would be much faster.
    int iStart = min(a.i, b.i);
    int iEnd = max(a.i, b.i);
    int iDist = 0;
    for (int i = iStart; i < iEnd; i++) {
      if (iGaps.contains(i)) {
        iDist += gapMultiplier;
      } else {
        iDist++;
      }
    }
    int jStart = min(a.j, b.j);
    int jEnd = max(a.j, b.j);
    int jDist = 0;
    for (int j = jStart; j < jEnd; j++) {
      if (jGaps.contains(j)) {
        jDist += gapMultiplier;
      } else {
        jDist++;
      }
    }
    return iDist + jDist;
  }

  record Coord(int i, int j) {
    int dist(Coord that) {
      return abs(this.i - that.i) + abs(this.j - that.j);
    }
  }
}
