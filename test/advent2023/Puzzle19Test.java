/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package advent2023;

import static com.google.common.truth.Truth.assertThat;

import advent2023.Puzzle19.Constraint;
import advent2023.Puzzle19.ConstraintSet;
import advent2023.Puzzle19.Constraints;
import java.util.Set;
import org.junit.Test;

public class Puzzle19Test {
  @Test
  public void testConstraintMatches() {
    Constraint constraint = new Constraint(2, 10);
    assertThat(constraint.matches(3)).isTrue();
    assertThat(constraint.matches(9)).isTrue();
    assertThat(constraint.matches(2)).isFalse();
    assertThat(constraint.matches(10)).isFalse();
    assertThat(constraint.matches(1000)).isFalse();
  }

  @Test
  public void testConstraintSize() {
    assertThat(new Constraint(1, 3).size()).isEqualTo(1);
    assertThat(new Constraint(0, 4001).size()).isEqualTo(4000);
    assertThat(new Constraint(1, 2).size()).isEqualTo(0);
    assertThat(new Constraint(1, 1).size()).isEqualTo(0);
    assertThat(new Constraint(1, 0).size()).isEqualTo(0);
  }

  @Test
  public void testConstraintIntersection() {
    assertThat(new Constraint(1, 20).intersection(new Constraint(1, 10))).isEqualTo(new Constraint(1, 10));
    assertThat(new Constraint(1, 20).intersection(new Constraint(10, 15))).isEqualTo(new Constraint(10, 15));
    assertThat(new Constraint(1, 20).intersection(new Constraint(10, 25))).isEqualTo(new Constraint(10, 20));
    assertThat(new Constraint(1, 20).intersection(new Constraint(19, 30)).isEmpty()).isTrue();
    assertThat(new Constraint(1, 20).intersection(new Constraint(19, 30))).isSameInstanceAs(Constraint.EMPTY);
  }

  @Test
  public void testConstraintMinus() {
    // If this says 2 < x < 10 and that says 4 < x < 8, then the before is 2 < x < 5 and the
    // after is 7 < x < 10. If this says 2 < x < 10 and that says 1 < x < 6, then the before is
    // 2 < x < 2 (which is empty) and the after is 5 < x < 10.
    assertThat(new Constraint(2, 10).minus(new Constraint(4, 8)))
        .containsExactly(new Constraint(2, 5), new Constraint(7, 10));
    assertThat(new Constraint(2, 10).minus(new Constraint(1, 6)))
        .containsExactly(new Constraint(5, 10));
  }

  @Test
  public void testConstraintsSize() {
    assertThat(
        Constraints.of(
            3, 6, // 2
            5, 9, // 3
            1, 10, // 8
            1, 5) // 3
        .size())
        .isEqualTo(2 * 3 * 8 * 3);
    assertThat(
        Constraints.of(
            3, 6, // 2
            5, 6, // 0
            1, 10, // 8
            1, 5) // 3
        .size())
        .isEqualTo(0);
  }

  @Test
  public void testConstraintsIntersection() {
    Constraints c1 = Constraints.of(3, 6, 5, 9, 1, 10, 1, 5);
    Constraints c2 = Constraints.of(1, 7, 6, 8, 8, 17, 0, 100);
    assertThat(c1.intersection(c2)).isEqualTo(Constraints.of(3, 6, 6, 8, 8, 10, 1, 5));
    Constraints c3 = Constraints.of(1, 7, 8, 10, 8, 17, 0, 100);
    assertThat(c1.intersection(c3)).isSameInstanceAs(Constraints.EMPTY);
  }

  @Test
  public void testConstraintsMinus() {
    Constraints c1 = Constraints.of(3, 6, 5, 9, 1, 10, 1, 5);
    Constraints c2 = Constraints.of(3, 6, 6, 8, 8, 10, 1, 5);
    ConstraintSet minus1 = c1.minus(c2);
    // c1 is 3<x<6, 5<m<9, 1<a<10, 1<s<5
    // c2 is 3<x<6, 6<m<8, 8<a<10, 1<s<5
    // The x and s values from c2 are a superset of the ones from c1. For the m and a values,
    // we have (5<m<7,7<m<9,1<a<9); for each of those we should have a new Constraints where the
    // other variables are unchanged.
    assertThat(minus1).isEqualTo(
        new ConstraintSet(
            Set.of(
                c1.with('m', new Constraint(5, 7)),
                c1.with('m', new Constraint(7, 9)),
                c1.with('a', new Constraint(1, 9)))));
  }
}
