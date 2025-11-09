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

RUN echo 'server { \n\
    listen 80; \n\
    server_name localhost; \n\
    \n\
    root /usr/share/nginx/html; \n\
    index index.html; \n\
    \n\
    location / { \n\
        try_files $uri $uri/ /index.html; \n\
    } \n\
    \n\
    error_page 404 /index.html; \n\
    \n\
    location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ { \n\
        expires 1y; \n\
        add_header Cache-Control "public, no-transform"; \n\
    } \n\
    \n\
    location ~* \.(html)$ { \n\
        expires -1; \n\
        add_header Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0"; \n\
    } \n\
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
