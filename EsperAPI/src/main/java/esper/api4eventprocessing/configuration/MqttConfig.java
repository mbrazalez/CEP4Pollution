package esper.api4eventprocessing.configuration;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    @Bean
    public IMqttClient mqttClient(@Value("${mqtt.broker.ip}") String brokerIp, @Value("${mqtt.broker.port}") String port) throws Exception {
        String publisherId = "spring-api";
        IMqttClient client = new MqttClient("tcp://" + brokerIp + ":" + port, publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client.connect(options);
        return client;
    }
}
