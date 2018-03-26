package bmp.pixmap;

import bmp.BitmapInfoHeader;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

@RequiredArgsConstructor
public class RlePixmapTransformer implements PixmapTransformer {

    private final ChannelMapperFactory channelMapperFactory;

    @Override
    public void transform(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        int channelCount = totalEditableChannelsCount(pixmap, infoHeader);
        ChannelMapper channelMapper = this.channelMapperFactory.createChannelMapper(channelCount);
        LongUnaryOperator pixelMapper = infoHeader.channelMasks().pixelMapper(channelMapper::mapChannel);

        ByteBuffer pixmapReadView = pixmap.duplicate().order(LITTLE_ENDIAN);
        ByteBuffer pixmapWriteView = pixmap.duplicate().order(LITTLE_ENDIAN);
        byte[] command = new byte[2];
        while (pixmapReadView.remaining() >= 2) {
            pixmapReadView.get(command);
            if (isEndOfLineCommand(command)) {
                pixmapWriteView.put(command);
            } else if (isEndOfPixmapCommand(command)) {
                pixmapWriteView.put(command);
                break;
            } else if (isDeltaCommand(command)) {
                pixmapWriteView.put(command);
                // пропускаем следующие два байта, в которых содержится величина сдвига курсора
                pixmapReadView.get(command);
                pixmapWriteView.put(command);
            } else if (isFillLineCommand(command)) {
                pixmapWriteView.put(command[0]);
                byte mappedPixel = (byte) pixelMapper.applyAsLong(command[1]);
                pixmapWriteView.put(mappedPixel);
            } else { // команда закраски пикселями, которые идут дальше
                pixmapWriteView.put(command);
                int pixelsCount = command[1];
                for (int i = 0; i < pixelsCount; i++) {
                    byte mappedPixel = (byte) pixelMapper.applyAsLong(pixmapReadView.get());
                    pixmapWriteView.put(mappedPixel);
                }
                // команды должны быть выравнены по 2 байта, так что если пикселей было нечетное количество,
                // один байт нужно пропустить из-за выравнивания
                if (pixelsCount % 2 == 1) {
                    pixmapWriteView.put(pixmapReadView.get());
                }
            }
        }
    }

    private static boolean isEndOfLineCommand(byte[] commandBuffer) {
        return commandBuffer[0] == 0x00 && commandBuffer[1] == 0x00;
    }

    private static boolean isEndOfPixmapCommand(byte[] commandBuffer) {
        return commandBuffer[0] == 0x00 && commandBuffer[1] == 0x01;
    }

    private static boolean isDeltaCommand(byte[] commandBuffer) {
        return commandBuffer[0] == 0x00 && commandBuffer[1] == 0x02;
    }

    private static boolean isFillLineCommand(byte[] commandBuffer) {
        return commandBuffer[0] > 0;
    }

    private static int totalEditableChannelsCount(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        CountingChannelConsumer channelCounter = new CountingChannelConsumer();
        new RlePixmapReader(channelCounter).readPixmap(pixmap, infoHeader);
        return channelCounter.count;
    }

    private static class CountingChannelConsumer implements ChannelConsumer {
        int count;

        @Override
        public void consumeChannel(long channel) {
            count++;
        }

        @Override
        public void finishConsumption() {
            // nop
        }
    }
}
