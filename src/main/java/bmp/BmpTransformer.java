package bmp;

import lombok.RequiredArgsConstructor;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.LongUnaryOperator;

@RequiredArgsConstructor
public class BmpTransformer {

    private final LongUnaryOperator pixelMapper;

    public void transform(ReadableByteChannel bmpInput, WritableByteChannel bmpOutput) {
        // TODO: implement
        // 1. прочитать два хедера и записать их сразу в output
        // 2. дочитать до начала pixmap и эту информацию тоже записать в output
        // а потом подумаю
    }
}
