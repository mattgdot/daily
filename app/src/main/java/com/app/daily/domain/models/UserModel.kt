package com.app.daily.domain.models

data class UserModel(
    val id: String = "",
    val name: String = "",
    val lists: MutableList<String> = mutableListOf()
)