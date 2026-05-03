FROM nginx:1.27-alpine

# round_robin | least_conn | ip_hash | weight — 빌드 시 전략 선택 (docker-compose build args와 동일)
ARG NGINX_LB_STRATEGY=round_robin
COPY deploy/nginx/upstreams/${NGINX_LB_STRATEGY}.conf /etc/nginx/conf.d/00-upstream.conf
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY src/main/resources/static /usr/share/nginx/html

EXPOSE 80
