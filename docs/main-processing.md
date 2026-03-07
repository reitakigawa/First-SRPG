# SRPG メイン処理フロー（Spring Boot / Java）

このドキュメントは `SrpgEngineService` の処理を要約したものです。

## 処理の流れ
1. `ScenarioLoader` が `src/main/resources/scenarios/*.json` を読み込む。
2. `SrpgEngineService` がユニット情報を初期化する。
3. ターンループを開始する。
   - PLAYER フェーズ
   - ENEMY フェーズ
4. 各ユニットは以下を1回実行する。
   - 射程外なら最寄り敵に向けて移動
   - 射程内なら攻撃
   - 攻撃対象がいない場合は待機
5. 行動ごとに勝敗判定を行う。
   - 敵全滅: `victory`
   - 味方全滅: `defeat`
6. ターン上限に達した場合は `turn_limit` で終了。

## 実装済み要件
- ターン制（プレイヤー → 敵）
- 1ターン行動（移動 / 攻撃 / 待機）
- 移動後攻撃
- ダメージ計算 `max(1, 攻撃 - 防御)`
- 敵AI（最寄りの味方へ接近して攻撃）
- 勝敗条件（敵全滅 / 味方全滅）

## API 実行方法
```bash
./mvnw spring-boot:run
# または mvn spring-boot:run
```

```bash
curl "http://localhost:8080/api/srpg/simulate?scenario=stage1"
```
