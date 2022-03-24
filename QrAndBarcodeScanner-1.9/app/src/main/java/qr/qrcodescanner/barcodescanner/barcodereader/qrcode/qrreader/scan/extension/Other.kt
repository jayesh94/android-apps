package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
