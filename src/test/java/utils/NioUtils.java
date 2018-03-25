package utils;

import lombok.Value;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class NioUtils {

    public static ChannelAndBuffer newChannelAndBuffer(int size) {
        byte[] data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
        return new ChannelAndBuffer(channel, buffer);
    }

    @Value
    public static class ChannelAndBuffer {
        ReadableByteChannel channel;
        ByteBuffer buffer;
    }
}
