package advent2023;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author Éamonn McManus
 */
public class Puzzle23 {
  public static void main(String[] args) throws Exception {
    try (InputStream in = Puzzle23.class.getResourceAsStream("puzzle23-small.txt")) {
      String lineString = new String(in.readAllBytes(), UTF_8);
      List<String> lines = List.of(lineString.split("\n"));
      char[][] cells = new char[lines.size()][];
      for (int i = 0; i < lines.size(); i++) {
        cells[i] = lines.get(i).toCharArray();
      }
      int start = lines.get(0).indexOf('.');
      assert start >= 0 && start == lines.get(0).lastIndexOf('.') : lines.get(0);
    }
  }
}
