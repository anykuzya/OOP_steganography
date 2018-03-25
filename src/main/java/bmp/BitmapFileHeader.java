package bmp;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Структура, представляющая доступ к содержимому заголовка BITMAPFILEHEADER.
 */
class BitmapFileHeader {
    private static final int SIZE_BYTES = 14;

    private final ByteBuffer headerBytes;

    private BitmapFileHeader(@NotNull ByteBuffer headerBytes) {
        this.headerBytes = headerBytes.asReadOnlyBuffer();
        this.headerBytes.order(LITTLE_ENDIAN);
    }

    int fileSizeBytes() {
        return headerBytes.getInt(0x02);
    }

    int pixmapOffsetBytes() {
        return headerBytes.getInt(0x0a);
    }

    public static BufferAndHeader<BitmapFileHeader> read(ReadableByteChannel input) throws IOException {
        ByteBuffer fileHeaderBuffer = ByteBuffer.allocate(SIZE_BYTES);
        if (input.read(fileHeaderBuffer) != SIZE_BYTES) {
            throw new IOException();
        }
        fileHeaderBuffer.flip();
        return new BufferAndHeader<>(fileHeaderBuffer.asReadOnlyBuffer(), new BitmapFileHeader(fileHeaderBuffer));
    }
}
