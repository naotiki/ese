
actual val clientName: String
        = "Ese Client-WASM"
actual val clientPlatform: ClientPlatform
     =ClientPlatform.JS
actual val platformInitMessage: String="""
    この${clientName}は一部機能が制限されており、バグもあります。
    既知の問題
      - スクロール不可
      - IMEの使用(日本語入力)不可
      - 自動改行されない
""".trimIndent()