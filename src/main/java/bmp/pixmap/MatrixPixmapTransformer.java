package bmp.pixmap;

import bmp.BitmapInfoHeader;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

@RequiredArgsConstructor
public class MatrixPixmapTransformer implements PixmapTransformer {

    private final ChannelMapperFactory channelMapperFactory;

    @Override
    public void transform(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        int channelCount = totalEditableChannelsCount(pixmap, infoHeader);
        ChannelMapper channelMapper = this.channelMapperFactory.createChannelMapper(channelCount);
        LongUnaryOperator pixelMapper = infoHeader.channelMasks().pixelMapper(channelMapper::mapChannel);

        ByteBuffer pixmapReadView = pixmap.duplicate().order(LITTLE_ENDIAN);
        ByteBuffer pixmapWriteView = pixmap.duplicate().order(LITTLE_ENDIAN);

        for (int h = 0; h < infoHeader.height(); h++) {
            transformLine(pixmapReadView, pixmapWriteView, infoHeader, pixelMapper);
        }
    }

    private void transformLine(ByteBuffer pixmapReadView, ByteBuffer pixmapWriteView,
                               BitmapInfoHeader infoHeader, LongUnaryOperator pixelMapper) {
        for (int w = 0; w < infoHeader.width(); w++) {
            long pixel = readNextPixel(pixmapReadView, infoHeader.bitsPerPixel());
            long mappedPixel = pixelMapper.applyAsLong(pixel);
            putPixel(pixmapWriteView, infoHeader.bitsPerPixel(), mappedPixel);
        }
        int remainder = infoHeader.width() * (infoHeader.bitsPerPixel() / Byte.SIZE) % 4;
        if (remainder != 0) {
            for (int i = 0; i < 4 - remainder; i++) {
                pixmapWriteView.put(pixmapReadView.get());
            }
        }
    }

    private long readNextPixel(ByteBuffer pixmap, int bitsPerPixel) {
        long pixel = 0;
        for (int i = 0; i < bitsPerPixel / Byte.SIZE; i++) {
            long currentByte = pixmap.get() & 0xff;
            pixel |= currentByte << (i * Byte.SIZE);
        }
        return pixel;
    }

    private void putPixel(ByteBuffer pixmap, int bitsPerPixel, long pixel) {
        for (int i = 0; i < bitsPerPixel / Byte.SIZE; i++) {
            pixmap.put((byte) ((pixel >>> (i * Byte.SIZE)) & 0xff));
        }
    }

    private static int totalEditableChannelsCount(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        return infoHeader.width() * infoHeader.height() * infoHeader.channelMasks().activeChannelsCount();
    }

}
