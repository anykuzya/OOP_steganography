package bmp;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongUnaryOperator;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
class ChannelMasks {

    /**
     * Данная маска задается только в случаях, когда пиксели кодируются номером строки в таблице цветов.
     * В этом случае, остальные все маски равны нулю.
     */
    private final MaskAndShift colorMapIndex;

    private final MaskAndShift red;

    private final MaskAndShift green;

    private final MaskAndShift blue;

    private final MaskAndShift alpha;

    public long redMask() {
        return this.red.mask;
    }

    public long greenMask() {
        return this.green.mask;
    }

    public long blueMask() {
        return this.blue.mask;
    }

    public long alphaMask() {
        return this.alpha.mask;
    }

    ChannelMasks(long colorMapIndexMask, long redMask, long greenMask, long blueMask, long alphaMask) {
        this.colorMapIndex = MaskAndShift.ofMask(colorMapIndexMask);
        this.red = MaskAndShift.ofMask(redMask);
        this.green = MaskAndShift.ofMask(greenMask);
        this.blue = MaskAndShift.ofMask(blueMask);
        this.alpha = MaskAndShift.ofMask(alphaMask);
        if (channels().noneMatch(MaskAndShift::isActive)) {
            throw new IllegalArgumentException("At least one active channel should be provided");
        }
    }

    /**
     * Построить преобразователя пикселей, который применяет {@code channelMapper} ко всем ненулевым каналам.
     *
     * @apiNote значения каналов в {@code channelMapper} передаются нормализованными, то есть они 'сдвинуты'
     * в начало шкалы. Если в результате преобразования канал 'вылез' за границы своей маски, лишние биты будут обнулены.
     */
    @NotNull
    public LongUnaryOperator pixelMapper(@NotNull LongUnaryOperator channelMapper) {
        return pixel -> channels()
                .filter(MaskAndShift::isActive)
                .mapToLong(mm -> mapChannel(pixel, mm, channelMapper))
                .reduce((x, y) -> x | y)
                .orElseThrow(() -> new IllegalStateException("at least one channel should be active"));
    }

    private Stream<MaskAndShift> channels() {
        return Stream.of(this.colorMapIndex, this.red, this.green, this.blue, this.alpha);
    }

    private static long mapChannel(long pixel, MaskAndShift mm, LongUnaryOperator channelMapper) {
        long normalizedChannelValue = (pixel & mm.mask) >>> mm.shift;
        long mappedValue = channelMapper.applyAsLong(normalizedChannelValue);
        return (mappedValue << mm.shift) & mm.mask;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    @EqualsAndHashCode
    private static class MaskAndShift {
        final long mask;

        final long shift;

        boolean isActive() {
            return mask != 0;
        }

        static MaskAndShift ofMask(long mask) {
            if (mask == 0) {
                return new MaskAndShift(0L, 0L);
            }

            long shift = 0;
            while (((mask >>> shift) & 1) == 0) {
                shift++;
            }
            return new MaskAndShift(mask, shift);
        }
    }
}
