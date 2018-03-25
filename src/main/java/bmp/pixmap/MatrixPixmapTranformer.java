package bmp.pixmap;

import bmp.BitmapInfoHeader;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class MatrixPixmapTranformer implements PixmapTranformer {

    private final ChannelMapperFactory channelMapperFactory;

    @Override
    public void tranform(ByteBuffer pixmap, BitmapInfoHeader infoHeader) {
        // TODO: implement
        // 1. посчитать, сколько у нас в картинке есть каналов и создать
        //    ChannelMapper channelMapper = this.channelMapperFactory.createChannelMapper(channelsCount)
        // 2. преобразовать пиксели, используя для каждого из
        // infoHeader.channelMasks().pixelMapper(channelMapper::mapChannel)
    }
}
