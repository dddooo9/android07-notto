package com.gojol.notto.util

import java.util.Calendar

fun Calendar.toYearMonth(): String {
    return "${this.get(Calendar.YEAR)}년 ${this.get(Calendar.MONTH) + 1}월"
}

fun Calendar.toYearMonthDate(): String {
    return "${this.get(Calendar.YEAR)}${this.get(Calendar.MONTH) + 1}${this.get(Calendar.DATE)}"
}

fun Calendar.getYear(): Int {
    return this.get(Calendar.YEAR)
}

fun Calendar.getMonth(): Int {
    return this.get(Calendar.MONTH)
}

fun Calendar.getDate(): Int {
    return this.get(Calendar.DATE)
}

fun Calendar.getDayOfWeek(): Int {
    return this.get(Calendar.DAY_OF_WEEK)
}

fun Calendar.getFirstDayOfMonth(): Int {
    return this.get(Calendar.DAY_OF_MONTH)
}

fun Calendar.getLastDayOfMonth(): Int {
    return this.getActualMaximum(Calendar.DATE)
}
