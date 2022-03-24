package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model

object Languages {
    fun supportedLanguages(): Map<String, String> {
        return mapOf(
            Pair("English", "en"),
            Pair("Català", "ca"),
            Pair("Deutsch", "de"),
            Pair("Ελληνικά", "el"),
            Pair("Español", "es"),
            Pair("Vasco", "eu"),
            Pair("فارسی", "fa"),
            Pair("Français", "fr"),
            Pair("Italiano", "it"),
            Pair("日本語", "ja"),
            Pair("Polski", "pl"),
            Pair("Português", "pt"),
            Pair("Português (Brasil)", "pt-rBR"),
            Pair("Pусский", "ru"),
            Pair("Türkçe", "tr"),
            Pair("中文 (简体中文)", "zh"), // Simplified Chinese
            Pair("中文 (简体中文,中国)", "zh-rCN"), // Simplified Chinese
            Pair("中文 (简体中文,新加坡)", "zh-rSG"), // Simplified Chinese
            Pair("中文 (繁体中文,香港)", "zh-rHK"), // Traditional Chinese
            Pair("中文 (繁体中文,台湾)", "zh-rTW") // Traditional Chinese
        )
    }
}