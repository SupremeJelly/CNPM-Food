FROM node:18

WORKDIR /app

COPY package*.json ./

RUN npm install -g @angular/cli && npm install

COPY . .

# Expose default Angular dev server port
EXPOSE 4200

# Start Angular dev server with host 0.0.0.0 to allow external access
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--disable-host-check"]
