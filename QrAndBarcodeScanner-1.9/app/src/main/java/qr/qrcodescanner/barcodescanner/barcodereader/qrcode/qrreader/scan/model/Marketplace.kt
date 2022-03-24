package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model

enum class Marketplace(val templateUrl: String) {
    AMAZON_COM("https://www.amazon.com/s?k="),
    EBAY_COM("https://www.ebay.com/sch/i.html?_nkw="),
    OPEN_FOOD("https://world.openfoodfacts.org/product/"),
    AMAZON_IN("https://www.amazon.in/s?k="),
    FLIPKART_COM("https://www.flipkart.com/search?q=")
}