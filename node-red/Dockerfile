FROM nodered/node-red:latest

# Copiar archivos de configuración personalizados y flujos
COPY settings.js /data/settings.js
COPY flows.json /data/flows.json

# Instalar nodo dashboard
RUN npm install node-red-dashboard
