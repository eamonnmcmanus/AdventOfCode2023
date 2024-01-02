package advent2023;

import static advent2023.Puzzle19.parseRule;
import static advent2023.Puzzle19.parseWorkflows;
import static com.google.common.truth.Truth.assertThat;

import advent2023.Puzzle19.Constraint;
import advent2023.Puzzle19.ConstraintSet;
import advent2023.Puzzle19.Constraints;
import advent2023.Puzzle19.Rule;
import advent2023.Puzzle19.Workflow;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
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
  public void testConstraintsWith() {
    Constraints c1 = Constraints.of(3, 6, 5, 9, 1, 10, 1, 5);
    assertThat(c1.with('m', new Constraint(7, 9)))
        .isEqualTo(Constraints.of(3, 6, 7, 9, 1, 10, 1, 5));
  }

  @Test
  public void testConstraintsMinus() {
    Constraints c1 = Constraints.of(3, 6, 5, 9, 1, 10, 1, 5);
    Constraints c2 = Constraints.of(5, 8, 6, 8, 8, 10, 0, 3);
    // c1 is 3<x<6, 5<m<9, 1<a<10, 1<s<5
    // c2 is 5<x<8, 6<m<8, 8<a<10, 0<s<3
    // The individual differences are:
    //       3<x<6
    //              5<m<7 or 7<m<9
    //                     1<a<9
    //                             2<s<5
    // So we should have two elements in the set, for the two m ranges, plus one value for the others.
    ConstraintSet expected = new ConstraintSet(
        Set.of(
            Constraints.of(3, 6, 5, 7, 1, 9, 2, 5),
            Constraints.of(3, 6, 7, 9, 1, 9, 2, 5)));
    ConstraintSet minus1 = c1.minus(c2);
    assertThat(minus1).isEqualTo(expected);
  }

  @Test
  public void testConstraintsPlus() {
    Constraints c1 = Constraints.of(3, 6, 5, 9, 1, 10, 1, 5);
    Constraints c2 = Constraints.of(5, 8, 6, 8, 8, 10, 0, 3);
    // c1 is 3<x<6, 5<m<9, 1<a<10, 1<s<5
    // c2 is 5<x<8, 6<m<8, 8<a<10, 0<s<3
    // If we start with a set that contains c2 and add c1, that should be equivalent to
    // {c2} ∪ (c1 - c2).
    ConstraintSet c2set = new ConstraintSet(Set.of(c2));
    Set<Constraints> expected = ImmutableSet.<Constraints>builder().add(c2).addAll(c1.minus(c2).constraintSet()).build();
    ConstraintSet union = c2set.plus(c1);
    assertThat(union.constraintSet()).isEqualTo(expected);
  }

  @Test
  public void constraintSetMinus() {
    ConstraintSet s = ConstraintSet.of(Constraints.of(0, 4001, 0, 4001, 0, 4001, 0, 1351));
    Constraints m = Constraints.of(0, 4001, 0, 1000, 0, 4001, 0, 4001);
    assertThat(s.minusRule(m))
        .isEqualTo(ConstraintSet.of(Constraints.of(0, 4001, 999, 4001, 0, 4001, 0, 1351)));
  }

  /*
  @Test
  public void allAcceptedBy_simple() {
    Map<String, Workflow> workflows = parseWorkflows(List.of("in{s<1351:A,R}"));
    ConstraintSet set = allAcceptedBy(workflows);
    assertThat(set).isEqualTo(ConstraintSet.of(Constraints.of(0, 4001, 0, 4001, 0, 4001, 0, 1351)));
  }

  @Test
  public void allAcceptedBy_regression() {
    Map<String, Workflow> workflows = parseWorkflows(
        List.of(
            "in{s<1351:px,qqz}",
            "qqz{s>2770:qs,R}",
            "qs{s>3448:A,lnx}",
            "lnx{m>1548:A,R}",
            "px{a<2006:A,R}"));
    // The path in->px should accept (0<s<1351,0<a<2006) and the path in->qqz->qs->lnx should accept
    // (s>1350,s>2770,s>3448)=>(s>3448) and (s>1350,s>2770,s<3449,m>1548)=>(2770<s<3449,1548<m<4001).
    ConstraintSet set = allAcceptedBy(workflows);
    assertThat(set).isNull();
  }
  */
}
/*
px{a<2006:qkq,m>2090:A,rfg}
pv{a>1716:R,A}
lnx{m>1548:A,A}
rfg{s<537:gd,x>2440:R,A}
qs{s>3448:A,lnx}
qkq{x<1416:A,crn}
crn{x>2662:A,R}
in{s<1351:px,qqz}
qqz{s>2770:qs,m<1801:hdj,R}
gd{a>3333:R,R}
hdj{m>838:A,pv}

{x=787,m=2655,a=1222,s=2876}
{x=1679,m=44,a=2067,s=496}
{x=2036,m=264,a=79,s=2244}
{x=2461,m=1339,a=466,s=291}
{x=2127,m=1623,a=2188,s=1013}
*/
