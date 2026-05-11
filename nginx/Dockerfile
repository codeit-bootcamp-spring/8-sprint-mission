FROM nginx:alpine

# Nginx 기본 설정 제거
RUN rm /etc/nginx/conf.d/default.conf

# 1. 커스텀 설정 복사 (nginx.conf 대신 실제 파일명인 api-gateway.conf 사용)
COPY ./conf/api-gateway.conf /etc/nginx/conf.d/

# 2. 프론트엔드 정적 파일들을 Nginx의 html 폴더로 복사
COPY ./html /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]