package bmp;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Структура, представляющая доступ к содержимому заголовка BITMAPFILEHEADER.
 */
public class BitmapFileHeader {

    private final ByteBuffer headerBytes;

    public BitmapFileHeader(@NotNull ByteBuffer headerBytes) {
        // TODO: добавить проверку, что в хедере ровно 14 байт и что он начинается с корректного идентификатора формта
        this.headerBytes = headerBytes.asReadOnlyBuffer();
        this.headerBytes.order(LITTLE_ENDIAN);
    }

    int fileSizeBytes() {
        return headerBytes.getInt(0x02);
    }

    int pixmapOffsetBytes() {
        return headerBytes.getInt(0x0a);
    }
}
