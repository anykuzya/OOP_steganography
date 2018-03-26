package bmp.pixmap;

import bmp.BitmapInfoHeader;

import java.nio.ByteBuffer;

public interface PixmapTransformer {

    void transform(ByteBuffer pixmap, BitmapInfoHeader infoHeader);
}
