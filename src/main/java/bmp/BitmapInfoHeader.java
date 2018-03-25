package bmp;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import static bmp.PixmapType.*;
import static java.lang.Math.abs;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class BitmapInfoHeader {
//    private final PixmapType type;
//    private final int width;
//    private final int height;
//    private final int bitsPerPixel;
//    private final ChannelMasks channelMaks;
//
//    /**
//     * содержит ли заголовок корректные (согласованные) и поддерживаемые значения
//     */
//    private final boolean isSupported;

    public BitmapInfoHeader(@NotNull ByteBuffer infoBytes) {
        infoBytes = infoBytes.asReadOnlyBuffer().order(LITTLE_ENDIAN);
        int size = infoBytes.getInt(0x00);
        Version version = Version.ofSize(size);

    }

    /**
     * Способ хранения растра.
     */
    @NotNull
    public PixmapType type() {
        //todo:: impl!
        return null;
    }

    /**
     * Высота растра в пикселях
     */
    public int width() {
        //todo:: impl!
        return 0;
    }

    /**
     * Ширина растра в пикселях
     */
    public int height() {
        //todo:: impl!
        return 0;
    }

    /**
     * Ширина пикселя в битах
     */
    public int bitsPerPixel() {
        //todo:: impl!
        return 0;
    }

    /**
     * Значения масок каналов для пикселей. Если маски указаны в заголовке, то они будут использованы.
     * В противном случае, будут использованы маски по умолчанию для значения {@link BitmapInfoHeader#bitsPerPixel()}.
     */
    @NotNull
    public ChannelMasks channelMaks() {
        //todo:: impl!
        return null;
    }

    @RequiredArgsConstructor
    private enum Version {
        CORE(12) {
            @Override
            public int extractHeight(ByteBuffer bytes) {
                return bytes.getShort(0x06);
            }

            @Override
            public int extractWidth(ByteBuffer bytes) {
                return bytes.getShort(0x04);
            }

            @Override
            public Optional<Compression> compression(ByteBuffer bytes) {
                return Compression.ofValue(0);
            }
        },
        V3(40) {
            @Override
            public int extractHeight(ByteBuffer bytes) {
                return abs(bytes.getInt(0x08));
            }

            @Override
            public int extractWidth(ByteBuffer bytes) {
                return abs(bytes.getInt(0x04));
            }

            @Override
            public Optional<Compression> compression(ByteBuffer bytes) {
                return Compression.ofValue(bytes.getInt(0x10));
            }
        },
        V4(108) {
            @Override
            public int extractHeight(ByteBuffer bytes) {
                return abs(bytes.getInt(0x08));
            }

            @Override
            public int extractWidth(ByteBuffer bytes) {
                return abs(bytes.getInt(0x04));
            }

            @Override
            public Optional<Compression> compression(ByteBuffer bytes) {
                return Compression.ofValue(bytes.getInt(0x10));
            }
        },
        V5(124) {
            @Override
            public int extractHeight(ByteBuffer bytes) {
                return abs(bytes.getInt(0x08));
            }

            @Override
            public int extractWidth(ByteBuffer bytes) {
                return abs(bytes.getInt(0x04));
            }

            @Override
            public Optional<Compression> compression(ByteBuffer bytes) {
                return Compression.ofValue(bytes.getInt(0x10));
            }
        };

        private final int size;

        @Nullable
        public static Version ofSize(int size) {
            return Arrays.stream(values()).filter(v -> v.size == size).findAny().orElse(null);
        }

        public abstract int extractHeight(ByteBuffer bytes);

        public abstract int extractWidth(ByteBuffer bytes);

        public abstract Optional<Compression> compression(ByteBuffer bytes);

    }

    @RequiredArgsConstructor
    private enum Compression {

        BI_RGB(0, PIXEL_MATRIX),
        BI_RLE8(1, RLE),
        BI_RLE4(2, UNSUPPORTED),
        BI_BITFIELDS(3, PIXEL_MATRIX),
        BI_JPEG(4, UNSUPPORTED),
        BI_PNG(5, UNSUPPORTED),
        BI_ALPHABITFIELDS(6, PIXEL_MATRIX);

        private final int value;
        private final PixmapType pixmapType;

        @Nullable
        public static Optional<Compression> ofValue(int value) {
            return Arrays.stream(values()).filter(v -> v.value == value).findAny();
        }
    }
}
