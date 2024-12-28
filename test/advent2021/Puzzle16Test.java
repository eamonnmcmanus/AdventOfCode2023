package advent2021;

import static com.google.common.truth.Truth.assertThat;

import advent2021.Puzzle16.BitStream;
import advent2021.Puzzle16.Literal;
import advent2021.Puzzle16.Operator;
import advent2021.Puzzle16.Packet;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

/**
 * @author Éamonn McManus
 */
public class Puzzle16Test {
  @Test
  public void literal() {
    BitStream stream = BitStream.ofHex("D2FE28");
    Packet packet = stream.parsePacket();
    assertThat(packet).isEqualTo(new Literal(6, 2021));
  }

  @Test
  public void operatorWithLength() {
    BitStream stream = BitStream.ofHex("38006F45291200");
    Packet packet = stream.parsePacket();
    assertThat(packet)
        .isEqualTo(new Operator(1, 6, ImmutableList.of(new Literal(6, 10), new Literal(2, 20))));
  }

  @Test
  public void operatorWithCount() {
    BitStream stream = BitStream.ofHex("EE00D40C823060");
    Packet packet = stream.parsePacket();
    assertThat(packet)
        .isEqualTo(
            new Operator(
                7, 3, ImmutableList.of(new Literal(2, 1), new Literal(4, 2), new Literal(1, 3))));
  }

  @Test
  public void versionSum1() {
    BitStream stream = BitStream.ofHex("8A004A801A8002F478");
    Packet packet = stream.parsePacket();
    assertThat(packet.versionSum()).isEqualTo(16);
  }

  @Test
  public void versionSum2() {
    BitStream stream = BitStream.ofHex("620080001611562C8802118E34");
    Packet packet = stream.parsePacket();
    assertThat(packet.versionSum()).isEqualTo(12);
  }

  @Test
  public void versionSum3() {
    BitStream stream = BitStream.ofHex("C0015000016115A2E0802F182340");
    Packet packet = stream.parsePacket();
    assertThat(packet.versionSum()).isEqualTo(23);
  }

  @Test
  public void versionSum4() {
    BitStream stream = BitStream.ofHex("A0016C880162017C3686B18A3D4780");
    Packet packet = stream.parsePacket();
    assertThat(packet.versionSum()).isEqualTo(31);
  }
}