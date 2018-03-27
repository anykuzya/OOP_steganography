package steganography;

import bmp.pixmap.ChannelMapper;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

/**
 * В 2 младших бита самого первого канала встраивается степень двойки, которой является bitsPerChannel
 * (т.е, если указано 0, изменению подлежит 1 бит из канала,
 * 1 -- 2 бита, 2 -- 4 и 3 -- 8
 * (последние два ваарианта будут изменять картинку заметно, если это не какая-то большая битность)
 * <p>
 * Эта информация затем будет использована для декодирования.
 * Далее в последующие каналы встраиваются биты из {@link EmbeddingChannelMapper#bytesToEmbed}.
 * bitsPerChannel является СТЕПЕНЬЮ ДВОЙКИ
 */
@RequiredArgsConstructor
public class EmbeddingChannelMapper implements ChannelMapper {

    static final int UTILITY_HEADER_LENGTH_BITS = 2;
    /**
     * supposed to be 1, 2 or 4, 8
     */
    private final int bitsPerChannel;
    private final ByteBuffer bytesToEmbed;

    private int bitsAlreadyEmbedded = 0;
    private byte currentByte;

    private boolean utilityHeaderEmbedded;
    private boolean allAlreadyEmbedded = false;

    //каждый раз ты берешь из буфера байт. запоминаешь, сколько из него вшито.
    @Override
    public long mapChannel(long channel) {
        if (!utilityHeaderEmbedded) {
            return embedUtilityHeader(channel);
        }
        if (allAlreadyEmbedded) {
            return channel;
        } else {
            return embedNext(channel);
        }
    }

    private long embedNext(long channel) {
        long mask = (long) -1 << bitsPerChannel;
        byte embeddingBits;
        if (bitsAlreadyEmbedded == 8) {
            bitsAlreadyEmbedded = 0;
            currentByte = bytesToEmbed.get();
        }
        embeddingBits = (byte) (((1 << bitsPerChannel) - 1) & currentByte);
        currentByte = (byte) (currentByte >>> bitsPerChannel);
        bitsAlreadyEmbedded += bitsPerChannel;
        allAlreadyEmbedded = bytesToEmbed.remaining() == 0 && bitsAlreadyEmbedded == 8;
        return (channel & mask) | embeddingBits;
    }

    private long embedUtilityHeader(long channel) {
        long mask = (long) -1 << UTILITY_HEADER_LENGTH_BITS;
        utilityHeaderEmbedded = true;
        currentByte = bytesToEmbed.get();
        return (channel & mask) | log2(bitsPerChannel);
    }

    private int log2(int value) {
        int power = 0;
        while (value >>> power > 1) {
            power++;
        }
        return power;
    }
}
