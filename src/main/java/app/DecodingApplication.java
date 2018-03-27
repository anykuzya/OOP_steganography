package app;

import bmp.BmpReader;
import lombok.RequiredArgsConstructor;
import steganography.EmbeddedDataDecoder;
import steganography.Encoding;
import steganography.TextFromChannelsExtractor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
public class DecodingApplication implements Runnable {

    private final File pathToBmp;

    private final Encoding encoding;

    private final File pathToOutput;

    public void run() {
        try (ReadableByteChannel input = new RandomAccessFile(pathToBmp, "r").getChannel();
             WritableByteChannel output = new RandomAccessFile(pathToOutput, "rw").getChannel()) {
            TextFromChannelsExtractor dataExtractor = new TextFromChannelsExtractor();
            new BmpReader(dataExtractor).read(input);

            ByteBuffer extractedData = dataExtractor.extractedData();
            EmbeddedDataDecoder decoder = new EmbeddedDataDecoder(extractedData);

            ByteBuffer result = encoding.getCharset().encode(decoder.text());
            output.write(result);

        } catch (IOException e) {
            System.err.println("Unexpected IO exception occurred:");
        }
    }
}
