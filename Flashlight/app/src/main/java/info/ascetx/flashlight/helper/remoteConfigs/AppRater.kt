package info.ascetx.flashlight.helper.remoteConfigs

data class AppRater(
    val appLaunchesUntilPrompt: Int,
    val cancelExtensionDays: Int,
    val daysUntilNextPrompt: Int,
    val daysUntilPrompt: Int,
    val mayBeLaterExtensionDays: Int,
    val openPlayStoreExtensionDays: Int,
    val submitFormExtensionDays: Int,
    val playStoreRatingThreshold: Float
)