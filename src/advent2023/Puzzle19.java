package advent2023;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.InputStream;
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

    private static final Pattern WORKFLOW_PATTERN = Pattern.compile("([a-z]+)\\{(.*)\\}");

    // px{a<2006:qkq,m>2090:A,rfg}
    private static Map<String, Workflow> parseWorkflows(List<String> lines) {
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

    private static Workflow parseWorkflow(String line) {
	List<String> ruleStrings = List.of(line.split(","));
	List<Rule> rules = ruleStrings.stream().limit(ruleStrings.size() - 1).map(Puzzle19::parseRule).toList();
	return new Workflow(rules, ruleStrings.getLast());
    }

    private static Rule parseRule(String ruleString) {
	char category = ruleString.charAt(0);
	char ltgt = ruleString.charAt(1);
	assert ltgt == '<' || ltgt == '>';
	int colon = ruleString.indexOf(':');
	assert colon > 0;
	int value = Integer.parseInt(ruleString.substring(2, colon));
	String target = ruleString.substring(colon + 1);
	return new Rule(category, ltgt, value, target);
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

  record Rule(char category, char ltgt, int value, String target) {
    boolean matches(Part part) {
      int partValue = part.get(category);
      return (ltgt == '<') ? partValue < value : partValue > value;
    }
  }

  static class Part extends TreeMap<Character, Integer> {
    Part(Map<Character, Integer> map) {
      super(map);
    }

    static Part of(int x, int m, int a, int s) {
      return new Part(Map.of('x', x, 'm', m, 'a', a, 's', s));
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

    boolean matches(int value) {
      return value > moreThan && value < lessThan;
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
      ImmutableSet.Builder<Constraints> newConstraints = ImmutableSet.builder();
      for (char c : map.keySet()) {
        Set<Constraint> minus = this.map.get(c).minus(that.map.get(c));
        for (Constraint con : minus) {
          newConstraints.add(this.with(c, con));
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
   * set.
   */
  record ConstraintSet(Set<Constraints> constraintSet) {
    static ConstraintSet of(Constraints constraints) {
      return new ConstraintSet(Set.of(constraints));
    }

    boolean matches(Part part) {
      return constraintSet.stream().anyMatch(c -> c.matches(part));
    }

    ConstraintSet plus(Constraints constraints) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return constraintSet.stream().map(Object::toString).collect(joining(" or ", "{", "}"));
    }
  }
}
