package bmp;

import bmp.pixmap.ChannelConsumer;
import lombok.RequiredArgsConstructor;

import java.nio.channels.ReadableByteChannel;

@RequiredArgsConstructor
public class BmpReader {

    private final ChannelConsumer channelConsumer;

    public void read(ReadableByteChannel bmpInput) {
        // TODO implement
    }
}
