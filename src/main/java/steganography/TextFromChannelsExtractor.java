package steganography;

import bmp.pixmap.ChannelConsumer;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class TextFromChannelsExtractor implements ChannelConsumer {

    private int bitsPerChannel;
    private ByteBuffer extractedData;
    private ByteBuffer extractedHeader;
    private boolean isDecodingFinished = false;
    private boolean isHeaderParsed = false;
    private int bitsForCurrentByteExtracted = 0;
    private byte currentByte;
    private long channelMask;

    @Override
    public void consumeChannel(long channel) {
        if (isDecodingFinished) return;
        if (bitsPerChannel == 0) {
            extractUtilityHeader(channel);
            extractedHeader = ByteBuffer.allocate(TextToChannelsEmbedderFactory.HEADER_SIZE_BYTES).order(LITTLE_ENDIAN);
            return;
        }
        //дальше первые 5 байт -- это только заголовок!
        if (extractedHeader.remaining() > 0) {
            extractNext(channel, extractedHeader);
            return;
        }
        if (!isHeaderParsed && extractedHeader.remaining() == 0) {
            extractedHeader.flip();
            extractedData = ByteBuffer.allocate(extractedHeader.getInt(0)
                    + TextToChannelsEmbedderFactory.HEADER_SIZE_BYTES);
            extractedData.put(extractedHeader);
            isHeaderParsed = true;
        }
        //и только потом пойдет реальная инфа
        extractNext(channel, extractedData);

        if (extractedData.remaining() == 0) {
            finishConsumption();
        }
    }

    @Override
    public void finishConsumption() {
        extractedData.position(0);
        isDecodingFinished = true;
    }

    public ByteBuffer extractedData() {
        if (!this.isDecodingFinished) {
            throw new IllegalStateException();
        }
        return this.extractedData.asReadOnlyBuffer();
    }

    private void extractNext(long channel, ByteBuffer bufferTo) {

        long extractedBits = channel & channelMask;
        currentByte |= extractedBits << bitsForCurrentByteExtracted;
        bitsForCurrentByteExtracted += bitsPerChannel;

        if (bitsForCurrentByteExtracted == 8) {
            bufferTo.put(currentByte);
            bitsForCurrentByteExtracted = 0;
            currentByte = 0;
        }
    }

    private void extractUtilityHeader(long channel) {
        long mask = (1 << EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS) - 1;
        bitsPerChannel = 1 << (mask & channel);
        channelMask = (1 << bitsPerChannel) - 1;
    }
}