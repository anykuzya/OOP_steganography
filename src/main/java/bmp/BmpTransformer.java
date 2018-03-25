package bmp;

import bmp.pixmap.ChannelMapperFactory;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
public class BmpTransformer {

    private final ChannelMapperFactory channelMaperFactory;

    public void transform(ReadableByteChannel bmpInput, WritableByteChannel bmpOutput) throws IOException{

        BitmapInfoHeader infoHeader = transferUponPixmap(bmpInput, bmpOutput);
        // 1. прочитать pixmap
        // 2. в зависимости от infoHeader.type() создать нужный PixmapTransformer и произвести трансформацию
        // 3. записать получившийся результат в bmpOutput
    }

    private BitmapInfoHeader transferUponPixmap(ReadableByteChannel bmpInput, WritableByteChannel bmpOutput)
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

        return infoHeader;
    }
}
