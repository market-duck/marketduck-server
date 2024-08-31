
docker-compose run --rm --entrypoint "\
certbot certonly \
-d marketduck.server-su.site \
--email sussa@gmail.com \
--manual --preferred-challenges dns \
--server https://acme-v02.api.letsencrypt.org/directory \
--force-renewal" certbot

docker-compose exec nginx_server nginx -s reload