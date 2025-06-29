# 自転車天気予報アプリ

このアプリは、OpenWeatherMap APIを使用して地域の天気情報を取得し、自転車に乗るのに適した日を7日先まで教えてくれるAndroidアプリです。

## 機能

- **7日間の天気予報**: 指定した地域の7日間の天気情報を表示
- **自転車適性判定**: 気温、降水確率、風速、天気を考慮して自転車に適しているかどうかを判定
- **地域検索**: ユーザーが任意の地域を指定して天気情報を取得可能
- **現在地取得**: GPSを使用して現在地の天気情報を取得（デフォルトは横浜市）
- **統計情報**: 7日間の自転車適性率を表示

## 自転車適性の判定基準

- **気温**: 10°C〜30°Cの範囲
- **降水確率**: 30%未満
- **風速**: 10m/s未満
- **天気**: 雨、雪、雷などの悪天候を除外

## セットアップ

### 1. OpenWeatherMap APIキーの取得

1. [OpenWeatherMap](https://openweathermap.org/)にアクセス
2. アカウントを作成
3. APIキーを取得
4. `app/src/main/java/com/example/mybestzitendate/repository/WeatherRepository.kt`の`apiKey`変数を実際のAPIキーに置き換え

```kotlin
private val apiKey = "YOUR_ACTUAL_API_KEY_HERE"
```

### 2. 必要な権限

アプリは以下の権限を要求します：
- `INTERNET`: 天気APIへのアクセス
- `ACCESS_FINE_LOCATION`: 現在地の取得
- `ACCESS_COARSE_LOCATION`: 現在地の取得

### 3. ビルドと実行

```bash
# プロジェクトをクローン
git clone <repository-url>
cd MyBestzitendate

# アプリをビルド
./gradlew build

# アプリを実行
./gradlew installDebug
```

## 使用方法

1. アプリを起動すると、デフォルトで横浜市の天気情報が表示されます
2. 「地域変更」ボタンをタップして任意の地域を検索できます
3. 各地域の天気情報がカード形式で表示され、自転車に適しているかどうかが色分けされています
4. 緑色のカードは自転車に適した日、オレンジ色のカードは自転車に適していない日を示します

## 技術スタック

- **言語**: Kotlin
- **UI**: Jetpack Compose
- **アーキテクチャ**: MVVM
- **ネットワーク**: Retrofit + OkHttp
- **位置情報**: Google Play Services Location
- **画像読み込み**: Coil
- **状態管理**: StateFlow

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## 注意事項

- OpenWeatherMap APIの無料プランには1分間に60回のリクエスト制限があります
- 位置情報の取得にはユーザーの許可が必要です
- インターネット接続が必要です 