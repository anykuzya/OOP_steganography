package bmp.pixmap;

public interface ChannelMapperFactory {

    /**
     * Создать {@link ChannelMapper}, который преобразует ровно {@code availableChannels} каналов.
     */
    ChannelMapper createChannelMapper(int availableChannels);
}
