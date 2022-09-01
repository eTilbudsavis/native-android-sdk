package com.tjek.sdk.api.models
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Parcel
import android.os.Parcelable
import com.tjek.sdk.api.OpeningHoursDate

class OpeningHoursDateRange(
    override val start: OpeningHoursDate,
    override val endInclusive: OpeningHoursDate
) : Iterable<OpeningHoursDate>, ClosedRange<OpeningHoursDate>, Parcelable {

    constructor(parcel: Parcel) : this(
        OpeningHoursDate.ofEpochDay(parcel.readLong()),
        OpeningHoursDate.ofEpochDay(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(start.toEpochDay())
        parcel.writeLong(endInclusive.toEpochDay())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ValidityPeriod> {
        override fun createFromParcel(parcel: Parcel): ValidityPeriod {
            return ValidityPeriod(parcel)
        }

        override fun newArray(size: Int): Array<ValidityPeriod?> {
            return arrayOfNulls(size)
        }
    }

    override fun iterator(): Iterator<OpeningHoursDate> {
        return OpeningHoursDateIterator(start, endInclusive, 1)
    }

}

operator fun OpeningHoursDate.rangeTo(other: OpeningHoursDate) = OpeningHoursDateRange(this, other)

class OpeningHoursDateIterator(
    startDate: OpeningHoursDate,
    private val endDateInclusive: OpeningHoursDate,
    private val stepDays: Long): Iterator<OpeningHoursDate> {

    private var currentDate = startDate

    override fun hasNext() = currentDate.plusDays(stepDays) <= endDateInclusive

    override fun next(): OpeningHoursDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}