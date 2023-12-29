package advent2023;

import static java.lang.Integer.max;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle15 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle15.class.getResourceAsStream("puzzle15.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8).trim();
      List<String> entries = List.of(lineString.split(","));
      int sum = 0;
      for (String s : entries) {
        sum += hash(s);
      }
      System.out.println(STR."Sum \{sum}");
      List<List<Lens>> boxes = new ArrayList<>();
      for (int i = 0; i < 256; i++) {
        boxes.add(new ArrayList<>());
      }
      for (String entry : entries) {
        int eq = entry.indexOf('=');
        int minus = entry.indexOf('-');
        assert (eq > 0) != (minus > 0);
        String label = entry.substring(0, max(eq, minus));
        int boxNumber = hash(label);
        List<Lens> lenses = boxes.get(boxNumber);
        int index = indexOf(lenses, label);
        if (eq > 0) {
          int n = Integer.parseInt(entry.substring(eq + 1));
          Lens newLens = new Lens(label, n);
          if (index >= 0) {
            lenses.set(index, newLens);
          } else {
            lenses.add(newLens);
          }
        } else if (index >= 0) {
          lenses.remove(index);
        }
      }
      int power = 0;
      for (int i = 0; i < boxes.size(); i++) {
        List<Lens> lenses = boxes.get(i);
        for (int j = 0; j < lenses.size(); j++) {
          power += (i + 1) * (j + 1) * lenses.get(j).n();
        }
      }
      System.out.println(STR."Power: \{power}");
    }
  }

  private static int indexOf(List<Lens> lenses, String label) {
    for (int i = 0; i < lenses.size(); i++) {
      if (lenses.get(i).label().equals(label)) {
        return i;
      }
    }
    return -1;
  }

  record Lens(String label, int n) {}

  private static int hash(String s) {
    int h = 0;
    for (char c : s.toCharArray()) {
      h += c;
      h *= 17;
      h &= 255;
    }
    return h;
  }
}
