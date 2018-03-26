package steganography;

import bmp.pixmap.ChannelMapper;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

/**
 * В 2 младших бита самого первого канала встраивается bitsPerChannel. Эта информация затем будет использована для
 * декодирования. Далее в последующие каналы встраиваются биты из {@link EmbeddingChannelMapper#bytesToEmbed}.
 */
@RequiredArgsConstructor
public class EmbeddingChannelMapper implements ChannelMapper {

    static final int UTILITY_HEADER_LENGTH_BITS = 2;

    private final int bitsPerChannel;
    private final ByteBuffer bytesToEmbed;

    private int bitsAlreadyEmbedded = 0;

    @Override
    public long mapChannel(long channel) {
        return 0;
    }
}
