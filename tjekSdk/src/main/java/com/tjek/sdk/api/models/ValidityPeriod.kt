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
import com.tjek.sdk.api.ValidityDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class ValidityPeriod(
    override val start: ValidityDate,
    override val endInclusive: ValidityDate
) : Iterable<ValidityDate>, ClosedRange<ValidityDate>, Parcelable {

    constructor(parcel: Parcel) : this(
        ValidityDate.of(LocalDateTime.ofEpochSecond(parcel.readLong(), 0, ZoneOffset.UTC), ZoneOffset.UTC),
        ValidityDate.of(LocalDateTime.ofEpochSecond(parcel.readLong(), 0, ZoneOffset.UTC), ZoneOffset.UTC)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(start.toEpochSecond())
        parcel.writeLong(endInclusive.toEpochSecond())
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

    override fun iterator(): Iterator<ValidityDate> {
        return ValidityDateIterator(start, endInclusive, 1)
    }

    override fun toString(): String {
        return "ValidityPeriod(start=$start, endInclusive=$endInclusive)"
    }

}

operator fun ValidityDate.rangeTo(other: ValidityDate) = ValidityPeriod(this, other)

class ValidityDateIterator(
    startDate: ValidityDate,
    private val endDateInclusive: ValidityDate,
    private val stepDays: Long): Iterator<ValidityDate> {

    private var currentDate = startDate

    override fun hasNext() = currentDate.plusDays(stepDays) <= endDateInclusive

    override fun next(): ValidityDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}