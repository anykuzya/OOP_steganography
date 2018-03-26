package steganography;

import bmp.pixmap.ChannelMapper;
import bmp.pixmap.ChannelMapperFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.ElementType;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.PrimitiveIterator;
import java.util.Spliterator;

import static java.lang.annotation.ElementType.*;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Формат кодирования информации (числа заголовка нужно хранить в little endian):
 *   1. размер закодированного текста в байтах -- 4 байта
 *   2. кодировка -- 1 байт.
 *          * 0 -- ASCII-US
 *          * 1 -- UTF-8
 *          * 2 -- UTF-16
 *   3. Сам текст
 */
@RequiredArgsConstructor
public class TextToChannelsEmbedderFactory implements ChannelMapperFactory {

    static final int HEADER_SIZE_BYTES = 5;
    /**
     * supposed to be power of 2 (1, 2, 4 or 8)
     */
    private final int bitsPerChannel;
    private final String textToEmbed;
    private final Encoding encoding;

    @Override
    public ChannelMapper createChannelMapper(int availableChannels) {
        ByteBuffer embeddedData = embeddedData(availableChannels * this.bitsPerChannel);
        return constructMapper(bitsPerChannel, embeddedData);
    }
    EmbeddingChannelMapper constructMapper(int bitsPerChannel, ByteBuffer embeddedData) {
        return new EmbeddingChannelMapper(bitsPerChannel, embeddedData);
    }
    private ByteBuffer embeddedData(int bitCount) {
        int bitsToEmbed = bitCount - EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS;
        int bytesToEmbed = bitsToEmbed / Byte.SIZE;
        int textBytesToEmbed = bytesToEmbed - HEADER_SIZE_BYTES;
        if (textBytesToEmbed < 0) {
            throw new RuntimeException("нечего вставить");
        }

        ByteBuffer embeddedData = ByteBuffer.allocate(bytesToEmbed);
        embeddedData.position(HEADER_SIZE_BYTES); // размер закодированного текста и кодировку укажем в самом конце
        encodeText(embeddedData);
        embeddedData.flip();

        ByteBuffer embeddedDataSlice = embeddedData.slice().order(LITTLE_ENDIAN);
        int embeddedTextSize = embeddedDataSlice.remaining() - HEADER_SIZE_BYTES;
        embeddedDataSlice.putInt(0, embeddedTextSize);
        embeddedDataSlice.put(4, this.encoding.getEmbeddedCode());
        return embeddedDataSlice;
    }

    private void encodeText(ByteBuffer outputBuffer) {
        CharsetEncoder encoder = this.encoding.getCharset().newEncoder();

        for (PrimitiveIterator.OfInt it = textToEmbed.codePoints().iterator(); it.hasNext(); ) {
            int codePoint = it.nextInt();
            String codePointStr = new String(new int[]{codePoint}, 0, 1);
            CharBuffer codePointBuffer = CharBuffer.wrap(codePointStr);
            CoderResult status = encoder.encode(codePointBuffer, outputBuffer, true);
            if (status.isOverflow()) {
                break;
            }
            if (status.isError()) {
                throw new IllegalStateException();
            }
        }
    }
}
