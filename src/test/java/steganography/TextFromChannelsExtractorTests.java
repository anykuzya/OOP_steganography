package steganography;

import org.junit.Test;
import utils.NioUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;
import static utils.NioUtils.createByteBuffer;

public class TextFromChannelsExtractorTests {

    @Test
    public void shouldExtractTwoBytesAndHeaderAndIgnoreNextChannels() {
        ByteBuffer toExtract = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(0,2) //size
                .put(4, (byte) 0b1) //encoding
                .put(5, (byte) 0b0101_1011).put(6, (byte) 0b0111_1011);
        long[] longsWithEmbeddedData = new long[]{
                // utility header
                0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,
                //header
                0b10, 0b00, 0b00, 0b00, //size (2) -- 4 bytes
                0b00, 0b00, 0b00, 0b00,
                0b00, 0b00, 0b00, 0b00,
                0b00, 0b00, 0b00, 0b00,
                0b01, 0b00, 0b00, 0b00, //encoding (1) -- 1 byte
                //data
                0b1000_1000_0000_0000_1111_1111_0000_1111_1011L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1010L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1011L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1010L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1011L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,
                0b1000_1000_0000_0000_1111_1111_0000_1111_1001L,
                0b1000_1000_0000_0010_1101_1111_0000_1111_1000L,
                0b1000_1000_0000_0010_1101_1111_0000_1111_1000L,
                0b1000_1000_0000_0010_1101_1111_0000_1111_1000L,
                0b1000_1000_0000_0000_1111_1111_0000_1110_1011L
        };

        TextFromChannelsExtractor extractor = new TextFromChannelsExtractor();
        for (long l: longsWithEmbeddedData) {
            extractor.consumeChannel(l);
        }
        extractor.finishConsumption();

        assertEquals(toExtract, extractor.extractedData());
    }

}