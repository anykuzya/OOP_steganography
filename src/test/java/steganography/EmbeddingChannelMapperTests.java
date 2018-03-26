package steganography;

import org.junit.Assert;
import org.junit.Test;
import utils.NioUtils;

import static org.junit.Assert.*;

public class EmbeddingChannelMapperTests {

    @Test
    public void shouldEmbed2bytesWith2bitsPerChannel() {
        byte[] bytesToEmbed = new byte[]{0b0101_1011, 0b0111_1011};
        EmbeddingChannelMapper channelMapper = new EmbeddingChannelMapper(2, NioUtils.createByteBuffer(bytesToEmbed));
        // хотим вшить 2 байта (16 бит), по 2 бита в каждый лонг. нужно под это 8 лонгов
        // в первой число вообще-то должны вшить 2 бита, соответсвующие информации о том сколько вшито во все следующие (3)
        // итого надо 9 чисел, и еще 10е для того, чтобы убедиться, что оно не поменяется
        long longExample = 0b1000_1000_0000_0000_1111_1111_0000_1111_1010L;

        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1001L, channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1011L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1010L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1011L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1010L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1011L,  channelMapper.mapChannel(longExample));
        assertEquals(0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,  channelMapper.mapChannel(longExample));
        assertEquals(longExample,  channelMapper.mapChannel(longExample));
        assertEquals(longExample,  channelMapper.mapChannel(longExample));
    }

}