# Бот для событий CU (@cu_events_bot)

## О боте
Часто выходит так, что регистрация на события закрывается очень быстро, а пост в чате делается не сразу. Этот бот сделан для более быстро и удобного автоматического уведомления о новых событиях

## Self hosting
В ci автоматически собирается образ докера и пушится в репозиторий https://hub.docker.com/repository/docker/kroune/cu-events-bot/general.
<br>
<br>
При желании вы можете использовать этот образ или создать свой, использую Dockerfile в репозитории, или просто запустить `./gradlew run`
<br>
<br>
Для запуска бота нужен конфиг, он должен находить в директории CONFIG_PATH (переменная среды) или /etc/cu-events-bot/config.json, если CONFIG_PATH не задан
<br>
<br>
Пример конфига бота

    {
      "serverConfig": {
        "host": "0.0.0.0",
        "port": 8080
      },
      "grafanaConfig": {
        "nameForScrape": "Имя для basicAuth prometheus'а",
        "passwordForScrape": "Пароль для basicAuth prometheus'а"
      },
      "botToken": "Токен бота в телеграмме",
      "authCookie": "Куки для аунтицикации на сайте centraluniversity (он называется bff.cookie)",
      "databasesConfig": {
        "userData": {
          "url": "URL для подключения к postgres",
          "username": "Имя для подключения к postgres",
          "password": "Пароль для подключения к postgres"
        }
      }
    }