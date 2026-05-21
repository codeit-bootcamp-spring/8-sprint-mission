FROM nginx:alpine

# Nginx 기본 설정 제거 후 커스텀 설정 복사
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# 프론트엔드 빌드 결과물 복사
COPY src/main/resources/static /usr/share/nginx/html

EXPOSE 80
