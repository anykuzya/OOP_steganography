package bmp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.LongUnaryOperator;

@RequiredArgsConstructor
public class BmpTransformer {

    private final LongUnaryOperator pixelMapper;

    public void transform(ReadableByteChannel bmpInput, WritableByteChannel bmpOutput) throws IOException{

        BitmapInfoHeader infoHeader = transferUponPixmap(bmpInput, bmpOutput);

        // TODO: implement
        // а потом подумаю
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
