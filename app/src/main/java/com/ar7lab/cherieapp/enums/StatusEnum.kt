package com.ar7lab.cherieapp.enums

/**
 * Define enum class for status of the item
 */
enum class StatusEnum(val type: String) {
    ON_SELL("ON SELL"),
    SOLD_OUT("SOLD OUT"),
    START_SOON("START SOON"),
    OWNED("OWNED"),
    RESELL("RE SELL")
}