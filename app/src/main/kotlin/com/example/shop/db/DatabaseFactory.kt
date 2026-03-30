package com.example.shop.db

import com.example.shop.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

object DatabaseFactory {
    private var dataSource: HikariDataSource? = null

    fun init(config: DatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            driverClassName = config.driverClassName
            username = config.user
            password = config.password
            maximumPoolSize = 10
            minimumIdle = 1
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            initializationFailTimeout = -1
        }

        dataSource = HikariDataSource(hikariConfig)

        repeat(20) { attempt ->
            try {
                Database.connect(dataSource!!)
                dataSource!!.connection.use { }
                createSchema()
                return
            } catch (e: SQLException) {
                if (attempt == 19) throw e
                Thread.sleep(3000)
            }
        }
    }

    private fun createSchema() {
        transaction {
            SchemaUtils.create(
                UsersTable,
                ProductsTable,
                OrdersTable,
                OrderItemsTable,
                AuditLogsTable
            )
        }
    }

    fun close() {
        dataSource?.close()
    }
}