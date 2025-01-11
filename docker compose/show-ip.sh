#!/bin/sh
# Muestra la dirección IP del contenedor
ip=$(hostname -i)
echo "Mosquitto broker IP: $ip"
# Luego inicia Mosquitto con su configuración habitual
/usr/sbin/mosquitto -c /mosquitto/config/mosquitto.conf
