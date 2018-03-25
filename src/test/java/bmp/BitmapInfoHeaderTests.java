package bmp;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import utils.NioUtils;
import utils.NioUtils.ChannelAndBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

import static bmp.BitmapInfoHeaderTests.Version.*;
import static bmp.PixmapType.PIXEL_MATRIX;
import static bmp.PixmapType.RLE;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BitmapInfoHeaderTests {
    // rle format | pixmatrix formats with different bitCount;
    // correct header | incorrect header
    // version
    // compression
    // channel masks default
    // channel masks custom


    @Test
    public void shouldRecognizeImageShapeCorrectly() throws Exception {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .width(1600)
                .height(900)
                .build();

        for (Version version : Version.values()) {
            ReadableByteChannel testHeader = bitmapInfo.toChannel(version);
            BitmapInfoHeader parsedHeader = BitmapInfoHeader.read(testHeader).getHeader();
            assertEquals("Width in version " + version, 1600, parsedHeader.width());
            assertEquals("Height in version" + version, 900, parsedHeader.height());
        }
    }

    @Test
    public void shouldRecognizeV5Rle8() throws Exception {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .bitCount(8)
                .compression(1)
                .clrUsed(174)
                .build();
        ReadableByteChannel testHeader = bitmapInfo.toChannel(V5);
        BitmapInfoHeader parsedHeader = BitmapInfoHeader.read(testHeader).getHeader();

        assertEquals(RLE, parsedHeader.type());
        assertEquals(8, parsedHeader.bitsPerPixel());
        assertEquals(new ChannelMasks(0xff, 0, 0, 0, 0), parsedHeader.channelMasks());
    }

    @Test
    public void shouldUseDefaultMasksWhenTheyAreNotSpecified() throws Exception {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .bitCount(32)
                .compression(3) // BI_BITFIELDS
                .build();
        ReadableByteChannel testHeader = bitmapInfo.toChannel(V4);
        BitmapInfoHeader parsedHeader = BitmapInfoHeader.read(testHeader).getHeader();
        ChannelMasks parsedMasks = parsedHeader.channelMasks();

        assertEquals(PIXEL_MATRIX, parsedHeader.type());
        assertEquals(32, parsedHeader.bitsPerPixel());
        assertThat(parsedMasks.redMask(), not(0));
        assertThat(parsedMasks.greenMask(), not(0));
        assertThat(parsedMasks.blueMask(), not(0));
        assertEquals(parsedMasks.alphaMask(), 0); // по умолчанию альфа-канал не используется

    }

    @Test
    public void shouldRecognizeV4Pixmatrix16bitCustomMasks() throws Exception {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .alpha(0b00_00_00_11)
                .red(0b11_00_00_00)
                .green(0b00_11_00_00)
                .blue(0b00_00_11_00)
                .width(600)
                .height(300)
                .bitCount(16)
                .clrUsed(0)
                .compression(6)
                .build();
        ReadableByteChannel testHeader = bitmapInfo.toChannel(V4);
        BitmapInfoHeader parsedHeader = BitmapInfoHeader.read(testHeader).getHeader();

        assertEquals(300, parsedHeader.height());
        assertEquals(600, parsedHeader.width());
        assertEquals(16, parsedHeader.bitsPerPixel());
        assertEquals(PixmapType.PIXEL_MATRIX, parsedHeader.type());
        assertEquals(new ChannelMasks(0, 0b11 << 6, 0b11 << 4, 0b11 << 2, 0b11), parsedHeader.channelMasks());
    }

    @Builder
    private static class BitmapInfo {
        @Builder.Default
        int width = 1600;
        @Builder.Default
        int height = 900;
        @Builder.Default
        int bitCount = 16;
        int compression = 0;
        int clrUsed = 0;

        int red = 0;
        int green = 0;
        int blue = 0;
        int alpha = 0;

        ReadableByteChannel toChannel(Version v) {

            ChannelAndBuffer channelAndBuffer = NioUtils.newChannelAndBuffer(v.size);

            ByteBuffer bb = channelAndBuffer.getBuffer().putInt(0x00, v.size);
            if (v == CORE) {
                bb.putShort(0x04, (short) width)
                        .putShort(0x06, (short) height)
                        .putShort(0x0a, (short) bitCount);
            } else {
                bb.putInt(0x04, width)
                        .putInt(0x08, height)
                        .putShort(0x0e, (short) bitCount)
                        .putInt(0x10, compression)
                        .putInt(0x20, clrUsed);
                if (v == V4 || v == V5) {
                    bb.putInt(0x28, red)
                            .putInt(0x2c, green)
                            .putInt(0x30, blue)
                            .putInt(0x34, alpha);
                }
            }
            return channelAndBuffer.getChannel();
        }
    }

    @RequiredArgsConstructor
    enum Version {
        CORE(12), V3(40), V4(108), V5(124);
        final int size;
    }
}