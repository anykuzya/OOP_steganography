package utils;

import lombok.Value;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class NioUtils {

    public static ChannelAndBuffer newChannelAndBuffer(int size) {
        byte[] data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(LITTLE_ENDIAN);
        ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(data));
        return new ChannelAndBuffer(channel, buffer);
    }

    @Value
    public static class ChannelAndBuffer {
        ReadableByteChannel channel;
        ByteBuffer buffer;
    }

    public static ByteBuffer createPixmap(byte[] commands) {
        ByteBuffer pixmap = ByteBuffer.allocate(commands.length).order(LITTLE_ENDIAN);
        for (byte command : commands) {
            pixmap.put(command);
        }
        pixmap.flip();
        return pixmap;
    }
}
