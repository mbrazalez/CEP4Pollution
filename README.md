# Guía de Instalación y Uso

Este documento proporciona las instrucciones necesarias para configurar y ejecutar los contenedores Docker que contienen la dashboard y el motor CEP para la simulación de la ESP32 con wowki.

## Prerrequisitos
- **Docker**: Asegúrate de tener Docker instalado en tu sistema antes de proceder. Puedes descargarlo e instalarlo desde [Docker Docs](https://docs.docker.com/engine/install/).

## Configuración

### Paso 1: Configurar el `docker-compose.yml`
Modifica el archivo `docker-compose.yml` para actualizar las variables de entorno `BROKER_IP` y `REACT_APP_BROKER_IP` con la IP de tu broker MQTT, por ejemplo, `test.mosquitto.org`.

### Paso 2: Configurar el `sketch.ino`
En el documento `sketch.ino` ubicado en la carpeta `codigo wowki`, cambia el valor de la variable `MQTT_BROKER` por la IP del broker.

## Despliegue

### Paso 3: Desplegar los Contenedores
Ejecuta el siguiente comando en una terminal para desplegar los contenedores que contienen la dashboard y el motor CEP:
```bash
docker compose up
```
### Paso 4: Iniciar simulación
Ejecuta la simulación de la ESP32 con wowki

### Paso 5: Acceder a la Dashboard

Abre tu navegador y accede a la dashboard a través de la URL:
```bash
http://localhost:3000
```

### Paso 6: (Opcional) API Swagger del Motor CEP
Si estás familiarizado con Esper y el procesamiento de eventos complejos, puedes acceder a la configuración de la API en:
```bash
http://localhost:8080/swagger-ui.html
```
En esta página encontrarás un documento de configuración de API con todas las operaciones que se pueden realizar sobre el motor CEP mediante peticiones HTTP.

### Repositorios con el código fuente empleado para las imágenes Docker:
- [API con el Motor CEP](https://github.com/mbrazalez/scf-project-repo/tree/dev/EsperAPI) - [Docker Image](https://hub.docker.com/r/mbrazalez/api4eventprocessing)
- [Dashboard para la visualización de valores en tiempo real](https://github.com/mbrazalez/scf-project-repo/tree/dev/dashboard) - [Docker Image](https://hub.docker.com/r/mbrazalez/dashboard)





