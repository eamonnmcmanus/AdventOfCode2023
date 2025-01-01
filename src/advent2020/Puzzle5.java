package advent2020;

import static java.util.stream.Collectors.toCollection;

import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * @author Éamonn McManus
 */
public class Puzzle5 {
  public static void main(String[] args) throws Exception {
    try (Reader r = new InputStreamReader(Puzzle5.class.getResourceAsStream("puzzle5.txt"))) {
      List<String> lines = CharStreams.readLines(r);
      NavigableSet<Integer> seats =
          lines.stream().map(Puzzle5::seatId).collect(toCollection(TreeSet::new));
      int max = seats.getLast();
      System.out.printf("Highest seat id is %d\n", max);
      for (int seat : seats) {
        if (seats.higher(seat) == seat + 2) {
          System.out.printf("Missing seat id is %d\n", seat + 1);
          break;
        }
      }
    }
  }

  private static int seatId(String seat) {
    int row =
        seat.substring(0, 7)
            .replace('B', '1')
            .replace('F', '0')
            .transform(s -> Integer.valueOf(s, 2));
    int col =
        seat.substring(7).replace('R', '1').replace('L', '0').transform(s -> Integer.valueOf(s, 2));
    return row * 8 + col;
  }
}
