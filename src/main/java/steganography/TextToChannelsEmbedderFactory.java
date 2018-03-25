package steganography;

import bmp.pixmap.ChannelMapper;
import bmp.pixmap.ChannelMapperFactory;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;

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

    private final int bitsPerChannel;
    private final String textToEmbed;
    private final Charset charset;

    @Override
    public ChannelMapper createChannelMapper(int availableChannels) {
        // TODO: implement
        // 1. вычислить, сколько байт текста мы сможем целиком уместить в такое количество каналов
        // 2. вычислить byteBuffer, в котором будет храниться заголовок (размер текста, кодировка) и текст
        // 3. вернуть new EmbeddingChannelMapper от этого буффера и заданного bitsPerPixel
        return null;
    }
}
