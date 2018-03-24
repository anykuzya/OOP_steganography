package bmp;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongUnaryOperator;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

public class ChannelMasks {

    /**
     * Данная маска задается только в случаях, когда пиксели кодируются номером строки в таблице цветов.
     * В этом случае, остальные все маски равны нулю.
     */
    private final MaskAndMultiplier colorMapIndex;

    private final MaskAndMultiplier red;

    private final MaskAndMultiplier green;

    private final MaskAndMultiplier blue;

    private final MaskAndMultiplier alpha;

    public ChannelMasks(long colorMapIndexMask, long redMask, long greenMask, long blueMask, long alphaMask) {
        this.colorMapIndex = MaskAndMultiplier.ofMask(colorMapIndexMask);
        this.red = MaskAndMultiplier.ofMask(redMask);
        this.green = MaskAndMultiplier.ofMask(greenMask);
        this.blue = MaskAndMultiplier.ofMask(blueMask);
        this.alpha = MaskAndMultiplier.ofMask(alphaMask);
        if (channels().noneMatch(MaskAndMultiplier::isActive)) {
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
                .filter(MaskAndMultiplier::isActive)
                .mapToLong(mm -> mapChannel(pixel, mm, channelMapper))
                .reduce((x, y) -> x | y)
                .orElseThrow(() -> new IllegalStateException("at least one channel should be active"));
    }

    private Stream<MaskAndMultiplier> channels() {
        return Stream.of(this.colorMapIndex, this.red, this.green, this.blue, this.alpha);
    }

    private static long mapChannel(long pixel, MaskAndMultiplier mm, LongUnaryOperator channelMapper) {
        long normalizedChannelValue = (pixel & mm.mask) / mm.multiplier;
        long mappedValue = channelMapper.applyAsLong(normalizedChannelValue);
        return (mappedValue * mm.multiplier) & mm.mask;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static class MaskAndMultiplier {
        final long mask;

        final long multiplier;

        boolean isActive() {
            return mask != 0 && multiplier != 0;
        }

        static MaskAndMultiplier ofMask(long mask) {
            if (mask == 0) {
                return new MaskAndMultiplier(0L, 0L);
            }

            long multiplier = 1;
            while (mask / multiplier % 2 == 0) {
                multiplier *= 2;
            }
            return new MaskAndMultiplier(mask, multiplier);
        }
    }
}
