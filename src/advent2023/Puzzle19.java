package advent2023;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle19 {
  /*
   * This was a huge huge slog.
   *
   * My initial idea was to represent each accepting state as e.g.
   * {0<x<4001, 23<m<1009, 500<a<4001, 379<s<1729}. Then Part 2 reduces to finding the number
   * of parameter values matched by these constraints. Of course more than one set of constraints
   * can match a set of values so we must avoid overcounting. To do this we start with one
   * constraint and add in all the others, proceeding by removing from the new constraint each
   * range of values that is already covered by the previous ones. This is clumsy and sort of
   * exponential (in general the removing ranges of one constraint from another can result in 16
   * different sets of ranges). But it is tractable.
   *
   * My big mistake was to try to represent the states in the input DFA using the same notion, so
   * m>1009 would be {0<x<4001, 1009<m<4001, 0<a<4001, 0<s<4001}. This does produce the right result
   * when running individual xmas values through the DFA, but it doesn't work very well if we want
   * to summarize them. When we arrive at a state, we will have a set of constraints that must be
   * true to have got to that point. We can simply intersect that with the constraints on the set to
   * determine what must be true for the state to succeed (jump to its label). But what about the
   * other case, where we instead proceed to the next state in the list for the current label? I
   * thought that was just the complement, but of course the complement is empty since 0<x<4001
   * covers all possible x values. I now think I could probably have salvaged this fairly easily,
   * with the right thing being to proceed with the union of the complements of each individual
   * condition. But meanwhile I changed the code to use a literal representation of m>1009 and
   * updated the logic accordingly. Thankfully, the remainder of the code then worked correctly to
   * produce the result in less than a second.
   *
   * In hindsight I could have solved this much more simply, thanks to two key insights. We don't
   * need the whole business with ConstraintSet and intersection and so on. Instead we just need to
   * record, for each node in the DFA, the list of individual constraints that lead to it, like
   * m>1000,m<2000,x>3000. That list may contain redundancies (m>1000,m>2000) or contradictions
   * (m>2000,m<1000) but it doesn't matter. When building the graph, at each non-terminal node we
   * branch into adding a condition (like m>1000) and connecting to the labeled node, or adding the
   * complement of the condition (like m<1001) and connecting to the next node in the list for the
   * current line. The first insight, obvious in retrospect, is that the sets of xmas values
   * accepted by each accepting state are mutually exclusive. I had spent a lot of time worrying
   * unnecessarily about overcounting because I thought the same xmas values might be accepted by
   * more than one terminal state. The second insight is that we don't even need to calculate the
   * final ranges of values accepted by a given accepting state. Since the xmas variables are
   * independent of each other, we can simply apply the list of conditions to all x values from 1 to
   * 4000, all m values, etc, and multiply the resulting counts together.
   *
   */
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle19.class.getResourceAsStream("puzzle19.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      int empty = lines.indexOf("");
      assert empty > 0;
      Map<String, Workflow> workflows = parseWorkflows(lines.subList(0, empty));
      List<Part> parts = parseParts(lines.subList(empty + 1, lines.size()));
      int sum = 0;
      for (Part part : parts) {
        if (accept(part, workflows)) {
          sum += part.rating();
        }
      }
      System.out.println(STR."Rating total \{sum}");

      List<ConditionList> summary = summarize(workflows).stream().map(ConditionList::sorted).toList();
      System.out.println(STR."Conditions \{Joiner.on("\n").join(summary)}");
      System.out.println(STR."New rating total \{parts.stream().filter(part -> summary.stream().anyMatch(list -> list.matches(part))).mapToInt(Part::rating).sum()}");

      List<Constraints> constraints = summary.stream().map(Constraints::from).toList();
      System.out.println(STR."Constraints \{Joiner.on("\n").join(constraints)}");
      System.out.println(STR."New new rating total \{parts.stream().filter(part -> constraints.stream().anyMatch(c -> c.matches(part))).mapToInt(Part::rating).sum()}");

      ConstraintSet set = new ConstraintSet(new HashSet<>(constraints));
      System.out.println(STR."New new new rating total \{parts.stream().filter(part -> set.matches(part)).mapToInt(Part::rating).sum()}");
      System.out.println(STR."Size \{set.size()}");
    }
  }

  private static boolean accept(Part part, Map<String, Workflow> workflows) {
    Workflow current = workflows.get("in");
    while (true) {
      String newLabel = current.apply(part);
      switch (newLabel) {
        case "A":
          return true;
        case "R":
          return false;
        default:
          current = workflows.get(newLabel);
      }
    }
  }

  private static List<ConditionList> summarize(Map<String, Workflow> workflows) {
    return summarize(workflows, "in", ConditionList.EMPTY);
  }

  private static List<ConditionList> summarize(
      Map<String, Workflow> workflows, String startLabel, ConditionList path) {
    switch (startLabel) {
      case "A" -> {
        return List.of(path);
      }
      case "R" -> {
        return List.of();
      }
    }
    List<ConditionList> conditions = new ArrayList<>();
    Workflow workflow = workflows.get(startLabel);
    for (Rule rule : workflow.rules) {
      ConditionList whenTrue = path.plus(rule.condition);
      conditions.addAll(summarize(workflows, rule.target, whenTrue));
      path = path.plus(rule.condition.inverse());
    }
    conditions.addAll(summarize(workflows, workflow.defaultTarget, path));
    return conditions;
  }

  /*
  static ConstraintSet allAcceptedBy(Map<String, Workflow> workflows) {
    return allAcceptedBy(workflows, ConstraintSet.MATCH_ALL, "in");
  }

  static ConstraintSet allAcceptedBy(
      Map<String, Workflow> workflows, ConstraintSet current, String startLabel) {
    switch (startLabel) {
      case "A" -> {
        return current;
      }
      case "R" -> {
        return ConstraintSet.EMPTY;
      }
    }
    ConstraintSet accepted = ConstraintSet.EMPTY;
    Workflow workflow = workflows.get(startLabel);
    for (Rule rule : workflow.rules) {
      // If the condition in the rule is true (intersection) then we'll pass the intersection of
      // `current` and that condition into a recursive call to find everything that matches that.
      // Otherwise, we'll update `current` with the complement of that condition.
      accepted = accepted.union(allAcceptedBy(workflows, current.intersection(rule.constraints), rule.target));
      current = current.minusRule(rule.constraints);
      // This isn't right. If the rule says a<1000, we want to subtract just a<1000, not the other
      // conditions. Similarly, when we construct unions, trying to avoid duplication, we should not
      // consider duplication from conditions that are not present.
    }
    return accepted.union(allAcceptedBy(workflows, current, workflow.defaultTarget));
  }
  */

  private static final Pattern WORKFLOW_PATTERN = Pattern.compile("([a-z]+)\\{(.*)\\}");

  // px{a<2006:qkq,m>2090:A,rfg}
  static Map<String, Workflow> parseWorkflows(List<String> lines) {
    Map<String, Workflow> map = new TreeMap<>();
    for (String line : lines) {
      Matcher matcher = WORKFLOW_PATTERN.matcher(line);
      if (!matcher.matches()) {
        throw new AssertionError(line);
      }
      var old = map.put(matcher.group(1), parseWorkflow(matcher.group(2)));
      assert old == null;
    }
    return map;
  }

  static Workflow parseWorkflow(String line) {
    List<String> ruleStrings = List.of(line.split(","));
    List<Rule> rules = ruleStrings.stream().limit(ruleStrings.size() - 1).map(Puzzle19::parseRule).toList();
    return new Workflow(rules, ruleStrings.getLast());
  }

  static Rule parseRule(String ruleString) {
    char category = ruleString.charAt(0);
    char ltgt = ruleString.charAt(1);
    assert ltgt == '<' || ltgt == '>';
    int colon = ruleString.indexOf(':');
    assert colon > 0;
    int value = Integer.parseInt(ruleString.substring(2, colon));
    String target = ruleString.substring(colon + 1);
    return Rule.of(category, ltgt, value, target);
  }

  private static List<Part> parseParts(List<String> lines) {
    return lines.stream().map(Puzzle19::parsePart).toList();
  }

  private static final Pattern PART_PATTERN = Pattern.compile("\\{x=([0-9]+),m=([0-9]+),a=([0-9]+),s=([0-9]+)\\}");

  private static Part parsePart(String line) {
    Matcher matcher = PART_PATTERN.matcher(line);
    if (!matcher.matches()) {
      throw new AssertionError(line);
    }
    List<String> groups = List.of(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
    List<Integer> values = groups.stream().map(Integer::parseInt).toList();
    return Part.of(values.get(0), values.get(1), values.get(2), values.get(3));
  }

  record Workflow(List<Rule> rules, String defaultTarget) {
    String apply(Part part) {
      for (Rule rule : rules) {
        if (rule.matches(part)) {
          return rule.target();
        }
      }
      return defaultTarget;
    }
  }

  record Rule(Condition condition, String target) {
    boolean matches(Part part) {
      return condition.matches(part);
    }

    static Rule of(char category, char ltgt, int value, String target) {
      return new Rule(new Condition(category, ltgt, value), target);
    }
  }

  record Condition(char cat, char ltgt, int value) implements Comparable<Condition> {
    @Override public String toString() {
      return STR."\{cat}\{ltgt}\{value}";
    }

    boolean matches(Part part) {
      int v = part.get(cat);
      return switch (ltgt) {
        case '<' -> v < value;
        case '>' -> v > value;
        default -> throw new AssertionError(ltgt);
      };
    }

    Condition inverse() {
      // opposite of x < 5 is x > 4
      // opposite of x > 5 is x < 6
      return switch (ltgt) {
        case '<' -> new Condition(cat, '>', value - 1);
        case '>' -> new Condition(cat, '<', value + 1);
        default -> throw new AssertionError(ltgt);
      };
    }

    private static final Comparator<Condition> COMPARATOR =
        Comparator.comparingInt((Condition c) -> "xmas".indexOf(c.cat))
            .thenComparingInt(c -> "><".indexOf(c.ltgt))
            .thenComparingInt(c -> (c.ltgt == '<') ? c.value : -c.value);

    @Override
    public int compareTo(Condition that) {
      return COMPARATOR.compare(this, that);
    }
  }

  record ConditionList(List<Condition> conditions) {
    static final ConditionList EMPTY = new ConditionList(List.of());

    ConditionList plus(Condition condition) {
      return new ConditionList(ImmutableList.<Condition>builder().addAll(conditions).add(condition).build());
    }

    boolean matches(Part part) {
      return conditions.stream().allMatch(c -> c.matches(part));
    }

    ConditionList sorted() {
      return new ConditionList(conditions.stream().sorted().toList());
    }
  }

  static class Part extends LinkedHashMap<Character, Integer> {
    Part(Map<Character, Integer> map) {
      super(map);
    }

    static Part of(int x, int m, int a, int s) {
      return new Part(ImmutableMap.of('x', x, 'm', m, 'a', a, 's', s));
    }

    int rating() {
      return get('x') + get('m') + get('a') + get('s');
    }

    private static final long serialVersionUID = 0;
  }

  record Constraint(int moreThan, int lessThan) {
    /** A constraint that matches nothing. */
    static final Constraint EMPTY = new Constraint(0, 0);

    Constraint {
      boolean empty = moreThan == Integer.MAX_VALUE || lessThan == Integer.MIN_VALUE
          || moreThan + 1 >= lessThan;
      if (empty) {
        moreThan = lessThan = 0;
      }
    }

    static Constraint from(Condition condition) {
      return switch (condition.ltgt) {
        case '<' -> new Constraint(0, condition.value);
        case '>' -> new Constraint(condition.value, 4001);
        default -> throw new AssertionError(condition.ltgt);
      };
    }

    boolean matches(int value) {
      return value > moreThan && value < lessThan;
    }

    boolean isDefault() {
      return moreThan == 0 && lessThan == 4001;
    }

    /**
     * The number of values that match the constraint. For {@literal 1 < x < 10}, the number of
     * values is 8. (This value plainly doesn't need to be a {@code long} but is declared so to
     * avoid the risk of {@code int} overflow in expressions involving it.)
     */
    long size() {
      return Math.max(lessThan - moreThan - 1, 0);
    }

    boolean isEmpty() {
      return size() == 0;
    }

    /**
     * A new constraint that matches anything that both {@code this} and {@code that} match.
     */
    Constraint intersection(Constraint that) {
      Constraint result = new Constraint(
          Math.max(this.moreThan, that.moreThan),
          Math.min(this.lessThan, that.lessThan));
      return result.isEmpty() ? EMPTY : result;
    }

    /**
     * A set of constraints such that a value matches one constraint in the set if {@code this}
     * matches it but {@code that} doesn't. The set will have zero to two elements.
     */
    Set<Constraint> minus(Constraint that) {
      // If this says 2 < x < 10 and that says 4 < x < 8, then the before is 2 < x < 5 and the
      // after is 7 < x < 10. If this says 2 < x < 10 and that says 1 < x < 6, then the before is
      // 2 < x < 2 (which is empty) and the after is 5 < x < 10.
      Constraint before = new Constraint(this.moreThan, that.moreThan + 1);
      Constraint after = new Constraint(that.lessThan - 1, this.lessThan);
      return List.of(before, after).stream().filter(c -> !c.isEmpty()).collect(toImmutableSet());
    }
  }

  record Constraints(Map<Character, Constraint> map) {
    static final Constraints EMPTY = new Constraints(
        ImmutableMap.of('x', Constraint.EMPTY, 'm', Constraint.EMPTY, 'a', Constraint.EMPTY, 's', Constraint.EMPTY));

    static final Constraints MATCH_ALL = Constraints.of(0, 4001, 0, 4001, 0, 4001, 0, 4001);

    static Constraints of(
        int xMoreThan, int xLessThan, int mMoreThan, int mLessThan,
        int aMoreThan, int aLessThan, int sMoreThan, int sLessThan) {
      return new Constraints(
          ImmutableMap.of(
              'x', new Constraint(xMoreThan, xLessThan),
              'm', new Constraint(mMoreThan, mLessThan),
              'a', new Constraint(aMoreThan, aLessThan),
              's', new Constraint(sMoreThan, sLessThan)));
    }

    static Constraints from(ConditionList conditions) {
      Constraints c = MATCH_ALL;
      for (Condition condition : conditions.conditions) {
        c = c.with(condition.cat, c.map.get(condition.cat).intersection(Constraint.from(condition)));
      }
      return c;
    }

    Constraints with(char c, Constraint constraint) {
      return new Constraints(
          ImmutableMap.<Character, Constraint>builder()
              .putAll(map)
              .put(c, constraint)
              .buildKeepingLast());
    }

    boolean matches(Part part) {
      return part.keySet().stream().allMatch(c -> map.get(c).matches(part.get(c)));
    }

    /**
     * The number of combinations of variable values that match the constraints.
     */
    long size() {
      return map.values().stream().mapToLong(Constraint::size).reduce(1, (a, b) -> a * b);
    }

    boolean isEmpty() {
      return size() == 0;
    }

    /**
     * An instance that matches the values that both {@code this} and {@code that} match.
     */
    Constraints intersection(Constraints that) {
      Map<Character, Constraint> result =
          map.keySet().stream().map(c -> Map.entry(c, this.map.get(c).intersection(that.map.get(c))))
              .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
      boolean empty = result.values().stream().anyMatch(Constraint::isEmpty);
      return empty ? EMPTY : new Constraints(result);
    }

    /**
     * Returns a {@link ConstraintSet} that matches any value matched by {@code this} but not by
     * {@code that}. In general this will consist of up to 16 constraints, the product of
     * two constraints for each variable, matching values less than the intersection or more than
     * the intersection.
     */
    ConstraintSet minus(Constraints that) {
      Set<Constraint> xMinus = this.map.get('x').minus(that.map.get('x'));
      Set<Constraint> mMinus = this.map.get('m').minus(that.map.get('m'));
      Set<Constraint> aMinus = this.map.get('a').minus(that.map.get('a'));
      Set<Constraint> sMinus = this.map.get('s').minus(that.map.get('s'));
      ImmutableSet.Builder<Constraints> newConstraints = ImmutableSet.builder();
      for (Constraint x : xMinus) {
        for (Constraint m : mMinus) {
          for (Constraint a : aMinus) {
            for (Constraint s : sMinus) {
              Constraints constraints = new Constraints(ImmutableMap.of('x', x, 'm', m, 'a', a, 's', s));
              newConstraints.add(constraints);
            }
          }
        }
      }
      return new ConstraintSet(newConstraints.build());
    }

    @Override
    public String toString() {
      return map.entrySet().stream()
          .map(e -> e.getValue().moreThan + "<" + e.getKey() + "<" + e.getValue().lessThan)
          .collect(joining(",", "{", "}"));
    }
  }

  /**
   * A set of constraints, such that a {@link Part} matches the set if it matches any element of the
   * set. The sets must not overlap.
   */
  record ConstraintSet(Set<Constraints> constraintSet) {
    ConstraintSet {
      List<Constraints> a = new ArrayList<>(constraintSet);
      for (int i = 0; i < a.size(); i++) {
        for (int j = i + 1; j < a.size(); j++) {
          Constraints intersection = a.get(i).intersection(a.get(j));
          if (!intersection.isEmpty()) {
            throw new IllegalArgumentException(
                STR."Overlapping sets \{a.get(i)} and \{a.get(j)} => \{intersection}");
          }
        }
      }
    }

    static final ConstraintSet EMPTY = new ConstraintSet(Set.of());

    static final ConstraintSet MATCH_ALL = new ConstraintSet(Set.of(Constraints.MATCH_ALL));

    static ConstraintSet of(Constraints constraints) {
      return new ConstraintSet(Set.of(constraints));
    }

    boolean matches(Part part) {
      return constraintSet.stream().anyMatch(c -> c.matches(part));
    }

    long size() {
      return constraintSet.stream().mapToLong(constraint -> constraint.size()).sum();
    }

    /**
     * Return a new {@link ConstraintSet} that matches everything this one does, and also matches
     * anything matched by the given {@code constraints}.
     */
    ConstraintSet plus(Constraints constraints) {
      // We want to remove from `constraints` any ranges that are already present in `constraintSet`.
      // The result is in general a set of disjoint Contraints. For each Constraints in
      // `constraintSet`, we will remove its elements from `remaining`. The end result is a set
      // where no element has any values in common with `constraintSet`.
      Set<Constraints> remaining = Set.of(constraints);
      for (Constraints oldConstraints : constraintSet) {
        Set<Constraints> newRemaining = new LinkedHashSet<>();
        for (Constraints r : remaining) {
          newRemaining.addAll(r.minus(oldConstraints).constraintSet);
        }
        remaining = newRemaining;
      }
      return new ConstraintSet(
          ImmutableSet.<Constraints>builder().addAll(constraintSet).addAll(remaining).build());
    }

    ConstraintSet minus(Constraints constraints) {
      Set<Constraints> newConstraints = constraintSet.stream()
          .flatMap(c -> c.minus(constraints).constraintSet.stream())
          .collect(toImmutableSet());
      return new ConstraintSet(newConstraints);
    }

    /**
     * Returns a {@link ConstraintSet} that matches any value matched by {@code this} but not a
     * non-default constraint imposed by {@code that}. This is a hack because I realized that my
     * modeling was incorrect. The idea is that if we have {@code a < 1000} in a rule then we'll
     * want to add (intersect) that into the rule on one branch, and subtract it on the other, but
     * we don't want to subtract e.g. {@code 0 < x < 4001} because that will exclude all {@code x}
     * values.
     */
    ConstraintSet minusRule(Constraints that) {
      Set<Map.Entry<Character, Constraint>> nonDefault = that.map.entrySet().stream()
          .filter(entry -> !entry.getValue().isDefault())
          .collect(toImmutableSet());
      return switch (nonDefault.size()) {
        case 0 -> this;
        case 1 -> {
          Map.Entry<Character, Constraint> only = Iterables.getOnlyElement(nonDefault);
          char c = only.getKey();
          Constraint ruleConstraint = only.getValue();
          Set<Constraints> updated = constraintSet.stream()
              .map(constraints -> constraints.with(c, Iterables.getOnlyElement(constraints.map.get(c).minus(ruleConstraint))))
              .filter(constraints -> !constraints.isEmpty())
              .collect(toImmutableSet());
          yield new ConstraintSet(updated);
        }
        default -> throw new IllegalArgumentException(STR."\{this} minusRule \{that}");
      };
    }

    ConstraintSet intersection(Constraints constraints) {
      ImmutableSet<Constraints> newConstraints = constraintSet.stream()
          .map(c -> c.intersection(constraints))
          .filter(c -> !c.isEmpty())
          .collect(toImmutableSet());
      return new ConstraintSet(newConstraints);
    }

    ConstraintSet union(ConstraintSet that) {
      ConstraintSet u = this;
      for (Constraints constraints : that.constraintSet) {
        u = u.plus(constraints);
      }
      return u;
    }

    @Override
    public String toString() {
      return constraintSet.stream().map(Object::toString).collect(joining(" or ", "{", "}"));
    }
  }
}
