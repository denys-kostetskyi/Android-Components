package com.denyskostetskyi.androidcomponents.user

class UserDao {
    private val users = mutableListOf(
        User(1, "John", "Doe"),
        User(2, "Jane", "Smith"),
        User(3, "Alice", "Johnson")
    )

    fun getUsers(): List<User> = users

    fun getUserById(id: Int): User? = users.find { it.id == id }

    fun addUser(user: User): Boolean {
        return if (users.none { it.id == user.id }) {
            users.add(user)
            true
        } else {
            false
        }
    }

    fun deleteUserById(id: Int): Boolean {
        return users.removeIf { it.id == id }
    }

    fun updateUser(user: User): Boolean {
        val index = users.indexOfFirst { it.id == user.id }
        return if (index >= 0) {
            users[index] = user
            true
        } else {
            false
        }
    }
}
