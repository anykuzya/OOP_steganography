package bmp.pixmap;

import bmp.BitmapInfoHeader;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

import static bmp.PixmapType.PIXEL_MATRIX;
import static bmp.PixmapType.RLE;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

@RequiredArgsConstructor
public class MatrixPixmapReader implements PixmapReader {

    private final ChannelConsumer channelConsumer;

    @Override
    public void readPixmap(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        validateHeader(infoHeader);

        ByteBuffer pixmapView = pixmap.asReadOnlyBuffer().order(LITTLE_ENDIAN);
        LongUnaryOperator pixelConsumer = pixelConsumer(infoHeader);

        for (int h = 0; h < infoHeader.height(); h++) {
            consumeLine(pixmapView, infoHeader, pixelConsumer);
        }
        channelConsumer.finishConsumption();
    }

    private void consumeLine(ByteBuffer pixmap, BitmapInfoHeader infoHeader, LongUnaryOperator pixelConsumer) {
        for (int w = 0; w < infoHeader.width(); w++) {
            long pixel = nextPixel(pixmap, infoHeader.bitsPerPixel());
            pixelConsumer.applyAsLong(pixel);
        }
        int remainder = infoHeader.width() * (infoHeader.bitsPerPixel() / Byte.SIZE) % 4;
        if (remainder != 0) {
            for (int i = 0; i < 4 - remainder; i++ ) {
                pixmap.get();
            }
        }
    }

    private long nextPixel(ByteBuffer pixmap, int bitsPerPixel) {
        long pixel = 0;
        for (int i = 0; i < bitsPerPixel / Byte.SIZE; i++ ) {
            long currentByte = pixmap.get() & 0xff;
            pixel |= currentByte << (i * Byte.SIZE);
        }
        return pixel;
    }

    private LongUnaryOperator pixelConsumer(BitmapInfoHeader infoHeader) {
        return infoHeader.channelMasks().pixelMapper(channel -> {
            this.channelConsumer.consumeChannel(channel);
            return channel;
        });
    }

    private static void validateHeader(BitmapInfoHeader infoHeader) {
        if (infoHeader.bitsPerPixel() % 8 != 0 || infoHeader.type() != PIXEL_MATRIX) {
            throw new UnsupportedOperationException();
        }
    }
}
