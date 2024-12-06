package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.math.LongMath;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle20 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle20.class.getResourceAsStream("puzzle20.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      solvePart1(lines);
      solvePart2(lines);
    }
  }

  static void solvePart1(List<String> lines) {
    Map<String, Module> modules = parseModules(lines);
    long lowCount = 0;
    long highCount = 0;
    Set<String> ignored = new TreeSet<>();
    for (int i = 0; i < 1000; i++) {
      List<Signal> signals = List.of(new Signal(null, "broadcaster", false));
      while (!signals.isEmpty()) {
        long thisHigh = signals.stream().filter(s -> s.pulse).count();
        highCount += thisHigh;
        lowCount += signals.size() - thisHigh;
        List<Signal> newSignals = new ArrayList<>();
        for (Signal signal : signals) {
          Module target = modules.get(signal.targetModuleName);
          if (target == null) {
            if (ignored.add(signal.targetModuleName)) {
              System.out.println("Ignore " + signal.targetModuleName);
            }
          } else {
            newSignals.addAll(target.receive(signal.source, signal.pulse));
          }
        }
        signals = newSignals;
      }
    }
    System.out.println(
        "Low " + lowCount + " high " + highCount + " product " + lowCount * highCount);
  }

  /*
  I suspected that the circuit was made up of cyclic subcircuits, so the final signal to rx would
  happen only when each of those subcircuits finished its cycle at the same time. That would mean
  the number of iterations ("button pushes") would be the LCM of those cycles. I could have
  investigated that to confirm it, but instead I confirmed it by cheating, via
  https://colab.sandbox.google.com/github/derailed-dash/Advent-of-Code/blob/master/src/AoC_2023/Dazbo%27s_Advent_of_Code_2023.ipynb#scrollTo=EFS4IeuPndFb
  */
  static void solvePart2(List<String> lines) {
    Map<String, Module> modules = parseModules(lines);

    // Verify that rx has exactly one input, call it vf, that is a Conjunction.
    List<Map.Entry<String, Module>> rxInputs =
        modules.entrySet().stream().filter(e -> e.getValue().targetModules.contains("rx")).toList();
    assert rxInputs.size() == 1 : rxInputs;
    assert rxInputs.get(0).getValue() instanceof Conjunction;
    String rxInput = rxInputs.get(0).getKey();
    System.out.println("Input to rx is " + rxInput);

    // Verify that vf has four inputs which are also Conjuctions.
    List<Map.Entry<String, Module>> vfInputs =
        modules.entrySet().stream()
            .filter(e -> e.getValue().targetModules.contains(rxInput))
            .toList();
    assert vfInputs.size() == 4;
    assert vfInputs.stream().allMatch(e -> e.getValue() instanceof Conjunction);

    // Observe when each of those inputs first sends a high pulse.
    AtomicLong i = new AtomicLong();
    Map<String, Long> first = new TreeMap<>();
    for (var entry : vfInputs) {
      entry
          .getValue()
          .observers
          .add(
              pulse -> {
                if (pulse) {
                  System.out.println("First high pulse for " + entry.getKey() + " at i=" + i.get());
                  first.put(entry.getKey(), i.get());
                }
              });
    }

    for (i.set(1); i.get() < 1_000_000; i.incrementAndGet()) {
      List<Signal> signals = List.of(new Signal(null, "broadcaster", false));
      while (!signals.isEmpty()) {
        List<Signal> newSignals = new ArrayList<>();
        for (Signal signal : signals) {
          String targetName = signal.targetModuleName;
          if (targetName.equals("rx")) {
            if (!signal.pulse) {
              System.out.println("Received after " + i.get() + " pushes");
              break;
            }
          } else {
            Module target = modules.get(targetName);
            if (target != null) {
              newSignals.addAll(target.receive(signal.source, signal.pulse));
            }
          }
        }
        signals = newSignals;
      }
      if (first.size() == 4) {
        break;
      }
    }

    long lcm = first.values().stream().reduce(1L, (a, b) -> lcm(a, b));
    System.out.println("LCM is " + lcm);
  }

  static long lcm(long a, long b) {
    long gcd = LongMath.gcd(a, b);
    return a / gcd * b;
  }

  record Signal(Module source, String targetModuleName, boolean pulse) {}

  abstract static class Module {
    final Set<String> targetModules;
    final List<Consumer<Boolean>> observers = new ArrayList<>(0);

    Module(Set<String> targetModules) {
      this.targetModules = targetModules;
    }

    List<Signal> send(boolean pulse) {
      observers.forEach(o -> o.accept(pulse));
      return targetModules.stream().map(module -> new Signal(this, module, pulse)).toList();
    }

    abstract List<Signal> receive(Module source, boolean pulse);
  }

  static class FlipFlop extends Module {
    boolean state;

    FlipFlop(Set<String> otherModules) {
      super(otherModules);
    }

    @Override
    List<Signal> receive(Module source, boolean pulse) {
      if (!pulse) {
        state = !state;
        return send(state);
      } else {
        return List.of();
      }
    }
  }

  static class Conjunction extends Module {
    final Map<Module, Boolean> inputs = new HashMap<>();

    Conjunction(Set<String> otherModules) {
      super(otherModules);
    }

    @Override
    List<Signal> receive(Module source, boolean pulse) {
      inputs.put(source, pulse);
      // If all the inputs are high (!values.contains(false)) then the output should be low.
      boolean output = inputs.values().contains(false);
      return send(output);
    }
  }

  static class Broadcaster extends Module {
    Broadcaster(Set<String> otherModules) {
      super(otherModules);
    }

    @Override
    List<Signal> receive(Module source, boolean pulse) {
      return send(pulse);
    }
  }

  private static final Pattern LINE_PATTERN = Pattern.compile("(.*) -> (.*)");

  static Map<String, Module> parseModules(List<String> lines) {
    Map<String, Module> modules = new TreeMap<>();
    for (String line : lines) {
      Matcher matcher = LINE_PATTERN.matcher(line);
      if (!matcher.matches()) {
        throw new AssertionError(line);
      }
      Set<String> targets = Set.of(matcher.group(2).split(", "));
      String lhs = matcher.group(1);
      if (lhs.startsWith("%")) {
        modules.put(lhs.substring(1), new FlipFlop(targets));
      } else if (lhs.startsWith("&")) {
        modules.put(lhs.substring(1), new Conjunction(targets));
      } else if (lhs.equals("broadcaster")) {
        modules.put("broadcaster", new Broadcaster(targets));
      } else {
        throw new AssertionError(line);
      }
    }

    // Now for every module that is wired to a Conjunction, we need to send a fake low pulse to the
    // Conjuction so it remembers that.
    for (Module module : modules.values()) {
      for (String target : module.targetModules) {
        if (modules.get(target) instanceof Conjunction) {
          modules.get(target).receive(module, false);
        }
      }
    }

    return modules;
  }
}
/*
broadcaster -> a, b, c
%a -> b
%b -> c
%c -> inv
&inv -> a
*/
