package bmp.pixmap;

import bmp.BitmapInfoHeader;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
@RequiredArgsConstructor
public class MatrixPixmapReader implements PixmapReader {

    private final ChannelConsumer channelConsumer;

    @Override
    public void readPixmap(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        // TODO: implement
    }
}
