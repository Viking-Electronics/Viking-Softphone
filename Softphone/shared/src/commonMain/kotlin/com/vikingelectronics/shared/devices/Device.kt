package com.vikingelectronics.shared.devices

import com.vikingelectronics.shared.activity.ActivityEntry
import dev.icerock.moko.parcelize.IgnoredOnParcel
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlin.jvm.Transient

@Parcelize
data class Device(
    val id: String = "",
    val name: String = "",
    val callAddress: String = "",
): Parcelable {
    @IgnoredOnParcel
    @Transient
    var latestActivityEntry: ActivityEntry? = null
}
