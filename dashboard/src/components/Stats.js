import React, { useEffect, useState } from 'react';
import mqtt from 'mqtt';
import { Line } from 'react-chartjs-2';
import 'chart.js/auto';
import 'chartjs-adapter-date-fns';
import StationSelector from './StationSelector'; 

const COLORS = {
  A1: 'red',
  A2: 'blue',
  A3: 'green',
  A4: 'orange',
  A5: 'purple',
  A6: 'brown',
};

const CONNECTION_NAMES = {
  A1: 'Punta del Parque',
  A2: 'Perpetuo Socorro',
  A3: 'Fuente de las ranas',
  A4: 'Hospital General',
  A5: 'Universidad',
  A6: 'Imaginalia',
};

const mqttBrokerIP = process.env.REACT_APP_MQTTBROKERIP || 'localhost';

const initialState = Object.keys(CONNECTION_NAMES).reduce((acc, key) => {
  acc[key] = Array(10).fill({ x: new Date().getTime(), y: 0 });
  return acc;
}, {});

export default function Stats() {
  const [pm10Data, setPm10Data] = useState(initialState);
  const [pm25Data, setPm25Data] = useState(initialState);
  const [humidityData, setHumidityData] = useState(initialState);
  const [selectedStations, setSelectedStations] = useState([]);


  useEffect(() => {
    const client = mqtt.connect(`ws://${mqttBrokerIP}:9001`);
    client.on('connect', () => {
      console.log('Connected to MQTT broker.');
      client.subscribe('highpm10topic');
      client.subscribe('highpm25topic');
      client.subscribe('highhumiditytopic');
    });

    client.on('message', (topic, message) => {
      const parsedMessage = JSON.parse(message.toString());
      const timestamp = parsedMessage.timestamp < 1e12 ? parsedMessage.timestamp * 1000 : parsedMessage.timestamp;
      const newPoint = { x: timestamp, y: parsedMessage.value };
      const connection = parsedMessage.station;

      if (CONNECTION_NAMES[connection]) {
        switch (topic) {
          case 'highpm10topic':
            updateData(setPm10Data, connection, newPoint);
            break;
          case 'highpm25topic':
            updateData(setPm25Data, connection, newPoint);
            break;
          case 'highhumiditytopic':
            updateData(setHumidityData, connection, newPoint);
            break;
         
          default:
            break;
        }
      } else {
        console.warn(`Unknown connection: ${connection}`);
      }
    });

    return () => {
      client.end();
    };
  }, []);

  const updateData = (setData, connection, newPoint) => {
    setData(prevData => {
      const updatedConnectionData = [...(prevData[connection] || []), newPoint].slice(-10);
      return { ...prevData, [connection]: updatedConnectionData };
    });
  };


  const handleStationChange = (event) => {
    const value = event.target.value;
    if (event.target.checked) {
      setSelectedStations((prevSelected) => [...prevSelected, value]);
    } else {
      setSelectedStations((prevSelected) => prevSelected.filter((station) => station !== value));
    }
  };

  const createDataset = (connection, data) => ({
    label: CONNECTION_NAMES[connection],
    data,
    fill: false,
    borderColor: COLORS[connection],
    backgroundColor: COLORS[connection],
  });

  const createChartData = (data) => ({
    labels: [],
    datasets: selectedStations.map(connection => createDataset(connection, data[connection]))
  });

  const chartOptions = {
    responsive: true,
    scales: {
      x: {
        type: 'time',
        time: {
          unit: 'minute',
          stepSize: 1,
        },
        title: {
          display: true,
          text: 'Date of the measurement'
        }
      },
      y: {
        title: {
          display: true,
          text: 'Detected value'
        }
      }
    }
  };

  const stationOptions = Object.keys(CONNECTION_NAMES).map(station => ({
    value: station,
    label: CONNECTION_NAMES[station]
  }));

  return (
    <div className="container mx-auto">
      <StationSelector
        options={stationOptions}
        selectedOptions={selectedStations}
        onChange={handleStationChange}
      />
      <div className="grid gap-4 grid-cols-1 md:grid-cols-2 lg:grid-cols-2">
        <div>
          <Line data={createChartData(pm10Data)} options={{...chartOptions, plugins: { title: { display: true, text: 'PM10 Concentration' } }}} />
        </div>
        <div>
          <Line data={createChartData(pm25Data)} options={{...chartOptions, plugins: { title: { display: true, text: 'PM2.5 Concentration' } }}} />
        </div>
        <div>
          <Line data={createChartData(humidityData)} options={{...chartOptions, plugins: { title: { display: true, text: 'Humidity' } }}} />
        </div>
      </div>
    </div>
  );
}
