#include <WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <time.h>


// Incluimos las librerías de FreeRTOS para manejo de tareas, colas y semáforos
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "freertos/semphr.h"

// Configuración de WiFi
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// Configuración de MQTT
#define MQTT_CLIENT_ID "wokwi_sensor"
#define MQTT_BROKER "test.mosquitto.org"
#define MQTT_PORT 1883

// Cliente MQTT
WiFiClient espClient;
PubSubClient client(espClient);

// Lista de estaciones
String stations[] = {"A1", "A2", "A3", "A4", "A5", "A6"};

// Configuración de DHT22
#define DHTPIN 27
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);
struct TempAndHumidity {
    float temperature;
    float humidity;
};

// Configuración de sensores analógicos
#define PM25_PIN 34
#define PM10_PIN 35

// Configuración de pantalla OLED
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
Adafruit_SSD1306 oled(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// Definimos colas para almacenar los valores del sensor DHT22 y del potenciómetro
QueueHandle_t xQueueTempHumidity;
QueueHandle_t xQueueTemperature;
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

// Función para obtener estación aleatoria
String getRandomStation() {
  int randomIndex = random(0, 6);
  return stations[randomIndex];
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

// Configura la zona horaria y el servidor NTP
void configTimeSetup() {
  // Configurar zona horaria
  configTime(1 * 3600, 3600, "pool.ntp.org", "time.nist.gov");

  Serial.println("\nWaiting for time");
  struct tm timeinfo;
  while (!getLocalTime(&timeinfo)) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("\nTime synchronized");
}

// Tarea para leer la humedad desde el sensor DHT22
void vTaskHumidityReader(void* pvParam){
  portBASE_TYPE xStatus;  
  TempAndHumidity  data;
  const portTickType xTicksToWait = pdMS_TO_TICKS(250); // Espera de 250 ms

  for(;;) { 
    data.humidity = dht.readHumidity();
    data.temperature = dht.readTemperature();

    if (isnan(data.humidity) || isnan(data.temperature)) {
      Serial.println("Error al leer del sensor DHT22");
    } else {
      xStatus = xQueueSendToBack(xQueueTempHumidity, &data, 0); // Envía el valor a la cola
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

// Tarea para mostrar los datos en el LCD y publicar en el topico
void vTaskLCDReceiver(void* pvParam) {
  const portTickType xTicksToWait = pdMS_TO_TICKS(250);
  float receivedPM10, currentPM10;
  float receivedPM25, currentPM25;
  TempAndHumidity receivedHumidityTemp;
  float currentTemperature, currentHumidity;

  portBASE_TYPE xStatusPM10;
  portBASE_TYPE xStatusPM25;  
  portBASE_TYPE xStatusTempHumidity;

  for(;;) {
    if (xSemaphoreTake(xMutex, portMAX_DELAY) == pdTRUE) { // Toma el mútex
      
      if (!client.connected()) {
        connectMQTT();
      }

      xStatusTempHumidity = xQueueReceive(xQueueTempHumidity, &receivedHumidityTemp, 0);  // Recibe el valor del DHT22
      if (xStatusTempHumidity) {
        currentHumidity = receivedHumidityTemp.humidity;
        currentTemperature = receivedHumidityTemp.temperature;

        // Crear mensaje JSON
        String message = "{\"station\":\"" + getRandomStation() + 
          "\",\"value\":" + String(currentHumidity, 2) +
          ",\"timestamp\":" + getEpochTime() +
        "}";
        // Publicar en MQTT para el motor CEP
        if (client.publish("humiditytopic", message.c_str())) {
          Serial.println("Mensaje publicado: " + message);
        } else {
          Serial.println("Error al publicar mensaje.");
        } 

        String humidityStr = String(currentHumidity, 1);
        String temperatureStr = String(currentTemperature, 2);

        // Publicar en MQTT para Node Red
        if (client.publish("/nr/humiditytopic", humidityStr.c_str()) &&
        client.publish("/nr/temperaturetopic", temperatureStr.c_str())) {
          Serial.println("Mensaje publicado: " + humidityStr);
          Serial.println("Mensaje publicado: " + temperatureStr);
        } else {
          Serial.println("Error al publicar mensaje.");
        }

      }


      xStatusPM10 = xQueueReceive(xQueuePM10, &receivedPM10, 0);  // Recibe el valor del potenciómetro
      if (xStatusPM10) {
        currentPM10 = receivedPM10;
        // Crear mensaje JSON
        String message = "{\"station\":\"" + getRandomStation() + 
          "\",\"value\":" + String(currentPM10, 1) +
          ",\"timestamp\":" + getEpochTime() +
        "}";
        // Publicar en MQTT
        if (client.publish("pm10topic", message.c_str())) {
          Serial.println("Mensaje publicado: " + message);
        } else {
          Serial.println("Error al publicar mensaje.");
        }
      }

      xStatusPM25 = xQueueReceive(xQueuePM25, &receivedPM25, 0);  // Recibe el valor del potenciómetro
      if (xStatusPM25) {
        currentPM25 = receivedPM25;
        // Crear mensaje JSON
        String message = "{\"station\":\"" + getRandomStation() + 
          "\",\"value\":" + String(currentPM25, 1) +
          ",\"timestamp\":" + getEpochTime() +
        "}";
        // Publicar en MQTT
        if (client.publish("pm25topic", message.c_str())) {
          Serial.println("Mensaje publicado: " + message);
        } else {
          Serial.println("Error al publicar mensaje.");
        }
      }

      oled.clearDisplay();
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
      oled.print("Temperatura: ");
      oled.print(currentTemperature, 1);
      oled.println(" ºC");
      oled.display();
    }

    xSemaphoreGive(xMutex);  // Libera el mútex

    vTaskDelay(pdMS_TO_TICKS(250));  // Espera 250 ms antes de la próxima lectura
  }

  vTaskDelete(NULL);
}

// void vTaskMonitorStack(void* pvParam) {
//   for (;;) {
//     UBaseType_t highWaterHumidity = uxTaskGetStackHighWaterMark(xHandleHumidityReader);
//     UBaseType_t highWaterPM10 = uxTaskGetStackHighWaterMark(xHandlePM10Reader);
//     UBaseType_t highWaterPM25 = uxTaskGetStackHighWaterMark(xHandlePM25Reader);
//     UBaseType_t highWaterLCD = uxTaskGetStackHighWaterMark(xHandleLCDReceiver);

//     Serial.println("---- Stack Libre ----");
//     Serial.print("Humidity Reader: ");
//     Serial.println(highWaterHumidity);
//     Serial.print("PM10 Reader: ");
//     Serial.println(highWaterPM10);
//     Serial.print("PM25 Reader: ");
//     Serial.println(highWaterPM25);
//     Serial.print("LCD MQTT Receiver: ");
//     Serial.println(highWaterLCD);
//     Serial.println("---------------------");

//     vTaskDelay(pdMS_TO_TICKS(5000));
//   }
//   vTaskDelete(NULL);
// }

void setup() {
  Serial.begin(115200);
  randomSeed(analogRead(0));

  // Configuración inicial
  connectWiFi();
  client.setServer(MQTT_BROKER, MQTT_PORT);

  // Configurar tiempo
  configTimeSetup();

  // Inicializar DHT y pantalla OLED
  dht.begin();

  // Comprobacion si el display esta configurado correctamente
  if(!oled.begin(SSD1306_SWITCHCAPVCC, 0x3C)) { 
    Serial.println("No se encontró pantalla OLED.");
    for (;;);
  }
  
  // Limpia la pantalla y configura texto y color 
  oled.clearDisplay();
  oled.setTextSize(0.8);
  oled.setTextColor(WHITE);
  
  app_main();
}

// Funcion para obtener la hora
unsigned long getEpochTime() {
  struct tm timeinfo;
  if (getLocalTime(&timeinfo)) {
    time_t now = mktime(&timeinfo);
    return (unsigned long)now;
  } else {
    Serial.println("Failed to obtain time");
    return 0;
  }
}

// Funcion principal de FreeRTOS
void app_main(void){
  if(xTaskGetSchedulerState() == taskSCHEDULER_RUNNING)
    printf("Scheduler is running\n");
  
  // Enceder pantalla oled
  oled.ssd1306_command(SSD1306_DISPLAYON);

  // Crea colas para los datos de temperatura, humedad y potenciómetros
  xQueueTempHumidity = xQueueCreate(20, sizeof(TempAndHumidity));
  xQueuePM10 = xQueueCreate(20, sizeof(float));
  xQueuePM25 = xQueueCreate(20, sizeof(float));

  xMutex = xSemaphoreCreateMutex();  // Crea el mútex

  // Crea las tareas solo si las colas y el mútex se crearon con éxito
  if (xQueueTempHumidity != NULL && xQueuePM10 != NULL && xQueuePM25 != NULL && xMutex != NULL) {
    xTaskCreate(vTaskHumidityReader, "Humidity Reader", 2048, NULL, 1, &xHandleHumidityReader);
    xTaskCreate(vTaskPM10Reader, "PM10 Reader", 2048, NULL, 1, &xHandlePM10Reader);
    xTaskCreate(vTaskPM25Reader, "PM25 Reader", 2048, NULL, 1, &xHandlePM25Reader);
    xTaskCreate(vTaskLCDReceiver, "MQTT and LCD", 4096, NULL, 2, &xHandleLCDReceiver);
  }
}

void loop() {
}