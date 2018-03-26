package bmp.pixmap;

import bmp.BitmapInfoHeader;
import bmp.ChannelMasks;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

import static bmp.PixmapType.PIXEL_MATRIX;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MatrixPixmapReaderTests {
    ChannelMasks masks;
    LongUnaryOperator pixelMapper;
    ChannelConsumer channelConsumer;
    @Test
    public void shouldConsumeAllPixels() {
        when(masks.pixelMapper(any(LongUnaryOperator.class))).thenReturn(pixelMapper);

        byte[] pixmap = new byte[] {
                // первая линия:
                (byte) 0xff, (byte) 0xaf, 0x15,
                0x22, 0x42, (byte) 0xaf,
                0x10, 0x25, 0x19,
                0x00, 0x00, 0x00, // выравнивание после первой линии
                // вторая линия:
                0x05, 0x06, 0x07,
                0x12, 0x13, 0x14,
                0x21, 0x31, 0x41,
                0x00, 0x00, 0x00 // выравнивание второй линии
        };
        ByteBuffer pixmapBuffer = ByteBuffer.wrap(pixmap);

        BitmapInfoHeader infoHeader = new BitmapInfoHeader(PIXEL_MATRIX, 3, 2, 24, masks);
        new MatrixPixmapReader(channelConsumer).readPixmap(pixmapBuffer, infoHeader);

        verify(pixelMapper).applyAsLong(0x15_af_ff);
        verify(pixelMapper).applyAsLong(0xaf_42_22);
        verify(pixelMapper).applyAsLong(0x19_25_10);
        verify(pixelMapper).applyAsLong(0x07_06_05);
        verify(pixelMapper).applyAsLong(0x14_13_12);
        verify(pixelMapper).applyAsLong(0x41_31_21);
        verifyNoMoreInteractions(pixelMapper);

        verify(channelConsumer).finishConsumption();
    }

    @Before
    public void setUp() throws Exception {
        this.masks = mock(ChannelMasks.class);
        this.pixelMapper = mock(LongUnaryOperator.class);
        this.channelConsumer = mock(ChannelConsumer.class);

    }
}