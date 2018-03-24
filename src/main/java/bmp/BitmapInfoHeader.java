package bmp;

import org.jetbrains.annotations.NotNull;

public interface BitmapInfoHeader {

    /**
     * Способ хранения растра.
     */
    @NotNull
    PixmapType type();

    /**
     * Высота растра в пикселях
     */
    int width();

    /**
     * Ширина растра в пикселях
     */
    int height();

    /**
     * Ширина пикселя в битах
     */
    int bitsPerPixel();

    /**
     * Значения масок каналов для пикселей. Если маски указаны в заголовке, то они будут использованы.
     * В противном случае, будут использованы маски по умолчанию для значения {@link BitmapInfoHeader#bitsPerPixel()}.
     */
    @NotNull
    ChannelMasks channelMaks();
}
