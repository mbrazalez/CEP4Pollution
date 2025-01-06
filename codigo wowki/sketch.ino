#include <WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

// Incluimos las librerías de FreeRTOS para manejo de tareas, colas y semáforos
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "freertos/semphr.h"

// Configuración de MQTT
#define MQTT_CLIENT_ID "wokwi_sensor"
#define MQTT_BROKER "test.mosquitto.org"
#define MQTT_PORT 1883
#define MQTT_TOPIC "/smartcities/airquality"

// Coordenadas iniciales GPS
#define INITIAL_LAT 42.411567
#define INITIAL_LONG 13.397309
#define MOVE_AMOUNT 1

// Configuración de WiFi
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// Configuración de DHT22
#define DHTPIN 15
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

// Configuración de sensores analógicos
#define PM25_PIN 34
#define PM10_PIN 35

// Configuración de pantalla OLED
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 oled(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// Cliente MQTT
WiFiClient espClient;
PubSubClient client(espClient);

// Variables GPS simuladas
float prev_lat = INITIAL_LAT;
float prev_long = INITIAL_LONG;

// Definimos colas para almacenar los valores del sensor DHT22 y del potenciómetro
QueueHandle_t xQueueHumidity;
QueueHandle_t xQueuePM10;
QueueHandle_t xQueuePM25;

// Mútex para asegurar que una sola tarea accede al panel LCD a la vez
QueueHandle_t xMutex;

// Identificadores de las tareas del sistema
TaskHandle_t xHandleHumidityReader, xHandlePM25Reader, xHandlePM10Reader, xHandleLCDReceiver;

// Función para mapear valores de ADC
float mapADC(int raw, int minADC, int maxADC, float minVal, float maxVal) {
  return (float)(raw - minADC) * (maxVal - minVal) / (maxADC - minADC) + minVal;
}

// Función para obtener coordenadas aleatorias
void getCoordinates(float &lat, float &long_) {
  float variation_lat = ((float)random(-1000, 1000) / 1000000) * MOVE_AMOUNT;
  float variation_long = ((float)random(-1000, 1000) / 1000000) * MOVE_AMOUNT;
  lat += variation_lat;
  long_ += variation_long;
}

// Tarea para leer la humedad desde el sensor DHT22
void vTaskHumidityReader(void* pvParam){
  portBASE_TYPE xStatus;  
  float humidity;  
  const portTickType xTicksToWait = pdMS_TO_TICKS(250); // Espera de 250 ms

  for(;;) {
    humidity = dht.readHumidity();

    if (isnan(humidity)) {  // Verifica si la lectura es válida
      Serial.println("Error al leer la temperatura del sensor DHT22");
    } else {
      xStatus = xQueueSendToBack(xQueueHumidity, &humidity, 0); // Envía el valor a la cola
      if (xStatus != pdPASS) Serial.println("Could not send to the queue.\r\n");
    }
    vTaskDelay(xTicksToWait);  // Espera 250 ms antes de la próxima lectura
  }
  vTaskDelete(NULL);
}

// Tarea para leer el valor de PM10
void vTaskPM10Reader(void* pvParam) {
  portBASE_TYPE xStatus;  
  int rawPM10;
  float pm10;
  const portTickType xTicksToWait = pdMS_TO_TICKS(250); // Espera de 250 ms

  for(;;) {
    rawPM10 = analogRead(PM10_PIN); // Lee el valor analógico del potenciómetro
    pm10 = mapADC(rawPM10, 0, 4095, 0, 500); // Mapea el valor de 10 a 30
    
    if (isnan(rawPM10)) {                         // Verifica si la lectura es válida
      Serial.print("Error al leer el valor del potenciómetro");
    } else {
      xStatus = xQueueSendToBack(xQueuePM10, &pm10, 0); // Envía el valor a la cola
    }
    
    vTaskDelay(xTicksToWait);  // Espera 250 ms antes de la próxima lectura
  }
  vTaskDelete(NULL);
}

// Tarea para leer el valor de PM25
void vTaskPM25Reader(void* pvParam) {
  portBASE_TYPE xStatus;  
  int rawPM25;
  float pm25;
  const portTickType xTicksToWait = pdMS_TO_TICKS(250); // Espera de 250 ms

  for(;;) {
    rawPM25 = analogRead(PM25_PIN); // Lee el valor analógico del potenciómetro
    pm25 = mapADC(rawPM25, 0, 4095, 0, 500);  // Mapea el valor de 0 a 500

    if (isnan(rawPM25)) {                         // Verifica si la lectura es válida
      Serial.print("Error al leer el valor del potenciómetro");
    } else {
      xStatus = xQueueSendToBack(xQueuePM25, &pm25, 0); // Envía el valor a la cola
    }
    
    vTaskDelay(xTicksToWait);  // Espera 250 ms antes de la próxima lectura
  }
  vTaskDelete(NULL);
}

// Conexión WiFi
void connectWiFi() {
  Serial.println("Conectando a WiFi...");
  WiFi.begin(ssid, password);
  
  int attempts = 0;  // Contador de intentos
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
    attempts++;
    if (attempts > 20) {  // Limitar el número de intentos
      Serial.println("Error: No se pudo conectar a la red WiFi.");
      return;
    }
  }
  
  Serial.println("");
  Serial.println("¡Conexión WiFi establecida!");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

// Conexión a MQTT
void connectMQTT() {
  while (!client.connected()) {
    Serial.print("Conectando al servidor MQTT...");
    if (client.connect(MQTT_CLIENT_ID)) {
      Serial.println("¡Conectado!");
    } else {
      Serial.print("Fallo, rc=");
      Serial.print(client.state());
      Serial.println(" Intentando de nuevo en 5 segundos...");
      delay(5000);
    }
  }
}

// Tarea para mostrar los datos en el LCD y publicar en el topico
void vTaskLCDReceiver(void* pvParam) {
  const portTickType xTicksToWait = pdMS_TO_TICKS(250);
  float receivedPM10, currentPM10;
  float receivedPM25, currentPM25;
  float receivedHumidity, currentHumidity;

  portBASE_TYPE xStatusPM10;
  portBASE_TYPE xStatusPM25;  
  portBASE_TYPE xStatusHumidity;

  for(;;) {
    if (xSemaphoreTake(xMutex, portMAX_DELAY) == pdTRUE) { // Toma el mútex
      // Actualizar coordenadas
      getCoordinates(prev_lat, prev_long);
      
      xStatusHumidity = xQueueReceive(xQueueHumidity, &receivedHumidity, 0);  // Recibe el valor del DHT22
      if (xStatusHumidity) {
        currentHumidity = receivedHumidity;
        // Crear mensaje JSON
        String message = "{\"station\":\"" + String(MQTT_CLIENT_ID) + 
          ",\"value\":" + String(currentHumidity, 1) +
        "}";
        // Publicar en MQTT
        if (client.publish(MQTT_TOPIC, message.c_str())) {
          Serial.println("Mensaje publicado: " + message);
        } else {
          Serial.println("Error al publicar mensaje.");
        }
      }

      xStatusPM10 = xQueueReceive(xQueuePM10, &receivedPM10, 0);  // Recibe el valor del potenciómetro
      if (xStatusPM10) {
        currentPM10 = receivedPM10;
        // Crear mensaje JSON
        String message = "{\"station\":\"" + String(MQTT_CLIENT_ID) + 
          ",\"value\":" + String(currentPM10, 1) +
        "}";
        // Publicar en MQTT
        if (client.publish(MQTT_TOPIC, message.c_str())) {
          Serial.println("Mensaje publicado: " + message);
        } else {
          Serial.println("Error al publicar mensaje.");
        }
      }

      xStatusPM25 = xQueueReceive(xQueuePM25, &receivedPM25, 0);  // Recibe el valor del potenciómetro
      if (xStatusPM25) {
        currentPM25 = receivedPM25;
        // Crear mensaje JSON
        String message = "{\"station\":\"" + String(MQTT_CLIENT_ID) + 
          ",\"value\":" + String(currentPM25, 1) +
        "}";
        // Publicar en MQTT
        if (client.publish(MQTT_TOPIC, message.c_str())) {
          Serial.println("Mensaje publicado: " + message);
        } else {
          Serial.println("Error al publicar mensaje.");
        }
      }
      // Mostrar datos en OLED
      oled.clearDisplay();
      oled.setTextSize(1);
      oled.setTextColor(WHITE);
      oled.setCursor(0, 0);
      oled.print("Humedad: ");
      oled.print(currentHumidity, 1);
      oled.println("%");
      oled.print("PM2.5: ");
      oled.print(currentPM25, 1);
      oled.println(" ug/m3");
      oled.print("PM10: ");
      oled.print(currentPM10, 1);
      oled.println(" ug/m3");
      oled.print("Lat: ");
      oled.println(prev_lat, 5);
      oled.print("Long: ");
      oled.println(prev_long, 5);
      oled.display();
    }

    xSemaphoreGive(xMutex);  // Libera el mútex

    vTaskDelay(pdMS_TO_TICKS(250));  // Espera 250 ms antes de la próxima lectura
  }

  vTaskDelete(NULL);
}

void setup() {
  Serial.begin(115200);
  randomSeed(analogRead(0));

  // Configuración inicial
  connectWiFi();
  client.setServer(MQTT_BROKER, MQTT_PORT);

  // Inicializar DHT y pantalla OLED
  dht.begin();
  if (!oled.begin(0x3C, OLED_RESET)) {
    Serial.println("No se encontró pantalla OLED.");
    for (;;);
  }
  oled.clearDisplay();
  app_main();
}

// Funcion principal de FreeRTOS
void app_main(void){
  if(xTaskGetSchedulerState() == taskSCHEDULER_RUNNING)
    printf("Scheduler is running\n");

  if (!client.connected()) {
    connectMQTT();
  }
  
  // Crea colas para los datos de temperatura y potenciómetro
  xQueueHumidity = xQueueCreate(5, sizeof(float));
  xQueuePM10 = xQueueCreate(5, sizeof(float));
  xQueuePM25 = xQueueCreate(5, sizeof(float));

  xMutex = xSemaphoreCreateMutex();  // Crea el mútex

  // Crea las tareas solo si las colas y el mútex se crearon con éxito
  if (xQueueHumidity != NULL && xQueuePM10 != NULL && xQueuePM25 != NULL && xMutex != NULL) {
    xTaskCreate(vTaskHumidityReader, "Humidity Reader", 4096, NULL, 1, &xHandleHumidityReader);
    xTaskCreate(vTaskPM10Reader, "PM10 Reader", 2048, NULL, 1, &xHandlePM10Reader);
    xTaskCreate(vTaskPM25Reader, "PM25 Reader", 2048, NULL, 2, &xHandlePM25Reader);
    xTaskCreate(vTaskLCDReceiver, "LCD Display", 4096, NULL, 1, &xHandleLCDReceiver);
  }
}

// Bucle vacío para mantener el esquema de Arduino
void loop() {
}