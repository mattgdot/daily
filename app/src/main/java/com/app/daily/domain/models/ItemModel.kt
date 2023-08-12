package com.app.daily.domain.models

data class ItemModel(
    val id: String = "",
    val name: String = "",
    var checked: Boolean = false
)