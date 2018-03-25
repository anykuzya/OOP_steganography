package bmp.pixmap;

import bmp.BitmapInfoHeader;

import java.nio.ByteBuffer;

public interface PixmapTranformer {

    void tranform(ByteBuffer pixmap, BitmapInfoHeader infoHeader);
}
