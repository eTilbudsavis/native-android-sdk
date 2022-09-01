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

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.util.*

enum class QuantityUnit(val unit: String, val symbol: String) {

    // mass (base unit: grams)
    Microgram("microgram", "μg"), // 0.000001 g
    Milligram("milligram", "mg"), // 0.001 g
    Centigram("centigram", "cg"), // 0.01 g
    Decigram ("decigram", "dg"),  // 0.1 g
    Gram     ("gram", "g"),      // 1 g
    Kilogram ("kilogram", "kg"),  // 1_000 g
    Tonne    ("tonne", "t"),     // 1_000_000 g

    ImperialTon("imperial_ton", "t"), // 1016050 g
    UsTon      ("us_ton", "t"),       // 907185 g
    Stone      ("stone", "st"),        // 6350.29 g
    Pound      ("pound", "lb"),        // 453.592 g
    Ounce      ("ounce", "oz"),        // 28.3495 g

    // volume (base unit: liters)
    Milliliter("milliliter", "mL"),  // 0.001 l
    Centiliter("centiliter", "cL"),  // 0.01 l
    Deciliter ("deciliter", "dL"),   // 0.1 l
    Liter     ("liter", "L"),       // 1 l
    Kiloliter ("kiloliter", "kL"),   // 1_000 l
    CubicMeter("cubic_meter", "m\u00B3"), // 1_000 l
    Megaliter ("megaliter", "ML"),   // 1_000_000 l

    UsTeaspoon  ("us_teaspoon", "tsp"),    // 0.00492892 l
    UsTablespoon("us_tablespoon", "tbl"),  // 0.0147868 l
    UsFluidOunce("us_fluid_ounce", "fl oz"), // 0.0295735 l
    UsCup       ("us_cup", "c"),         // 0.24 l
    UsPint      ("us_pint", "pt"),        // 0.473176 l
    UsQuart     ("us_quart", "qt"),       // 0.946353 l
    UsGallon    ("us_gallon", "gal"),      // 3.78541 l

    ImperialTeaspoon  ("imperial_teaspoon", "tsp"),    // 0.00591939 l
    ImperialTablespoon("imperial_tablespoon", "tbl"),  // 0.0177582 l
    ImperialFluidOunce("imperial_fluid_ounce", "fl oz"), // 0.0284131 l
    ImperialPint      ("imperial_pint", "pt"),        // 0.568261 l
    ImperialQuart     ("imperial_quart", "qt"),       // 1.13652 l
    ImperialGallon    ("imperial_gallon", "gal"),      // 4.54609 l

    CubicInch("cubic_inch", "in\u00B3"), // 0.0163871 l
    CubicFoot("cubic_foot", "ft\u00B3"), // 28.3168 l

    Piece("piece", "pcs");

    companion object {
        fun fromSymbol(symbol: String): QuantityUnit {
            return values().find { it.symbol.lowercase(Locale.ENGLISH) == symbol.lowercase(Locale.ENGLISH) } ?: Piece
        }
    }


    fun isMass(): Boolean = when(this) {
        Microgram, Milligram, Centigram, Decigram,
        Gram, Kilogram, Tonne, UsTon, ImperialTon,
        Stone, Pound, Ounce
        -> true
        else -> false
    }

    fun isVolume(): Boolean = when(this) {
        Milliliter, Centiliter, Deciliter, Liter, Kiloliter, Megaliter,
        CubicMeter, UsTeaspoon, UsTablespoon, UsFluidOunce, UsCup,
        UsPint, UsQuart, UsGallon, ImperialTeaspoon, ImperialTablespoon,
        ImperialFluidOunce, ImperialPint, ImperialQuart, ImperialGallon,
        CubicInch, CubicFoot
        -> true
        else -> false
    }    
}

// Derive from JsonAdapter in a more complicated way to support null
class QuantityUnitAdapter : JsonAdapter<QuantityUnit>() {

    override fun toJson(writer: JsonWriter, value: QuantityUnit?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.unit)
        }
    }

    override fun fromJson(reader: JsonReader): QuantityUnit? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        val type = reader.nextString()
        return QuantityUnit.values().find { it.unit == type } ?: QuantityUnit.Piece
    }
}