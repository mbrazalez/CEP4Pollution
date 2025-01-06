FROM node:16-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json (if available)
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of your app's source code from your host to your image filesystem.
COPY . .

# Expose the default port the development server runs on
EXPOSE 3000

# CMD to start the app, now set to run a script instead
CMD ["sh", "entrypoint.sh"]
