package bmp;

import bmp.pixmap.*;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
public class BmpReader {

    private final ChannelConsumer channelConsumer;

    public void read(ReadableByteChannel bmpInput) throws IOException {
        Headers headers = readHeaders(bmpInput);

        BitmapInfoHeader infoHeader = headers.getInfoHeader();
        PixmapReader reader;
        switch (infoHeader.type()) {
            case RLE:
                reader = new RlePixmapReader(channelConsumer);
                break;
            case PIXEL_MATRIX:
                reader = new MatrixPixmapReader(channelConsumer);
                break;
            case UNSUPPORTED:
                throw new IllegalArgumentException();
            default:
                throw new IllegalStateException();

        }
        BitmapFileHeader fileHeader = headers.getFileHeader();
        ByteBuffer pixmap = ByteBuffer.allocate(fileHeader.fileSizeBytes() - fileHeader.pixmapOffsetBytes());
        bmpInput.read(pixmap);

        reader.readPixmap(pixmap, infoHeader);

    }

    private Headers readHeaders(ReadableByteChannel bmpInput)
            throws IOException {
        BufferAndHeader<BitmapFileHeader> fileBufferAndHeader = BitmapFileHeader.read(bmpInput);
        BitmapFileHeader fileHeader = fileBufferAndHeader.getHeader();
        BufferAndHeader<BitmapInfoHeader> infoBufferAndHeader = BitmapInfoHeader.read(bmpInput);
        BitmapInfoHeader infoHeader = infoBufferAndHeader.getHeader();

        ByteBuffer fileBuffer = fileBufferAndHeader.getBuffer();
        ByteBuffer infoBuffer = infoBufferAndHeader.getBuffer();

        int gapSize = fileHeader.pixmapOffsetBytes() - infoBuffer.remaining() - fileBuffer.remaining();


        ByteBuffer gapBuffer = ByteBuffer.allocate(gapSize);
        if (bmpInput.read(gapBuffer) < gapSize) {
            throw new IOException();
        }

        return new Headers(fileHeader, infoHeader);
    }

    @Value
    private class Headers {
        public BitmapFileHeader fileHeader;
        public BitmapInfoHeader infoHeader;
    }
}
