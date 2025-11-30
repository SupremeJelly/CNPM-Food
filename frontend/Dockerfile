# FROM node:18 as builder

# WORKDIR /app

# COPY package*.json ./

# RUN npm install -g @angular/cli && npm install

# COPY . .

# RUN npm run build

# FROM nginx:alpine

# COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# RUN echo 'server { \
#     listen 80; \
#     server_name localhost; \
#     root /usr/share/nginx/html; \
#     index index.html; \
#     location / { \
#         try_files $uri $uri/ /index.html; \
#     } \
# }' > /etc/nginx/conf.d/default.conf

# # Ensure nginx sends UTF-8 charset in Content-Type header
# RUN sed -i "s/server {/server {\n    charset utf-8;/" /etc/nginx/conf.d/default.conf || true

# EXPOSE 80

# CMD ["nginx", "-g", "daemon off;"]

# Stage 1: Build Angular
FROM node:18 as builder

WORKDIR /app

COPY package*.json ./
# Cài đặt dependency
RUN npm install

COPY . .

# Build project với cấu hình productionWW
RUN npm run build -- --configuration=production

# Stage 2: Serve với Nginx
FROM nginx:alpine

# ⚠️ QUAN TRỌNG: Copy đúng thư mục 'browser' do Angular 17 tạo ra
# Tên project là 'frontend' (lấy từ angular.json)
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Tạo file cấu hình Nginx trực tiếp tại đây để tránh lỗi MIME type
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    # Nén dữ liệu để tải nhanh hơn
    gzip on; \
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript; \
    \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    \
    # Cache cho file tĩnh (JS/CSS)
    location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ { \
        expires 1y; \
        add_header Cache-Control "public, immutable"; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]