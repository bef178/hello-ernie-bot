# Hello Ernie Bot

A simple service talking to Ernie Bot.

Using SpringBoot WebFlux. With Padding.

```bash
curl --location 'http://localhost:8084/api/ernie-bot' \
--header 'Content-Type: application/json' \
--data '{
    "requestId": "111",
    "sessionId": "111",
    "queryText": "中午吃啥"
}'
```