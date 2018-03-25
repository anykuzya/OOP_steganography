package bmp;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    public void shouldRecognizeImageShapeCorrectly() {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .width(1600)
                .height(900)
                .build();

        for (Version version : Version.values()) {
            ByteBuffer testHeader = bitmapInfo.toBytes(version);
            BitmapInfoHeader parsedHeader = BitmapInfoHeader.ofBytes(testHeader);
            assertEquals("Width in version " + version, 1600, parsedHeader.width());
            assertEquals("Height in version" + version, 900, parsedHeader.height());
        }
    }

    @Test
    public void shouldRecognizeV5Rle8() {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .bitCount(8)
                .compression(1)
                .clrUsed(174)
                .build();
        ByteBuffer testHeader = bitmapInfo.toBytes(V5);
        BitmapInfoHeader parsedHeader = BitmapInfoHeader.ofBytes(testHeader);

        assertEquals(RLE, parsedHeader.type());
        assertEquals(8, parsedHeader.bitsPerPixel());
        assertEquals(new ChannelMasks(0xff, 0, 0, 0, 0), parsedHeader.channelMasks());
    }

    @Test
    public void shouldUseDefaultMasksWhenTheyAreNotSpecified() {
        BitmapInfo bitmapInfo = BitmapInfo.builder()
                .bitCount(32)
                .compression(3) // BI_BITFIELDS
                .build();
        ByteBuffer testHeader = bitmapInfo.toBytes(V4);
        BitmapInfoHeader parsedHeader = BitmapInfoHeader.ofBytes(testHeader);
        ChannelMasks parsedMasks = parsedHeader.channelMasks();

        assertEquals(PIXEL_MATRIX, parsedHeader.type());
        assertEquals(32, parsedHeader.bitsPerPixel());
        assertThat(parsedMasks.redMask(), not(0));
        assertThat(parsedMasks.greenMask(), not(0));
        assertThat(parsedMasks.blueMask(), not(0));
        assertEquals(parsedMasks.alphaMask(), 0); // по умолчанию альфа-канал не используется

    }

    @Test
    public void shouldRecognizeV4Pixmatrix16bitCustomMasks() {
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
        ByteBuffer testHeader = bitmapInfo.toBytes(V4);
        BitmapInfoHeader parsedHeader = BitmapInfoHeader.ofBytes(testHeader);

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

        ByteBuffer toBytes(Version v) {
            ByteBuffer bb = ByteBuffer.allocate(v.size)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(0x00, v.size);
            if (v == CORE) {
                return bb.putShort(0x04, (short) width)
                        .putShort(0x06, (short) height)
                        .putShort(0x0a, (short) bitCount);
            }
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
            return bb;
        }
    }

    @RequiredArgsConstructor
    enum Version {
        CORE(12), V3(40), V4(108), V5(124);
        final int size;
    }
}