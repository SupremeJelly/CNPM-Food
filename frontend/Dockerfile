# Sử dụng Node.js
FROM node:18

WORKDIR /app

# Copy file cấu hình
COPY package*.json ./

# Cài dependencies
RUN npm install -g @angular/cli && npm install

# Copy source code
COPY . .

# Mở cổng cho Angular
EXPOSE 4200

# Chạy app Angular ở dev mode
CMD ["npm", "start"]
