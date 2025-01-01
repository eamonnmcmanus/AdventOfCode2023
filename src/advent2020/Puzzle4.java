package advent2020;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * @author Éamonn McManus
 */
public class Puzzle4 {
  private static final String SAMPLE =
      """
      ecl:gry pid:860033327 eyr:2020 hcl:#fffffd
      byr:1937 iyr:2017 cid:147 hgt:183cm

      iyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884
      hcl:#cfa07d byr:1929

      hcl:#ae17e1 iyr:2013
      eyr:2024
      ecl:brn pid:760753108 byr:1931
      hgt:179cm

      hcl:#cfa07d eyr:2025 pid:166559648
      iyr:2011 ecl:brn hgt:59in
      """;

  private static final Map<String, Callable<Reader>> INPUT_PRODUCERS =
      ImmutableMap.of(
          "sample", () -> new StringReader(SAMPLE),
          "problem", () -> new InputStreamReader(Puzzle4.class.getResourceAsStream("puzzle4.txt")));

  private static final ImmutableSet<String> REQUIRED_FIELDS =
      ImmutableSet.of("byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid");

  public static void main(String[] args) throws Exception {
    for (var entry : INPUT_PRODUCERS.entrySet()) {
      String name = entry.getKey();
      try (Reader r = entry.getValue().call()) {
        String text = CharStreams.toString(r);
        Pattern whitespace = Pattern.compile("\\s");
        List<ImmutableMap<String, String>> maps =
            Splitter.on("\n\n")
                .splitToStream(text)
                .map(
                    s ->
                        Splitter.on(whitespace)
                            .omitEmptyStrings()
                            .splitToStream(s)
                            .map(ss -> Splitter.on(':').splitToList(ss))
                            .collect(toImmutableMap(kv -> kv.get(0), kv -> kv.get(1))))
                .toList();
        long validCount1 =
            maps.stream().filter(map -> map.keySet().containsAll(REQUIRED_FIELDS)).count();
        System.out.printf("For %s, Part 1 valid count is %d\n", name, validCount1);
        long validCount2 =
            maps.stream()
                .filter(map -> map.keySet().containsAll(REQUIRED_FIELDS) && valid(map))
                .count();
        System.out.printf("For %s, Part 2 valid count is %d\n", name, validCount2);
      }
    }
  }

  private static boolean valid(ImmutableMap<String, String> passport) {
    return validYear(passport.get("byr"), 1920, 2002)
        && validYear(passport.get("iyr"), 2010, 2020)
        && validYear(passport.get("eyr"), 2020, 2030)
        && validHeight(passport.get("hgt"))
        && validHairColour(passport.get("hcl"))
        && validEyeColour(passport.get("ecl"))
        && validPassportId(passport.get("pid"));
  }

  private static final Pattern YEAR_PATTERN = Pattern.compile("[0-9]{4}");

  private static boolean validYear(String value, int lower, int upper) {
    int y;
    return YEAR_PATTERN.matcher(value).matches()
        && (y = Integer.parseInt(value)) >= lower
        && y <= upper;
  }

  private static final Pattern HEIGHT_PATTERN = Pattern.compile("([0-9]+)(in|cm)");

  private static boolean validHeight(String height) {
    var matcher = HEIGHT_PATTERN.matcher(height);
    if (!matcher.matches()) {
      return false;
    }
    int h = Integer.parseInt(matcher.group(1));
    return switch (matcher.group(2)) {
      case "cm" -> 150 <= h && h <= 193;
      case "in" -> 59 <= h && h <= 76;
      default -> throw new AssertionError(matcher.group(2));
    };
  }

  private static final Pattern HAIR_PATTERN = Pattern.compile("#[0-9a-f]{6}");

  private static boolean validHairColour(String colour) {
    return HAIR_PATTERN.matcher(colour).matches();
  }

  private static final Pattern EYE_PATTERN = Pattern.compile("amb|blu|brn|gry|grn|hzl|oth");

  private static boolean validEyeColour(String colour) {
    return EYE_PATTERN.matcher(colour).matches();
  }

  private static final Pattern PASSPORT_ID_PATTERN = Pattern.compile("[0-9]{9}");

  private static boolean validPassportId(String id) {
    return PASSPORT_ID_PATTERN.matcher(id).matches();
  }
}