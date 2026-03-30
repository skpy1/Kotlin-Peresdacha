package com.example.shop.domain.repository

import com.example.shop.db.UsersTable
import com.example.shop.domain.model.User
import com.example.shop.domain.model.UserRole
import com.example.shop.domain.model.asText
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {
    fun create(email: String, passwordHash: String, fullName: String, role: UserRole): User = transaction {
        val id = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.fullName] = fullName
            it[UsersTable.role] = role.name
        }[UsersTable.id]
        findById(id)!!
    }

    fun findByEmail(email: String): UserRow? = transaction {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()?.toUserRow()
    }

    fun findById(id: Long): User? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull()?.toUser()
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        fullName = this[UsersTable.fullName],
        role = UserRole.valueOf(this[UsersTable.role]),
        createdAt = this[UsersTable.createdAt].asText(),
    )

    private fun ResultRow.toUserRow() = UserRow(
        user = toUser(),
        passwordHash = this[UsersTable.passwordHash],
    )
}

data class UserRow(val user: User, val passwordHash: String)
