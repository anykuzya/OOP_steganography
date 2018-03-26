package steganography;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Getter
public enum Encoding {
    US_ASCII(StandardCharsets.US_ASCII, (byte) 0),
    UTF_8(StandardCharsets.UTF_8, (byte) 1),
    UTF_16(StandardCharsets.UTF_16, (byte) 2);

    private final Charset charset;
    private final byte embeddedCode;
}
