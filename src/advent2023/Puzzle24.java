package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Éamonn McManus
 */
public class Puzzle24 {
  public static void main(String[] args) throws Exception {
    String input = "puzzle24.txt";
    try (InputStream in = Puzzle24.class.getResourceAsStream(input)) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      List<Hailstone> hailstones = lines.stream().map(line -> parseHailstone(line)).toList();
      boolean small = input.contains("small");
      Bounds bounds = small ? new Bounds(7, 27) : new Bounds(200000000000000L, 400000000000000L);
      part1(hailstones, bounds);
      part2(hailstones);
    }
  }

  static void part1(List<Hailstone> hailstones, Bounds bounds) {
    int count = 0;
    for (int i = 0; i < hailstones.size(); i++) {
      Hailstone h1 = hailstones.get(i);
      for (int j = i + 1; j < hailstones.size(); j++) {
        Hailstone h2 = hailstones.get(j);
        Intersection intersection = intersection(h1, h2);
        if (intersection.t1 > 0 && intersection.t2 > 0 && bounds.inside(intersection.x, intersection.y)) {
          count++;
        }
      }
    }
    System.out.println(STR."Count is \{count}");
  }

  /*
  This is basically algebra. If we interpret
  19, 13, 30 @ -2,  1, -2
  as hailstone 1, we can say x1=19, y1=13, dx1=-2, dy1=1. Then given hailstones 1 and 2, we are
  looking for times t1 and t2 such that
  x1 + t1*dx1 = x2 + t2*dx2  and
  y1 + t1*dy1 = y2 + t2*dy2.

  Rearranging the first,
  t1*dx1 = x2 - x1 + t2*dx2
  t1 = (x2 - x1 + t2*dx2) / dx1.

  Substituting into the y equation,
  y1 + [(x2 - x1 + t2*dx2) / dx1] * dy1 = y2 + t2*dy2
  y1 - y2 + (x2 - x1)*(dy1/dx1) + t2*dx2*dy1/dx1 = t2*dy2
  t2*dy2 = y1 - y2 + (x2 - x1)*(dy1/dx1) + t2*dx2*dy1/dx1
  t2 * [dy2 - dx2*dy1/dx1] = y1 - y2 + (x2 - x1)*(dy1/dx1)
  t2 = [y1 - y2 + (x2 - x1)*(dy1/dx1)] / [dy2 - dx2*dy1/dx1]

  Remarkably, this algebra appears to be right first time. :-)
  */
  static Intersection intersection(Hailstone h1, Hailstone h2) {
    double x1 = h1.startX;
    double y1 = h1.startY;
    double dx1 = h1.deltaX;
    double dy1 = h1.deltaY;
    double x2 = h2.startX;
    double y2 = h2.startY;
    double dx2 = h2.deltaX;
    double dy2 = h2.deltaY;
    double slope1 = dy1 / dx1;
    double t2 = (y1 - y2 + (x2 - x1) * slope1) / (dy2 - dx2 * slope1);
    double t1 = (x2 - x1 + t2*dx2) / dx1;
    double x = x1 + t1 * dx1;
    double y = y1 + t1 * dy1;
    return new Intersection(t1, t2, x, y);
  }

  record Intersection(double t1, double t2, double x, double y) {}

  record Bounds(long low, long high) {
    boolean inside(double x, double y) {
      return low <= x && x <= high && low <= y && y <= high;
    }
  }

  /*
  For part 2, we are looking for a starting point h0=(x0,y0,z0) and a vector d0=(dx0,dy0,dz0) such
  that the line they define intersects each of the hailstone lines. In general, two 3D lines don't
  intersect so what are the conditions so that they do?

  Consider the equations from before, supplemented by a z equation:
  x1 + t1*dx1 = x2 + t2*dx2
  y1 + t1*dy1 = y2 + t2*dy2
  z1 + t1*dz1 = z2 + t2*dz2

  So:
  t2 = [y1 - y2 + (x2 - x1)*(dy1/dx1)] / [dy2 - dx2*dy1/dx1]

  But also, if the lines intersect:
  t2 = [z1 - z2 + (x2 - x1)*(dz1/dx1)] / [dz2 - dx2*dz1/dx1]

  Therefore:
  [y1 - y2 + (x2 - x1)*(dy1/dx1)] / [dy2 - dx2*dy1/dx1] = [z1 - z2 + (x2 - x1)*(dz1/dx1)] / [dz2 - dx2*dz1/dx1]

  Writing x0 instead of x2, etc, this gives us an equation with 6 unknowns (the _0 values).
  [y1 - y0 + (x0 - x1)*(dy1/dx1)] / [dy0 - dx0*dy1/dx1] = [z1 - z0 + (x0 - x1)*(dz1/dx1)] / [dz0 - dx0*dz1/dx1]
  [y1 - y0 + (x0 - x1)*(dy1/dx1)] * [dz0 - dx0*dz1/dx1] = [z1 - z0 + (x0 - x1)*(dz1/dx1)] * [dy0 - dx0*dy1/dx1]
  We can make as many of these equations as we like by looking at the other hailstones. However, they
  are not linear equations, so solving might be tricky.

  A more tractable approach from https://github.com/dirk527/aoc2021/blob/main/src/aoc2023/Day24.jpg :
  We can initially ignore the z values, since the problem is massively overspecified.

  x0 + t1*dx0 = x1 + t1*dx1
  t1*(dx0 - dx1) = x1 - x0
  t1 = (x1 - x0) / (dx0 - dx1)

  Similarly:
  t1 = (y1 - y0) / (dy0 - dy1)

  So:
  (x1 - x0) / (dx0 - dx1) = (y1 - y0) / (dy0 - dy1)
  (x1 - x0) * (dy0 - dy1) = (y1 - y0) * (dx0 - dx1)
  x1*dy0 - x1*dy1 - x0*dy0 + x0*dy1 = y1*dx0 - y1*dx1 - y0*dx0 + y0*dx1

  Gathering the terms that are independent of the _1 variables:
  y0*dx0 - x0*dy0 = y1*dx0 - y1*dx1 + y0*dx1 - x1*dy0 + x1*dy1 - x0*dy1
  Because the LHS is independent and must be the same for every hailstone:
  y0*dx0 - x0*dy0 = y2*dx0 - y2*dx2 + y0*dx2 - x2*dy0 + x2*dy2 - x0*dy2

  y1*dx0 - y1*dx1 + y0*dx1 - x1*dy0 + x1*dy1 - x0*dy1 = y2*dx0 - y2*dx2 + y0*dx2 - x2*dy0 + x2*dy2 - x0*dy2
  Moving the unknowns (_0) to the LHS:
  (y1-y2)*dx0 + (x2-x1)*dy0 + (dy2-dy1)*x0 + (dx1-dx2)*y0 = y1*dx1 - y2*dx2 + x2*dy2 - x1*dy1

  There are 4 unknowns, and the RHS is entirely known. So we can examine 4 pairs of hailstones
  (perhaps just 5 hailstones in all) to determine the unknowns. That tells us x0,dx0,y0,dy0. We can
  plug those in to determine t1, then use that to determine cz1, the z-coordinate of the collision point for h1.
  Similarly we can determine cz2. In the time t2-t1, the rock traveled from cz1 to cz2, so its
  speed dz0 is (cz2-cz1)/(t2-t1). Then z0 = cz1 - dz0*t1.

  Apparently the numbers involved are too big for double arithmetic. The code here is essentially
  the same as https://github.com/dirk527/aoc2021/blob/main/src/aoc2023/Day24.java with the `gauss`
  method being copied verbatim.

  */

  static void part2(List<Hailstone> hailstones) {
    Hailstone one = hailstones.get(0);
    BigDecimal[][] matrix = new BigDecimal[4][5]; // 4 equations in 4 variables, each with a constant RHS
    MathContext context = MathContext.DECIMAL128;
    BigDecimal x1 = BigDecimal.valueOf(one.startX);
    BigDecimal y1 = BigDecimal.valueOf(one.startY);
    BigDecimal dx1 = BigDecimal.valueOf(one.deltaX);
    BigDecimal dy1 = BigDecimal.valueOf(one.deltaY);
    for (int i = 0; i < 4; i++) {
      Hailstone two = hailstones.get(i + 1);
      BigDecimal x2 = BigDecimal.valueOf(two.startX);
      BigDecimal y2 = BigDecimal.valueOf(two.startY);
      BigDecimal dx2 = BigDecimal.valueOf(two.deltaX);
      BigDecimal dy2 = BigDecimal.valueOf(two.deltaY);
      // (y1-y2)*dx0 + (x2-x1)*dy0 + (dy2-dy1)*x0 + (dx1-dx2)*y0 = y1*dx1 - y2*dx2 + x2*dy2 - x1*dy1
      // variables are (dx0, dy0, x0, y0) in that order
      matrix[i][0] = y1.subtract(y2);   // (y1-y2)*dx0
      matrix[i][1] = x2.subtract(x1);   // (x2-x1)*dy0
      matrix[i][2] = dy2.subtract(dy1); // (dy2-dy1)*x0
      matrix[i][3] = dx1.subtract(dx2); // (dx1-dx2)*y0
      matrix[i][4] = y1.multiply(dx1)
          .subtract(y2.multiply(dx2))
          .add(x2.multiply(dy2))
          .subtract(x1.multiply(dy1));
    }

    gauss(matrix);

    // This is copied, but I think the `gauss` algorithm could perhaps have done these extra calculations?
    BigDecimal y0 = matrix[3][4].divide(matrix[3][3], context);
    BigDecimal x0 = matrix[2][4]
        .subtract(matrix[2][3].multiply(y0))
        .divide(matrix[2][2], context);
    BigDecimal dy0 = matrix[1][4]
        .subtract(matrix[1][3].multiply(y0))
        .subtract(matrix[1][2].multiply(x0))
        .divide(matrix[1][1], context);
    BigDecimal dx0 = matrix[0][4]
        .subtract(matrix[0][3].multiply(y0))
        .subtract(matrix[0][2].multiply(x0))
        .subtract(matrix[0][1].multiply(dy0))
        .divide(matrix[0][0], context);
    System.out.println(STR."y0 \{y0} x0 \{x0} dy0 \{dy0} dx0 \{dx0}");

    BigDecimal z1 = BigDecimal.valueOf(one.startZ);
    BigDecimal dz1 = BigDecimal.valueOf(one.deltaZ);
    BigDecimal t1 = x1.subtract(x0).divide(dx0.subtract(dx1), context);
    BigDecimal cz1 = z1.add(t1.multiply(dz1));
    Hailstone two = hailstones.get(1);
    BigDecimal x2 = BigDecimal.valueOf(two.startX);
    BigDecimal z2 = BigDecimal.valueOf(two.startZ);
    BigDecimal dx2 = BigDecimal.valueOf(two.deltaX);
    BigDecimal dz2 = BigDecimal.valueOf(two.deltaZ);
    BigDecimal t2 = x2.subtract(x0).divide(dx0.subtract(dx2), context);
    BigDecimal cz2 = z2.add(t2.multiply(dz2));

    // dz0 = (cz2-cz1)/(t2-t1); z0 = cz1 - dz0*t1.
    BigDecimal dz0 = cz2.subtract(cz1).divide(t2.subtract(t1), context);
    BigDecimal z0 = cz1.subtract(dz0.multiply(t1));
    System.out.println(STR."x0 \{x0} y0 \{y0} z0 \{z0} dx0 \{dx0} dy0 \{dy0} dz0 \{dz0}");

    System.out.println(x0.add(y0).add(z0).setScale(0, RoundingMode.HALF_UP));
  }

  static void gauss(BigDecimal[][] matrix) {
    // See https://en.wikipedia.org/wiki/Gaussian_elimination
    int pivotRow = 0;
    int pivotCol = 0;
    int nRows = matrix.length;
    int nCols = matrix[0].length;
    while (pivotRow < nRows && pivotCol < nCols) {
      BigDecimal max = BigDecimal.ZERO;
      int idxMax = -1;
      for (int i = pivotRow; i < nRows; i++) {
        BigDecimal cand = matrix[i][pivotCol].abs();
        if (cand.compareTo(max) > 0) {
          max = cand;
          idxMax = i;
        }
      }
      if (matrix[idxMax][pivotCol].equals(BigDecimal.ZERO)) {
        // nothing to pivot in this column
        pivotCol++;
      } else {
        // swap rows idxMax and pivotRow
        BigDecimal[] tmp = matrix[pivotRow];
        matrix[pivotRow] = matrix[idxMax];
        matrix[idxMax] = tmp;
        for (int i = pivotRow + 1; i < nRows; i++) {
          // for all lower rows, subtract so that matrix[i][pivotCol] becomes 0
          BigDecimal factor = matrix[i][pivotCol].divide(matrix[pivotRow][pivotCol], MathContext.DECIMAL128);
          matrix[i][pivotCol] = BigDecimal.ZERO;
          for (int j = pivotCol + 1; j < nCols; j++) {
            // only need to go right, to the left it's all zeros anyway
            matrix[i][j] = matrix[i][j].subtract(factor.multiply(matrix[pivotRow][j]));
          }
        }
      }
      pivotCol++;
      pivotRow++;
    }
  }

  private static final Pattern HAILSTONE_PATTERN =
      Pattern.compile("(\\d+),\\s+(\\d+),\\s+(\\d+)\\s+@\\s+(-?\\d+),\\s+(-?\\d+),\\s+(-?\\d+)");
      // 19, 13, 30 @ -2,  1, -2

  static Hailstone parseHailstone(String line) {
    Matcher matcher = HAILSTONE_PATTERN.matcher(line);
    boolean matches = matcher.matches();
    assert matches : line;
    long[] group = groups(matcher).stream().mapToLong(Long::parseLong).toArray();
    return new Hailstone(group[0], group[1], group[2], group[3], group[4], group[5]);
  }

  static List<String> groups(Matcher matcher) {
    return IntStream.rangeClosed(1, matcher.groupCount()).mapToObj(matcher::group).toList();
  }

  record Hailstone(long startX, long startY, long startZ, long deltaX, long deltaY, long deltaZ) {}
}