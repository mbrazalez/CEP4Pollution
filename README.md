# Guía de Instalación y Uso

Este documento proporciona las instrucciones necesarias para configurar y ejecutar los contenedores Docker que contienen la dashboard y el motor CEP para la simulación de la ESP32 con wowki.

## Prerrequisitos
- **Docker**: Asegúrate de tener Docker instalado en tu sistema antes de proceder. Puedes descargarlo e instalarlo desde [Docker Docs](https://docs.docker.com/engine/install/).

## Configuración

### Paso 1: Configurar el `docker-compose.yml`
En caso de que se quiera usar un bróker MQTT propio, modifica el archivo `docker-compose.yml` para actualizar las variables de entorno `BROKER_IP`, `REACT_APP_BROKER_IP` y `MQTT_BROKER_ADDRESS` con la IP de tu broker MQTT. Esta variable viene configurada por defecto con la IP del broker público `test.mosquitto.org` el cual nos va a servir para comprobar el funcionamiento de la aplicación sin necesidad de desplegar un bróker propio.

### Paso 2: Configurar el `sketch.ino`
En el documento `sketch.ino` ubicado en la carpeta `codigo wowki`, si se quisiera usar un bróker propio, cambia el valor de la variable `MQTT_BROKER` por la IP del broker. Esta variable viene configurada por defecto con el broker público `test.mosquitto.org`.

## Despliegue

### Paso 3: Desplegar los Contenedores
Dirígete a la ruta donde se encuentre el archivo `docker-compose.yml`  y ejecuta el siguiente comando en una terminal para desplegar los contenedores que contienen las dashboards y el motor CEP:
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

### Paso 6: Acceder a la Node Red

Abre tu navegador y accede al panel de node red a través de la siguiente URL:
```bash
http://localhost:1880/
```

### Paso 7: Acceder a la dashboard de Node Red
Para abrir la dashboard de node red haz click en la opción **dashboard** que se encuentra en el panel de opciones de la derecha de la pantalla, una vez aquí deberás acceder a través del botón con el icono de enlace.

### Paso 8: (Opcional) API Swagger del Motor CEP
Si estás familiarizado con Esper y el procesamiento de eventos complejos, puedes acceder a la configuración de la API en:
```bash
http://localhost:8080/swagger-ui.html
```
En esta página encontrarás un documento de configuración de API con todas las operaciones que se pueden realizar sobre el motor CEP mediante peticiones HTTP.

### Repositorios con el código fuente empleado para las imágenes Docker:
- [API con el Motor CEP](https://github.com/mbrazalez/scf-project-repo/tree/dev/EsperAPI) - [Docker Image](https://hub.docker.com/r/mbrazalez/api4eventprocessing)
- [Dashboard para la visualización de valores en tiempo real](https://github.com/mbrazalez/scf-project-repo/tree/dev/dashboard) - [Docker Image](https://hub.docker.com/r/mbrazalez/dashboard)
- [Node Red con las dashboard y la conexión al bróker MQTT](https://github.com/mbrazalez/scf-project-repo/tree/dev/node-red) - [Docker Image](https://hub.docker.com/r/mbrazalez/node-red-custom)



