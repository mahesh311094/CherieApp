package com.ar7lab.cherieapp.utils

import com.github.mikephil.charting.formatter.ValueFormatter
//float to value string converter
class DateValueFormatter: ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return "2021-9-10"
    }
}