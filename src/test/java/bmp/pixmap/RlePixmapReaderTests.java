package bmp.pixmap;

import bmp.BitmapInfoHeader;
import bmp.ChannelMasks;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static bmp.PixmapType.RLE;
import static org.mockito.Mockito.*;
import static utils.NioUtils.createByteBuffer;
import static org.junit.Assert.assertEquals;

public class RlePixmapReaderTests {

    private ChannelConsumer channelConsumer;

    @Test
    public void shouldGiveAllPixelValuesToConsumerUsingProvidedChannelMasksAndCallFinish() {
        byte[] commands = new byte[] {
                0x03, 0x04, // закрасить 3 пикселя цветом 0x04
                0x05, 0x0a, // закрасить 5 пикселей цветом 0x0a
                0x00, 0x03, 0x45, 0x56, 0x67, 0x00, // следующие 3 пикселя закрасить цветами 0x45, 0x56, 0x67.
                0x02, 0x78, // закрасить 2 пикселя цветом 0x78
                0x00, 0x02, 0x05, 0x01, // переместить курсор на 5 пикселей вправо и 1 пиксель вниз
                0x02, 0x79, // закрасить 2 пикселя цветом 0x79
                0x00, 0x00, // перейти на следующую строку
                0x09, 0x1E, // закрасить следующие 9 пикселей цветом 0x1E
                0x00, 0x01, // завершить
        };
        ByteBuffer pixmap = createByteBuffer(commands);
        ChannelMasks channelMasks = new ChannelMasks(0x0f, 0, 0, 0, 0);
        BitmapInfoHeader infoHeader = new BitmapInfoHeader(RLE, 13, 3, 8, channelMasks);
        new RlePixmapReader(this.channelConsumer).readPixmap(pixmap, infoHeader);

        verify(this.channelConsumer).consumeChannel(0x04);
        verify(this.channelConsumer).consumeChannel(0x0a);
        verify(this.channelConsumer).consumeChannel(0x05);
        verify(this.channelConsumer).consumeChannel(0x06);
        verify(this.channelConsumer).consumeChannel(0x07);
        verify(this.channelConsumer).consumeChannel(0x08);
        verify(this.channelConsumer).consumeChannel(0x09);
        verify(this.channelConsumer).consumeChannel(0x0E);
        verify(this.channelConsumer).finishConsumption();
        verifyNoMoreInteractions(this.channelConsumer);

        assertEquals(createByteBuffer(commands), pixmap);
    }

    @Before
    public void setUp() throws Exception {
        this.channelConsumer = mock(ChannelConsumer.class);
    }
}