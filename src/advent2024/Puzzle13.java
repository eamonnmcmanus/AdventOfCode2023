package advent2024;

import static com.google.common.math.LongMath.gcd;
import static java.lang.Math.absExact;
import static java.lang.Math.multiplyExact;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle13 {
  private static final String SAMPLE =
      """
      Button A: X+94, Y+34
      Button B: X+22, Y+67
      Prize: X=8400, Y=5400

      Button A: X+26, Y+66
      Button B: X+67, Y+21
      Prize: X=12748, Y=12176

      Button A: X+17, Y+86
      Button B: X+84, Y+37
      Prize: X=7870, Y=6450

      Button A: X+69, Y+23
      Button B: X+27, Y+71
      Prize: X=18641, Y=10279
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample",
          () -> new StringReader(SAMPLE),
          "problem",
          () -> new InputStreamReader(Puzzle13.class.getResourceAsStream("puzzle13.txt")));

  // Button A: X+94, Y+34
  // Button B: X+22, Y+67
  // Prize: X=8400, Y=5400
  private static final Pattern INPUT_PATTERN =
      Pattern.compile(
          """
          Button A: X\\+(\\d+), Y\\+(\\d+)
          Button B: X\\+(\\d+), Y\\+(\\d+)
          Prize: X=(\\d+), Y=(\\d+)\
          """,
          Pattern.MULTILINE);

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        String input = CharStreams.toString(r);
        List<Machine> machines =
            INPUT_PATTERN
                .matcher(input)
                .results()
                .map(m -> IntStream.range(1, 7).map(i -> Integer.parseInt(m.group(i))).toArray())
                .map(ns -> new Machine(ns[0], ns[1], ns[2], ns[3], ns[4], ns[5]))
                .toList();
        long total1 =
            machines.stream()
                .map(m -> solutionCost(m.aX, m.bX, m.prizeX, m.aY, m.bY, m.prizeY))
                .reduce(0L, Math::addExact);
        System.out.printf("For Part 1 %s, total %d\n", name, total1);
        long offset = 10_000_000_000_000L;
        long total2 =
            machines.stream()
                .map(
                    m -> solutionCost(m.aX, m.bX, m.prizeX + offset, m.aY, m.bY, m.prizeY + offset))
                .reduce(0L, Math::addExact);
        System.out.printf("For Part 2 %s, total %d\n", name, total2);
      }
    }
  }

  record Machine(int aX, int aY, int bX, int bY, int prizeX, int prizeY) {}

  // I wasted a huge amount of time trying to understand the intricacies of Linear Diophantine
  // Equations before realizing that that was a complete red herring. We have two simultaneous
  // equations in two unknowns, which means that there is exactly one solution in reals. (Assuming
  // the equations are independent, which they always are with the given inputs.) Then the question
  // is just whether that solution consists of nonnegative integers. It happens that the Diophantine
  // equation ax + by = c has solutions (x, y) only if c mod d = 0, where d = gcd(a, b), but we
  // don't even need to use that fact to get something that runs instantly on the given input.
  //
  // Solving a pair of linear equations in two unknowns has a well-known formula, or can be done
  // with matrices, but I can never remember all that so here I am rederiving it with algebra:
  //   a * aX + b * bX = pX
  //   a * aY + b * bY = pY
  //
  //   Express a in terms of b:
  //
  //   a * aX = pX - b * bX
  //   a = (pX - b * bX) / aX
  //
  //   Substitute for a in the second equation:
  //   (pX - b * bX) * aY / aX + b * bY = pY
  //   pX * aY / aX  -  b * bX * aY / aX  +  b * bY  =  pY
  //   b * bY  -  b * bX * aY / aX  =  pY  -  pX * aY / aX
  //   b(bY  -  bX * aY / aX)       =  pY  -  pX * aY / aX
  //   b = (pY  -  pX * aY / aX)  /  (bY  -  bX * aY / aX)
  //   a = (pX  -  b * bX)  /  aX
  private static long solutionCost(long aX, long bX, long pX, long aY, long bY, long pY) {
    // Floating-point arithmetic or BigDecimal might have worked? But I wasn't taking any chances.
    Rational aSlope = new Rational(aY, aX);
    Rational above = new Rational(pY).minus(new Rational(pX).multiply(aSlope));
    Rational below = new Rational(bY).minus(new Rational(bX).multiply(aSlope));
    Rational bRational = above.divide(below);
    if (bRational.asInteger().isEmpty()) {
      return 0;
    }
    Rational aRational =
        new Rational(pX).minus(bRational.multiply(new Rational(bX))).divide(new Rational(aX));
    if (aRational.asInteger().isEmpty()) {
      return 0;
    }
    long a = aRational.asInteger().getAsLong();
    long b = bRational.asInteger().getAsLong();
    return a * 3L + b;
  }

  private record Rational(long numer, long denom) {
    Rational {
      if (denom < 0) {
        numer = -numer;
        denom = -denom;
      }
      long gcd = gcd(absExact(numer), denom);
      numer /= gcd;
      denom /= gcd;
    }

    Rational(long integer) {
      this(integer, 1);
    }

    Rational reciprocal() {
      return new Rational(denom, numer);
    }

    OptionalLong asInteger() {
      return denom == 1 ? OptionalLong.of(numer) : OptionalLong.empty();
    }

    Rational minus(Rational that) {
      return new Rational(
          multiplyExact(this.numer, that.denom) - multiplyExact(that.numer, this.denom),
          multiplyExact(this.denom, that.denom));
    }

    Rational multiply(Rational that) {
      return new Rational(
          multiplyExact(this.numer, that.numer), multiplyExact(this.denom, that.denom));
    }

    Rational divide(Rational that) {
      return this.multiply(that.reciprocal());
    }
  }
}