package steganography;

import bmp.pixmap.ChannelMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static steganography.EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS;

//https://lkrnac.net/blog/2014/01/mock-constructor/#mock-constructor
public class TextToChannelsEmbedderFactoryTests {

    @Test
    public void shouldNotWritePartOfOverflowedSymbol() {
        String textToEmbed = "русский!";
        int bitsPerChannel = 2;
        int substringToEmbedLength = 3;
        TextToChannelsEmbedderFactory textToChannelsEmbedderFactory = new TextToChannelsEmbedderFactory(bitsPerChannel, textToEmbed, Encoding.UTF_8);
        TextToChannelsEmbedderFactory textToChannelsEmbedderFactorySpy = Mockito.spy(textToChannelsEmbedderFactory);

        // 8 символов (7 из них по 2 байта), 15 байт должна весить строка. с заголовком -- 20.


        int bitsPerMessage = EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS +
                TextToChannelsEmbedderFactory.HEADER_SIZE_BYTES  * Byte.SIZE +
                // 3 двухбайтные буквы
                2 * Byte.SIZE * substringToEmbedLength;

        ChannelMapper mapper = textToChannelsEmbedderFactorySpy.createChannelMapper((bitsPerMessage/bitsPerChannel) + 5);

        ByteBuffer byteBuffer = ByteBuffer.allocate((bitsPerMessage - EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS) / Byte.SIZE);


        ByteBuffer bufferToEmbed = Encoding.UTF_8.getCharset().encode(textToEmbed.substring(0, substringToEmbedLength));
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(bufferToEmbed.limit()).put(Encoding.UTF_8.getEmbeddedCode()).put(bufferToEmbed);
        byteBuffer.flip();

        verify(textToChannelsEmbedderFactorySpy).constructMapper(bitsPerChannel, byteBuffer);
     }

     @Test
    public void shouldWriteFullString() {
        String textToEmbed = "русский, english";
        int bitsPerChannel = 1;
        TextToChannelsEmbedderFactory textToChannelsEmbedderFactory = new TextToChannelsEmbedderFactory(bitsPerChannel, textToEmbed, Encoding.UTF_8);
        TextToChannelsEmbedderFactory textToChannelsEmbedderFactorySpy = Mockito.spy(textToChannelsEmbedderFactory);

        // 16 символов (7 из них по 2 байта), 23 байта должна весить строка. с заголовком -- 28.


        int bitsPerMessage = EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS +
                TextToChannelsEmbedderFactory.HEADER_SIZE_BYTES  * Byte.SIZE +
                Byte.SIZE * 23;

        ChannelMapper mapper = textToChannelsEmbedderFactorySpy.createChannelMapper((bitsPerMessage/bitsPerChannel) + 5);

        ByteBuffer byteBuffer = ByteBuffer.allocate((bitsPerMessage - EmbeddingChannelMapper.UTILITY_HEADER_LENGTH_BITS) / Byte.SIZE);


        ByteBuffer bufferToEmbed = Encoding.UTF_8.getCharset().encode(textToEmbed);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN).putInt(bufferToEmbed.limit()).put(Encoding.UTF_8.getEmbeddedCode()).put(bufferToEmbed);
        byteBuffer.flip();

        verify(textToChannelsEmbedderFactorySpy).constructMapper(bitsPerChannel, byteBuffer);
     }

}