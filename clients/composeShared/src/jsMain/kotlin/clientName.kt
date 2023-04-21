
actual val clientName: String
        = "Ese Web"
actual val platform: Platform
     =Platform.Web
actual val platformInitMessage: String="""
    この${clientName}は一部機能が制限されており、バグもあります。
    既知の問題
      - スクロール不可
      - IMEの使用(日本語入力)不可
      - 自動改行されない
""".trimIndent()