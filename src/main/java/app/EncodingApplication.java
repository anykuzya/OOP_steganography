package app;

import bmp.BmpTransformer;
import lombok.RequiredArgsConstructor;
import steganography.Encoding;
import steganography.TextToChannelsEmbedderFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
public class EncodingApplication implements Runnable {

    private final File pathToBmp;

    private final String textToEmbed;

    private final Encoding encoding;

    private final int bitsPerPixel;

    private final File outputFile;

    public void run() {
        try (ReadableByteChannel inputChannel = new RandomAccessFile(this.pathToBmp, "r").getChannel();
             WritableByteChannel outputChannel = new RandomAccessFile(this.outputFile, "rw").getChannel()) {
            TextToChannelsEmbedderFactory embedder =
                    new TextToChannelsEmbedderFactory(this.bitsPerPixel, this.textToEmbed, this.encoding);
            new BmpTransformer(embedder).transform(inputChannel, outputChannel);
        } catch (IOException e) {
            System.err.println("Unexpected IO exception occurred:");
        }
    }
}

