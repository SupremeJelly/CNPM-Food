FROM node:18 as builder

WORKDIR /app

COPY package*.json ./

RUN npm install -g @angular/cli && npm install

COPY . .

RUN npm run build && ls -la dist/

FROM nginx:alpine

# Copy the built files from correct location
COPY --from=builder /app/dist/frontend /usr/share/nginx/html/

# Debug: List files in nginx html directory
RUN ls -la /usr/share/nginx/html/

RUN echo 'server { \
    listen 80; \
    server_name _; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
