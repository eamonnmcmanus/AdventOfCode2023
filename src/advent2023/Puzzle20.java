package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.FormatProcessor.FMT;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
      Map<String, Module> modules = parseModules(lines);
      solvePart2(modules);
    }
  }

  static void solvePart1(Map<String, Module> modules) {
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
              System.out.println(STR."Ignore \{signal.targetModuleName}");
            }
          } else {
            newSignals.addAll(target.receive(signal.source, signal.pulse));
          }
        }
        signals = newSignals;
      }
    }
    System.out.println(STR."Low \{lowCount} high \{highCount} product \{lowCount * highCount}");
  }

  static void solvePart2(Map<String, Module> modules) {
    Set<String> ignored = new TreeSet<>();
    System.out.println(STR."Started at \{LocalDateTime.now()}");
    long startTime = System.nanoTime();
    for (long i = 0; i < 10_000_000_000L; i++) {
      if ((i & ((1L << 20) - 1)) == 0) {
        long elapsed = (System.nanoTime() - startTime) / 1_000_000_000;
        System.out.println(FMT."i=%,d\{i} elapsed %d\{elapsed / 60}:%02d\{elapsed % 60}");
      }
      List<Signal> signals = List.of(new Signal(null, "broadcaster", false));
      while (!signals.isEmpty()) {
        List<Signal> newSignals = new ArrayList<>();
        for (Signal signal : signals) {
          String targetName = signal.targetModuleName;
          if (targetName.equals("rx")) {
            if (!signal.pulse) {
              System.out.println(STR."Received after \{i + 1} pushes");
              break;
            }
          } else {
            Module target = modules.get(targetName);
            if (target == null) {
              if (ignored.add(signal.targetModuleName)) {
                System.out.println(STR."Ignore \{signal.targetModuleName}");
              }
            } else {
              newSignals.addAll(target.receive(signal.source, signal.pulse));
            }
          }
        }
        signals = newSignals;
      }
    }

  }

  record Signal(Module source, String targetModuleName, boolean pulse) {}

  abstract static class Module {
    final Set<String> targetModules;

    Module(Set<String> targetModules) {
      this.targetModules = targetModules;
    }

    List<Signal> send(boolean pulse) {
      return targetModules.stream().map(module -> new Signal(this, module, pulse)).toList();
    }

    abstract List<Signal> receive(Module source, boolean pulse);
  }

  static class FlipFlop extends Module {
    boolean state;

    FlipFlop(Set<String> otherModules) {
      super(otherModules);
    }

    @Override List<Signal> receive(Module source, boolean pulse) {
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

    @Override List<Signal> receive(Module source, boolean pulse) {
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

    @Override List<Signal> receive(Module source, boolean pulse) {
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
