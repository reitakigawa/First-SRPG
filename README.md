# First-DiscordBot
はじめてのDiscord Bot

## Documents
- [SRPG 要件定義書](docs/srpg-requirements.md)
- [SRPG メイン処理フロー（Spring Boot / Java）](docs/main-processing.md)

## SRPG Backend Prototype (Spring Boot)

### Run
```bash
mvn spring-boot:run
```

### Simulate Stage
```bash
curl "http://localhost:8080/api/srpg/simulate?scenario=stage1"
```
