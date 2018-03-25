package bmp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bmp.PixmapType.*;
import static java.lang.Math.abs;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

@RequiredArgsConstructor
public class BitmapInfoHeader {
    private final PixmapType type;
    private final int width;
    private final int height;
    private final int bitsPerPixel;
    private final ChannelMasks channelMaks;

    /**
     * Способ хранения растра.
     */
    @NotNull
    public PixmapType type() {
        ensureSupported();
        return type;
    }

    /**
     * Высота растра в пикселях
     */
    public int width() {
        ensureSupported();
        return width;
    }

    /**
     * Ширина растра в пикселях
     */
    public int height() {
        ensureSupported();
        return height;
    }

    /**
     * Ширина пикселя в битах
     */
    public int bitsPerPixel() {
        ensureSupported();
        return bitsPerPixel;
    }

    /**
     * Значения масок каналов для пикселей. Если маски указаны в заголовке, то они будут использованы.
     * В противном случае, будут использованы маски по умолчанию для значения {@link BitmapInfoHeader#bitsPerPixel()}.
     */
    @NotNull
    public ChannelMasks channelMasks() {
        return channelMaks;
    }

    private void ensureSupported() {
        if (this.type == UNSUPPORTED) {
            throw new IllegalStateException("Header is unsupported");
        }
    }

    public static BufferAndHeader<BitmapInfoHeader> read(ReadableByteChannel input) throws IOException {
        ByteBuffer infoHeaderSizeBuffer = ByteBuffer.allocate(Integer.BYTES).order(LITTLE_ENDIAN);
        if (input.read(infoHeaderSizeBuffer) < 4) {
            throw new IOException();
        }
        int infoHeaderSize = infoHeaderSizeBuffer.getInt(0);
        ByteBuffer infoHeaderBuffer = ByteBuffer.allocate(infoHeaderSize).order(LITTLE_ENDIAN);
        infoHeaderSizeBuffer.flip();
        infoHeaderBuffer.put(infoHeaderSizeBuffer);
        if (input.read(infoHeaderBuffer) + 4 != infoHeaderSize) {
            throw new IOException();
        }
        infoHeaderBuffer.flip();
        return new BufferAndHeader<>(infoHeaderBuffer.asReadOnlyBuffer(), ofBytes(infoHeaderBuffer));
    }

    private static BitmapInfoHeader ofBytes(@NotNull ByteBuffer infoBytes) {
        infoBytes = infoBytes.asReadOnlyBuffer().order(LITTLE_ENDIAN);
        int size = infoBytes.getInt(0x00);
        Version version = Version.ofSize(size);
        if (version == null) {
            return unsupportedHeader();
        }
        Optional<Compression> compressionOptional = version.extractCompression(infoBytes);
        PixmapType type = compressionOptional.map(Compression::getPixmapType).orElse(UNSUPPORTED);
        if (type == UNSUPPORTED) {
            return unsupportedHeader();
        }
        Compression compression = compressionOptional.get();
        int bitsPerPixel = version.extractbitsPerPixel(infoBytes);
        if (bitsPerPixel < 8 || !compression.isBitCountSuppoted(bitsPerPixel)) {
            return unsupportedHeader();
        }

        return new BitmapInfoHeader(
                type,
                version.extractWidth(infoBytes),
                version.extractHeight(infoBytes),
                bitsPerPixel,
                version.extractChannelMasks(infoBytes, bitsPerPixel)
        );
    }

