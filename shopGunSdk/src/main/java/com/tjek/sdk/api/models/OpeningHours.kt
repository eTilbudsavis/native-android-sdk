package com.tjek.sdk.api.models

import android.os.Parcel
import android.os.Parcelable
import com.tjek.sdk.api.TimeOfDay
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime

sealed class OpeningHours : Parcelable {

    @Parcelize
    // normal opening hours
    data class OpenDay(val dayOfWeek: DayOfWeek, val dailyHours: OpenHour): OpeningHours()

    @Parcelize
    // holiday hours (e.g. reduced opening hours)
    data class DateRangeOpen(val dateRange: OpeningHoursDateRange, val dailyHours: OpenHour): OpeningHours()

    @Parcelize
    // holiday closed date
    data class DateRangeClosed(val dateRange: OpeningHoursDateRange): OpeningHours()

    @Parcelize
    // closing day
    data class ClosedDay(val dayOfWeek: DayOfWeek): OpeningHours()
}

data class OpenHour(
    val opens: TimeOfDay,
    val closes: TimeOfDay
) : Parcelable {
    constructor(parcel: Parcel) : this(
        LocalTime.ofNanoOfDay(parcel.readLong()),
        LocalTime.ofNanoOfDay(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(opens.toNanoOfDay())
        parcel.writeLong(closes.toNanoOfDay())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OpenHour> {
        override fun createFromParcel(parcel: Parcel): OpenHour {
            return OpenHour(parcel)
        }

        override fun newArray(size: Int): Array<OpenHour?> {
            return arrayOfNulls(size)
        }
    }
}