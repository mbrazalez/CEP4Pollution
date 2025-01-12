import random
import time
from datetime import datetime
from paho.mqtt import client as mqtt_client
import json

broker = '54.173.0.126'
port = 1883
list_of_stations = ["A1", "A2", "A3", "A4", "A5", "A6"]

# Función para crear un evento simulado
def create_event():
    timestamp = int(datetime.now().timestamp())
    print("Timestamp: ", timestamp)    
    station = random.choice(list_of_stations)
    value = random.uniform(0, 200)
    humidityval = random.uniform(90, 100)

    return {
	"pm25topic": {"timestamp": timestamp, "value": value, "station": station},
        "pm10topic": {"timestamp": timestamp, "value": value, "station": station},
        "humiditytopic": {"timestamp": timestamp, "value": humidityval, "station": station},
    }

def connect_mqtt():
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT Broker!")
        else:
            print(f"Failed to connect, return code {rc}\n")

    client = mqtt_client.Client()
    client.on_connect = on_connect
    client.connect(broker, port)
    return client


def publish(client):
    msg_count = 1
    while True:
        time.sleep(2)
        msg = f"messages: {msg_count}"
        event = create_event()
        for key in event:
            client.publish(key, json.dumps(event[key]))

        msg_count += 1
        if msg_count > 50:
            break

def run():
    client = connect_mqtt()
    client.loop_start()
    publish(client)
    client.loop_stop()

if __name__ == '__main__':
    run()
