package bmp.pixmap;

import bmp.BitmapInfoHeader;

import java.nio.ByteBuffer;

public interface PixmapReader {

    void readPixmap(ByteBuffer pixmap, BitmapInfoHeader infoHeader);
}
