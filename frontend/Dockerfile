FROM node:18 as builder

WORKDIR /app

COPY package*.json ./

RUN npm install -g @angular/cli && npm install

COPY . .

RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Ensure nginx sends UTF-8 charset in Content-Type header
RUN sed -i "s/server {/server {\n    charset utf-8;/" /etc/nginx/conf.d/default.conf || true

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
