package bmp.pixmap;

public interface ChannelConsumer {

    void consumeChannel(long channel);

    /**
     * Уведомить о том, что каналы завершились.
     */
    void finishConsumption();
}
