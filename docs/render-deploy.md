# Render 連携手順

このリポジトリは `render.yaml` を使った Blueprint デプロイに対応しています。

## 1. GitHub 側準備
- リポジトリ: `reitakigawa/MyHomePage`
- デプロイ対象ブランチ: `main`

## 2. Render で連携
1. Render ダッシュボードで **New +** → **Blueprint** を選択
2. GitHub を接続し、`reitakigawa/MyHomePage` を選択
3. `render.yaml` が自動検出されるのでそのまま作成

## 3. 設定内容（render.yaml）
- Runtime: `python`
- Build Command: `pip install -r requirements.txt`
- Start Command: `gunicorn app:app`
- Health Check: `/`
- Auto Deploy: `true`

## 4. 反映
- `main` ブランチに push すると自動デプロイされます。

## 5. トラブル時
- 起動失敗時は `requirements.txt` に `gunicorn` があるか確認
- `healthCheckPath` はアプリの `GET /` が `200` を返すことを確認
