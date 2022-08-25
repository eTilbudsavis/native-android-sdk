package com.tjek.sdk.api.models

import com.digidemic.unitof.UnitOf
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
}

fun QuantityUnit.isMass(): Boolean = when(this) {
    QuantityUnit.Microgram, QuantityUnit.Milligram, QuantityUnit.Centigram, QuantityUnit.Decigram,
    QuantityUnit.Gram, QuantityUnit.Kilogram, QuantityUnit.Tonne, QuantityUnit.UsTon, QuantityUnit.ImperialTon,
    QuantityUnit.Stone, QuantityUnit.Pound, QuantityUnit.Ounce
    -> true
    else -> false
}

fun QuantityUnit.isVolume(): Boolean = when(this) {
    QuantityUnit.Milliliter, QuantityUnit.Centiliter, QuantityUnit.Deciliter, QuantityUnit.Liter, QuantityUnit.Kiloliter, QuantityUnit.Megaliter,
    QuantityUnit.CubicMeter, QuantityUnit.UsTeaspoon, QuantityUnit.UsTablespoon, QuantityUnit.UsFluidOunce, QuantityUnit.UsCup,
    QuantityUnit.UsPint, QuantityUnit.UsQuart, QuantityUnit.UsGallon, QuantityUnit.ImperialTeaspoon, QuantityUnit.ImperialTablespoon,
    QuantityUnit.ImperialFluidOunce, QuantityUnit.ImperialPint, QuantityUnit.ImperialQuart, QuantityUnit.ImperialGallon,
    QuantityUnit.CubicInch, QuantityUnit.CubicFoot
    -> true
    else -> false
}

// The unit we convert to when expressing price per unit (eg. 30kr/100g -> 300kr/kg)
fun QuantityUnit.getPricePerUnit(): QuantityUnit = when(this) {

    QuantityUnit.Microgram, QuantityUnit.Milligram, QuantityUnit.Centigram, QuantityUnit.Decigram,
    QuantityUnit.Gram, QuantityUnit.Kilogram, QuantityUnit.Tonne
    -> QuantityUnit.Kilogram

    QuantityUnit.UsTon, QuantityUnit.ImperialTon, QuantityUnit.Stone, QuantityUnit.Pound, QuantityUnit.Ounce
    -> QuantityUnit.Pound

    QuantityUnit.Milliliter, QuantityUnit.Centiliter, QuantityUnit.Deciliter, QuantityUnit.Liter,
    QuantityUnit.Kiloliter, QuantityUnit.Megaliter, QuantityUnit.CubicMeter
    -> QuantityUnit.Liter

    QuantityUnit.UsCup, QuantityUnit.UsPint, QuantityUnit.UsQuart, QuantityUnit.UsGallon, QuantityUnit.CubicFoot
    -> QuantityUnit.UsQuart

    QuantityUnit.UsTeaspoon, QuantityUnit.UsTablespoon, QuantityUnit.UsFluidOunce, QuantityUnit.CubicInch
    -> QuantityUnit.UsFluidOunce

    QuantityUnit.ImperialTeaspoon, QuantityUnit.ImperialTablespoon, QuantityUnit.ImperialFluidOunce
    -> QuantityUnit.ImperialFluidOunce

    QuantityUnit.ImperialPint, QuantityUnit.ImperialQuart, QuantityUnit.ImperialGallon
    -> QuantityUnit.ImperialQuart

    QuantityUnit.Piece -> QuantityUnit.Piece
}

fun QuantityUnit.getPricePerUnitScalingFactor(): Double = when(this) {

    QuantityUnit.Microgram -> UnitOf.Mass().fromMicrograms(1.0).toKilograms()
    QuantityUnit.Milligram -> UnitOf.Mass().fromMilligrams(1.0).toKilograms()
    QuantityUnit.Centigram -> UnitOf.Mass().fromCentigrams(1.0).toKilograms()
    QuantityUnit.Decigram -> UnitOf.Mass().fromDecigrams(1.0).toKilograms()
    QuantityUnit.Gram -> UnitOf.Mass().fromGrams(1.0).toKilograms()
    QuantityUnit.Tonne -> UnitOf.Mass().fromTonsMetric(1.0).toKilograms()
    QuantityUnit.Kilogram -> 1.0

    QuantityUnit.UsTon -> UnitOf.Mass().fromTonsUS(1.0).toPounds()
    QuantityUnit.ImperialTon -> UnitOf.Mass().fromTonsImperial(1.0).toPounds()
    QuantityUnit.Stone -> UnitOf.Mass().fromStonesUK(1.0).toPounds()
    QuantityUnit.Ounce -> UnitOf.Mass().fromOuncesUS(1.0).toPounds()
    QuantityUnit.Pound -> 1.0

    QuantityUnit.Milliliter -> UnitOf.Volume().fromMilliliters(1.0).toLiters()
    QuantityUnit.Centiliter -> UnitOf.Volume().fromCentiliters(1.0).toLiters()
    QuantityUnit.Deciliter -> UnitOf.Volume().fromDeciliters(1.0).toLiters()
    QuantityUnit.Kiloliter -> UnitOf.Volume().fromKiloliters(1.0).toLiters()
    QuantityUnit.Megaliter -> UnitOf.Volume().fromMegaliters(1.0).toLiters()
    QuantityUnit.CubicMeter -> UnitOf.Volume().fromCubicMeters(1.0).toLiters()
    QuantityUnit.Liter -> 1.0

    QuantityUnit.UsCup -> UnitOf.Volume().fromCupsUS(1.0).toQuartsUS()
    QuantityUnit.UsPint -> UnitOf.Volume().fromPintsUS(1.0).toQuartsUS()
    QuantityUnit.UsGallon -> UnitOf.Volume().fromGallonsUS(1.0).toQuartsUS()
    QuantityUnit.CubicFoot -> UnitOf.Volume().fromCubicFeet(1.0).toQuartsUS()
    QuantityUnit.UsQuart -> 1.0

    QuantityUnit.UsTeaspoon -> UnitOf.Volume().fromTeaspoonsUS(1.0).toFluidOuncesUS()
    QuantityUnit.UsTablespoon -> UnitOf.Volume().fromTablespoonsUS(1.0).toFluidOuncesUS()
    QuantityUnit.CubicInch -> UnitOf.Volume().fromCubicInches(1.0).toFluidOuncesUS()
    QuantityUnit.UsFluidOunce -> 1.0

    QuantityUnit.ImperialTeaspoon -> UnitOf.Volume().fromTeaspoonsUK(1.0).toFluidOuncesUK()
    QuantityUnit.ImperialTablespoon -> UnitOf.Volume().fromTablespoonsUK(1.0).toFluidOuncesUK()
    QuantityUnit.ImperialFluidOunce -> 1.0

    QuantityUnit.ImperialPint -> UnitOf.Volume().fromPintsUK(1.0).toQuartsUK()
    QuantityUnit.ImperialGallon -> UnitOf.Volume().fromGallonsUK(1.0).toQuartsUK()
    QuantityUnit.ImperialQuart -> 1.0

    QuantityUnit.Piece -> 1.0
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