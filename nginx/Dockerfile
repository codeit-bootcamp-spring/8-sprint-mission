FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx/conf/default.conf /etc/nginx/conf.d/default.conf

COPY src/main/resources/static /usr/share/nginx/html

EXPOSE 80