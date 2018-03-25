package bmp;

import org.junit.Test;
import utils.NioUtils;
import utils.NioUtils.ChannelAndBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.junit.Assert.*;

public class BitmapFileHeaderTests {

    @Test
    public void shouldReadFileHeader() throws IOException {
        ChannelAndBuffer channelAndBuffer = NioUtils.newChannelAndBuffer(14);

        ByteBuffer byteBuffer = channelAndBuffer.getBuffer();
        byteBuffer.putInt(0x02, 1300);
        byteBuffer.putInt(0x0a, 300);


        BufferAndHeader<BitmapFileHeader> parsedFileHeader = BitmapFileHeader.read(channelAndBuffer.getChannel());

        assertEquals(byteBuffer, parsedFileHeader.getBuffer());
        assertEquals(1300, parsedFileHeader.getHeader().fileSizeBytes());
        assertEquals(300, parsedFileHeader.getHeader().pixmapOffsetBytes());
    }



}