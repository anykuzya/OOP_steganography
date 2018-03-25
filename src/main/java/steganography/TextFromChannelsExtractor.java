package steganography;

import bmp.pixmap.ChannelConsumer;

import java.nio.ByteBuffer;

public class TextFromChannelsExtractor implements ChannelConsumer {

    private boolean isDecodingFinished;

    private int bitsPerChannel;
    private ByteBuffer extractedData;

    @Override
    public void consumeChannel(long channel) {
        // TODO: implement
    }

    @Override
    public void finishConsumption() {
        // TODO: implement
    }

    public ByteBuffer extractedData() {
        if (!this.isDecodingFinished) {
            throw new IllegalStateException();
        }
        return this.extractedData.asReadOnlyBuffer();
    }
}
