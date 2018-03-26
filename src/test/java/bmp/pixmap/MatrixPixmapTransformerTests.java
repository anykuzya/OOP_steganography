package bmp.pixmap;

import bmp.BitmapInfoHeader;
import bmp.ChannelMasks;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.function.LongUnaryOperator;

import static bmp.PixmapType.PIXEL_MATRIX;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static utils.NioUtils.createByteBuffer;

public class MatrixPixmapTransformerTests {
    ChannelMasks masks;
    LongUnaryOperator pixelMapper;
    private ChannelMapperFactory channelMapperFactory;


    @Test
    public void shouldCallPixelMapperForEachPixel() {
        when(pixelMapper.applyAsLong(anyLong())).thenReturn(0x010203L, 0x040506L, 0x070809L,
                                                                  0xf1f2f3L, 0xf4f5f6L, 0xf7f8f9L);
        when(masks.pixelMapper(any(LongUnaryOperator.class))).thenReturn(pixelMapper);
        when(masks.activeChannelsCount()).thenReturn(3);
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
        when(this.channelMapperFactory.createChannelMapper(anyInt())).thenReturn(l -> 1);

        new MatrixPixmapTransformer(channelMapperFactory).transform(pixmapBuffer, infoHeader);

        verify(channelMapperFactory).createChannelMapper(18);

        verify(pixelMapper, times(6)).applyAsLong(anyLong());
        byte[] pixmapAfterTransform = new byte[] {
                // первая линия:
                0x03, 0x02, 0x01,
                0x06, 0x05, 0x04,
                0x09, 0x08, 0x07,
                0x00, 0x00, 0x00, // выравнивание после первой линии
                // вторая линия:
                (byte) 0xf3L, (byte) 0xf2L, (byte) 0xf1L,
                (byte) 0xf6L, (byte) 0xf5L, (byte) 0xf4L,
                (byte) 0xf9L, (byte) 0xf8L, (byte) 0xf7L,
                0x00, 0x00, 0x00 // выравнивание второй линии
        };
        ByteBuffer expected = createByteBuffer(pixmapAfterTransform);
        assertEquals(expected, pixmapBuffer);

    }

    @Before
    public void setUp() throws Exception {
        this.masks = mock(ChannelMasks.class);
        this.pixelMapper = mock(LongUnaryOperator.class);
        this.channelMapperFactory = mock(ChannelMapperFactory.class);
    }
}