package info.ascetx.flashlight.helper.remoteConfigs

data class InAppUpdater(
    val daysForFlexibleUpdate: Int,
    val priorityThresholdFlexibleUpdate: Int,
    val priorityThresholdImmediateUpdate: Int
)