    @NotNull
    private static BitmapInfoHeader unsupportedHeader() {
        return new BitmapInfoHeader(UNSUPPORTED, 0, 0, 0, null);
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
            public int extractbitsPerPixel(ByteBuffer bytes) {
                return abs(bytes.getShort(0x0a));
            }

            @Override
            public Optional<Compression> extractCompression(ByteBuffer bytes) {
                return Compression.ofValue(0);
            }

            @Override
            public ChannelMasks extractChannelMasks(ByteBuffer bytes, int bitsPerPixel) {
                if (isClrUsed(bytes, bitsPerPixel)) {
                    return new ChannelMasks((1 << bitsPerPixel) - 1,
                            0,
                            0,
                            0,
                            0);
                }
                return defaultMask(bitsPerPixel);
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
            public int extractbitsPerPixel(ByteBuffer bytes) {
                return abs(bytes.getShort(0x0e));
            }

            @Override
            public Optional<Compression> extractCompression(ByteBuffer bytes) {
                return Compression.ofValue(bytes.getInt(0x10));
            }

            @Override
            public ChannelMasks extractChannelMasks(ByteBuffer bytes, int bitsPerPixel) {
                if (isClrUsed(bytes, bitsPerPixel)) {
                    return new ChannelMasks((1 << bitsPerPixel) - 1,
                            0,
                            0,
                            0,
                            0);
                }
                return defaultMask(bitsPerPixel);
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
            public int extractbitsPerPixel(ByteBuffer bytes) {
                return abs(bytes.getShort(0x0e));
            }

            @Override
            public Optional<Compression> extractCompression(ByteBuffer bytes) {
                return Compression.ofValue(bytes.getInt(0x10));
            }
            @Override
            public ChannelMasks extractChannelMasks(ByteBuffer bytes, int bitsPerPixel) {
                if (isClrUsed(bytes, bitsPerPixel)) {
                    return new ChannelMasks((1 << bitsPerPixel) - 1,
                            0,
                            0,
                            0,
                            0);
                }
                return extractMaskFromBytes(bytes, bitsPerPixel);
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
            public int extractbitsPerPixel(ByteBuffer bytes) {
                return abs(bytes.getShort(0x0e));
            }

            @Override
            public Optional<Compression> extractCompression(ByteBuffer bytes) {
                return Compression.ofValue(bytes.getInt(0x10));
            }
            @Override
            public ChannelMasks extractChannelMasks(ByteBuffer bytes, int bitsPerPixel) {
                if (isClrUsed(bytes, bitsPerPixel)) {
                    return new ChannelMasks((1 << bitsPerPixel) - 1,
                            0,
                            0,
                            0,
                            0);
                }
                return extractMaskFromBytes(bytes, bitsPerPixel);
            }
        };

        private final int size;

        @Nullable
        public static Version ofSize(int size) {
            return Arrays.stream(values()).filter(v -> v.size == size).findAny().orElse(null);
        }

        public abstract int extractHeight(ByteBuffer bytes);

        public abstract int extractWidth(ByteBuffer bytes);

        public abstract int extractbitsPerPixel(ByteBuffer bytes);

        public abstract Optional<Compression> extractCompression(ByteBuffer bytes);

        public abstract ChannelMasks extractChannelMasks(ByteBuffer bytes, int bitsPerPixel);

        private static ChannelMasks extractMaskFromBytes(ByteBuffer bytes, int bitsPerPixel) {
            int red = bytes.getInt(0x28);
            int green = bytes.getInt(0x2c);
            int blue = bytes.getInt(0x30);
            int alpha = bytes.getInt(0x34);
            if (red == 0 && green == 0 && blue == 0 && alpha == 0) {
                return defaultMask(bitsPerPixel);
            }
            return new ChannelMasks(0,
                    red,
                    green,
                    blue,
                    alpha);
        }
        public boolean isClrUsed(ByteBuffer bytes, int bitsPerPixel) {
            return (bitsPerPixel <= 8) || (this != CORE && bytes.getInt(0x20) > 0);
        }
        private static ChannelMasks defaultMask(int bitsPerPixel) {
            switch (bitsPerPixel) {
                case 16:
                    return new ChannelMasks(0,
                            0x7c00,
                            0x03e0,
                            0x001f,
                            0);
                case 32:
                    return new ChannelMasks(0,
                            0x00ff_0000L,
                            0x0000_ff00L,
                            0x0000_00ffL,
                            0x0000_0000L);
                case 24:
                    return new ChannelMasks(0,
                            0x00ff_0000L,
                            0x0000_ff00L,
                            0x0000_00ffL,
                            0);
                case 48:
                    return new ChannelMasks(0,
                            0xffff_0000_0000L,
                            0x0000_ffff_0000L,
                            0x0000_0000_ffffL,
                            0x0000_0000_0000L);
                case 64:
                    return new ChannelMasks(0,
                            0xffff_0000_0000_0000L,
                            0x0000_ffff_0000_0000L,
                            0x0000_0000_ffff_0000L,
                            0x0000_0000_0000_0000L);
                default:
                    return null;
            }

        }
    }

    @RequiredArgsConstructor
    private enum Compression {

        BI_RGB(0, PIXEL_MATRIX, Collections.emptySet()),
        BI_RLE8(1, RLE, Collections.singleton(8)),
        BI_RLE4(2, UNSUPPORTED, Collections.singleton(4)),
        BI_BITFIELDS(3, PIXEL_MATRIX, Stream.of(16, 32).collect(Collectors.toSet())),
        BI_JPEG(4, UNSUPPORTED, Collections.singleton(0)),
        BI_PNG(5, UNSUPPORTED, Collections.singleton(0)),
        BI_ALPHABITFIELDS(6, PIXEL_MATRIX, Stream.of(16, 32).collect(Collectors.toSet()));

        private final int value;
        @Getter
        private final PixmapType pixmapType;
        private final Set<Integer> bitCountRestrictions;

        @Nullable
        public static Optional<Compression> ofValue(int value) {
            return Arrays.stream(values()).filter(v -> v.value == value).findAny();
        }
        public boolean isBitCountSuppoted(int bitCount) {
            return bitCountRestrictions.isEmpty() || bitCountRestrictions.contains(bitCount);
        }

    }
}
