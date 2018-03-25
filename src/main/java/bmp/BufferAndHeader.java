package bmp;

import lombok.Value;

import java.nio.ByteBuffer;

@Value
class BufferAndHeader<T> {

    ByteBuffer buffer;

    T header;
}
