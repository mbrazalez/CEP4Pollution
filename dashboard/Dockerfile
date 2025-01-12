FROM node:16-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json (if available)
COPY package*.json ./

# Install dependencies
RUN npm install --silent

# Copy the app's source code into the container
COPY . .

# Expose the default port the development server runs on
EXPOSE 3000

# Execute the app
CMD ["npm", "start"]


