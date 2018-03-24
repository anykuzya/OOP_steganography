package bmp;

import org.junit.Before;
import org.junit.Test;

import java.util.function.LongUnaryOperator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class ChannelMasksTests {

    private LongUnaryOperator channelMapper;

    @Test
    public void shouldApplyChannelMapperOnlyOnActiveFilters() {
        when(this.channelMapper.applyAsLong(anyLong())).thenReturn(1L, 1L, 1L);

        long fourBitMask = 0b1111;
        ChannelMasks masks = new ChannelMasks(0, fourBitMask, fourBitMask << 4, fourBitMask << 8, 0);
        long mappedPixel = masks.pixelMapper(this.channelMapper).applyAsLong(0b1000_0100_0010);

        assertEquals(0b0001_0001_0001, mappedPixel);

        verify(this.channelMapper, times(3)).applyAsLong(anyLong());
    }

    @Before
    public void setUp() throws Exception {
        this.channelMapper = mock(LongUnaryOperator.class);
    }
}