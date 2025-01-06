#!/bin/sh
# Set the REACT_APP_MQTTBROKERIP dynamically
export REACT_APP_MQTTBROKERIP=$(getent hosts mosquitto | awk '{ print $1 }')

# Build the app with the updated environment variable
npm run build

# Start the application
npm start
