FROM node:18 as builder

WORKDIR /app

COPY package*.json ./

RUN npm install -g @angular/cli && npm install

COPY . .

RUN npm run build && ls -la dist/

FROM nginx:alpine

# Copy the built files from browser directory
COPY --from=builder /app/dist/frontend/browser/* /usr/share/nginx/html/

# Debug: List files in nginx html directory
RUN ls -la /usr/share/nginx/html/

RUN echo 'server { \
    listen 80; \
    server_name _; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html =404; \
    } \
    location ~ \.html$ { \
        add_header Cache-Control "private, no-cache, no-store, must-revalidate"; \
        add_header Expires "Sat, 01 Jan 2000 00:00:00 GMT"; \
        add_header Pragma no-cache; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
