package com.app.daily.domain.models

data class ListModel(
    val id: String = "",
    val name: String = "",
    val timestamp: Long = 0,
    val priority:Int = 0,
    val content: List<ItemModel> = listOf()
)