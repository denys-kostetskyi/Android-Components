package com.denyskostetskyi.androidcomponents.user

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
) {
    companion object {
        const val UNDEFINED_ID = 0
    }
}