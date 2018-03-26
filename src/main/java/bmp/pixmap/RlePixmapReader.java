package bmp.pixmap;

import bmp.BitmapInfoHeader;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

import static bmp.PixmapType.RLE;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

@RequiredArgsConstructor
public class RlePixmapReader implements PixmapReader {

    private final ChannelConsumer channelConsumer;

    @Override
    public void readPixmap(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        validateHeader(infoHeader);
        ByteBuffer pixmapView = pixmap.asReadOnlyBuffer().order(LITTLE_ENDIAN);
        LongUnaryOperator pixelConsumer = pixelConsumer(infoHeader);

        byte[] command = new byte[2];
        while (pixmapView.remaining() >= 2) {
            pixmapView.get(command);
            if (isEndOfLineCommand(command)) {
                continue;
            } else if (isEndOfPixmapCommand(command)) {
                break;
            } else if (isDeltaCommand(command)) {
                // пропускаем следующие два байта, в которых содержится величина сдвига курсора
                pixmapView.get();
                pixmapView.get();
            } else if (isFillLineCommand(command)) {
                pixelConsumer.applyAsLong(command[1]);
            } else { // команда закраски пикселями, которые идут дальше
                int pixelsCount = command[1];
                for (int i = 0; i < pixelsCount; i++) {
                    pixelConsumer.applyAsLong(pixmapView.get());
                }
                // команды должны быть выравнены по 2 байта, так что если пикселей было нечетное количество,
                // один байт нужно пропустить из-за выравнивания
                if (pixelsCount % 2 == 1) {
                    pixmapView.get();
                }
            }
        }
        channelConsumer.finishConsumption();
    }

    private LongUnaryOperator pixelConsumer(BitmapInfoHeader infoHeader) {
        return infoHeader.channelMasks().pixelMapper(channel -> {
            this.channelConsumer.consumeChannel(channel);
            return channel;
        });
    }

    private static boolean isEndOfLineCommand(byte[] command) {
        return command[0] == 0x00 && command[1] == 0x00;
    }

    private static boolean isEndOfPixmapCommand(byte[] command) {
        return command[0] == 0x00 && command[1] == 0x01;
    }

    private static boolean isDeltaCommand(byte[] command) {
        return command[0] == 0x00 && command[1] == 0x02;
    }

    private static boolean isFillLineCommand(byte[] command) {
        return command[0] > 0;
    }

    private static void validateHeader(BitmapInfoHeader infoHeader) {
        if (infoHeader.bitsPerPixel() != 8 || infoHeader.type() != RLE) {
            throw new UnsupportedOperationException();
        }
    }
}
