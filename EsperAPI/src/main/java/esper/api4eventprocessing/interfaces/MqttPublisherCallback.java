package esper.api4eventprocessing.interfaces;

@FunctionalInterface
public interface MqttPublisherCallback {
    void publishAsync(byte[] message, String topic) throws Exception;
}
