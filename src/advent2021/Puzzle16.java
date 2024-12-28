package advent2021;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.signum;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.function.BinaryOperator;

/**
 * @author Éamonn McManus
 */
public class Puzzle16 {
  public static void main(String[] args) throws Exception {
    try (Reader r = new InputStreamReader(Puzzle16.class.getResourceAsStream("puzzle16.txt"))) {
      String input = new BufferedReader(r).readLine();
      BitStream stream = BitStream.ofHex(input);
      Packet packet = stream.parsePacket();
      int versionSum = packet.versionSum();
      System.out.printf("version sum %d\n", versionSum);
      System.out.printf("value %d\n", packet.value());
    }
  }

  sealed interface Packet {
    int version();

    BigInteger value();

    int versionSum();
  }

  record Literal(int version, BigInteger value) implements Packet {
    Literal(int version, int value) {
      this(version, BigInteger.valueOf(value));
    }

    @Override
    public int versionSum() {
      return version;
    }
  }

  record Operator(int version, int type, ImmutableList<Packet> subpackets) implements Packet {
    @Override
    public int versionSum() {
      return version + subpackets.stream().mapToInt(p -> p.versionSum()).sum();
    }

    @Override
    public BigInteger value() {
      BinaryOperator<BigInteger> pairwise = switch (type) {
        case 0 -> BigInteger::add;
        case 1 -> BigInteger::multiply;
        case 2 -> BigInteger::min;
        case 3 -> BigInteger::max;
        case 5 -> (a, b) -> compare(a, b, 1);
        case 6 -> (a, b) -> compare(a, b, -1);
        case 7 -> (a, b) -> compare(a, b, 0);
        default -> throw new AssertionError(type);
      };
      BigInteger acc = subpackets.get(0).value();
      for (var packet : subpackets.subList(1, subpackets.size())) {
        acc = pairwise.apply(acc, packet.value());
      }
      return acc;
    }

    private static BigInteger compare(BigInteger a, BigInteger b, int trueSignum) {
      return signum(a.compareTo(b)) == trueSignum ? BigInteger.ONE : BigInteger.ZERO;
    }
  }

  static class BitStream {
    private final BigInteger bits;
    private int bitIndex;

    private BitStream(BigInteger bits, int bitIndex) {
      this.bits = bits;
      this.bitIndex = bitIndex;
    }

    static BitStream ofHex(String hex) {
      BigInteger bits = new BigInteger(hex, 16);
      int bitIndex = hex.length() * 4 - 1;
      return new BitStream(bits, bitIndex);
    }

    boolean eof() {
      return bitIndex < 0;
    }

    private boolean readBit() {
      checkState(bitIndex >= 0, "EOF");
      boolean result = bits.testBit(bitIndex);
      --bitIndex;
      return result;
    }

    private int readBits(int n) {
      int result = 0;
      for (int i = 0; i < n; i++) {
        result = (result << 1) | (readBit() ? 1 : 0);
      }
      return result;
    }

    Packet parsePacket() {
      int version = readBits(3);
      int type = readBits(3);
      return switch (type) {
        case 4 -> parseLiteral(version);
        default -> parseOperator(version, type);
      };
    }

    private Literal parseLiteral(int version) {
      BigInteger result = BigInteger.ZERO;
      boolean more;
      do {
        more = readBit();
        int nybble = readBits(4);
        result = result.shiftLeft(4).or(BigInteger.valueOf(nybble));
      } while (more);
      return new Literal(version, result);
    }

    private Operator parseOperator(int version, int type) {
      ImmutableList.Builder<Packet> subpackets = ImmutableList.builder();
      if (readBit()) {
        // length type id 1, next 11 bits indicate how many subpackets
        int count = readBits(11);
        for (int i = 0; i < count; i++) {
          subpackets.add(parsePacket());
        }
      } else {
        // length type id 0, next 15 bits indicate size of subpacket bits
        int size = readBits(15);
        checkState(size <= bitIndex);
        int stop = bitIndex - size;
        while (bitIndex > stop) {
          subpackets.add(parsePacket());
        }
        checkState(bitIndex == stop);
      }
      return new Operator(version, type, subpackets.build());
    }
  }
}