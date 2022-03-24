package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.remoteConfigs

data class InAppUpdater(
    val daysForFlexibleUpdate: Int,
    val priorityThresholdFlexibleUpdate: Int,
    val priorityThresholdImmediateUpdate: Int
)