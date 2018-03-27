package bmp;

import bmp.pixmap.ChannelMapperFactory;
import bmp.pixmap.MatrixPixmapTransformer;
import bmp.pixmap.PixmapTransformer;
import bmp.pixmap.RlePixmapTransformer;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
public class BmpTransformer {

    private final ChannelMapperFactory channelMapperFactory;

    public void transform(ReadableByteChannel bmpInput, WritableByteChannel bmpOutput) throws IOException {

        Headers headers = transferUponPixmap(bmpInput, bmpOutput);
        BitmapInfoHeader infoHeader = headers.getInfoHeader();
        PixmapTransformer transformer;
        switch (infoHeader.type()) {
            case RLE:
                transformer = new RlePixmapTransformer(channelMapperFactory);
                break;
            case PIXEL_MATRIX:
                transformer = new MatrixPixmapTransformer(channelMapperFactory);
                break;
            case UNSUPPORTED:
                throw new IllegalArgumentException();
            default:
                throw new IllegalStateException();

        }
        BitmapFileHeader fileHeader = headers.getFileHeader();
        ByteBuffer pixmap = ByteBuffer.allocate(fileHeader.fileSizeBytes() - fileHeader.pixmapOffsetBytes());
        bmpInput.read(pixmap);
        pixmap.flip();
        transformer.transform(pixmap, infoHeader);

        bmpOutput.write(pixmap);
    }

    private Headers transferUponPixmap(ReadableByteChannel bmpInput, WritableByteChannel bmpOutput)
            throws IOException {
        BufferAndHeader<BitmapFileHeader> fileBufferAndHeader = BitmapFileHeader.read(bmpInput);
        BitmapFileHeader fileHeader = fileBufferAndHeader.getHeader();
        BufferAndHeader<BitmapInfoHeader> infoBufferAndHeader = BitmapInfoHeader.read(bmpInput);
        BitmapInfoHeader infoHeader = infoBufferAndHeader.getHeader();

        ByteBuffer fileBuffer = fileBufferAndHeader.getBuffer();
        ByteBuffer infoBuffer = infoBufferAndHeader.getBuffer();

        int gapSize = fileHeader.pixmapOffsetBytes() - infoBuffer.remaining() - fileBuffer.remaining();

        bmpOutput.write(fileBuffer);
        bmpOutput.write(infoBuffer);

        ByteBuffer gapBuffer = ByteBuffer.allocate(gapSize);
        if (bmpInput.read(gapBuffer) < gapSize) {
            throw new IOException();
        }
        gapBuffer.flip();
        bmpOutput.write(gapBuffer);

        return new Headers(fileHeader, infoHeader);
    }

    @Value
    private class Headers {
        public BitmapFileHeader fileHeader;
        public BitmapInfoHeader infoHeader;
    }
}
