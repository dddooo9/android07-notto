package com.gojol.notto.ui.home

data class DateWithFocus(
    val date: String,
    val successCount: Int,
    val isChecked: Boolean = false
)