package bmp;

import junit.runner.Version;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static bmp.BitmapInfoHeaderTests.Version.*;
import static org.junit.Assert.*;

public class BitmapInfoHeaderTests {
    // rle format | pixmatrix formats with different bitCount;
    // correct header | incorrect header
    // version
    // compression
    // channel masks default
    // channel masks custom

    @Test
    public void shouldRecognizeV4pixmatrix16bitCustomMasks() {
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
        assertEquals(new ChannelMasks(0, 0b11 << 6, 0b11 << 4, 0b11 << 2, 0b11), parsedHeader.channelMaks());
    }

    @Builder
    private static class BitmapInfo{
        int width;
        int height;
        int bitCount;
        int compression;
        int clrUsed;

        int red;
        int green;
        int blue;
        int alpha;

        public ByteBuffer toBytes(Version v) {
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
                    .putShort(0x0e, (short)bitCount)
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