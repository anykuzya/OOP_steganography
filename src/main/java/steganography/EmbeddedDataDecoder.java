package steganography;

import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class EmbeddedDataDecoder {

    private final ByteBuffer embeddedData;

    public String text() {
        ByteBuffer dataView = embeddedData.asReadOnlyBuffer();
        dataView.position(TextToChannelsEmbedderFactory.HEADER_SIZE_BYTES);

        int textSize = embeddedData.remaining() - TextToChannelsEmbedderFactory.HEADER_SIZE_BYTES;
        byte[] textBytes = new byte[textSize];
        dataView.get(textBytes);
        return new String(textBytes, encoding().getCharset());
    }

    private Encoding encoding() {
        byte encodingCode = embeddedData.get(4);
        return Encoding.byCode(encodingCode).orElseThrow(IllegalArgumentException::new);
    }

}
