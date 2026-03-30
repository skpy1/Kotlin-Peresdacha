package com.example.shop.domain.repository

import com.example.shop.db.AuditLogsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class AuditLogRepository {
    fun write(userId: Long?, action: String, payload: String) = transaction {
        AuditLogsTable.insert {
            it[AuditLogsTable.userId] = userId
            it[AuditLogsTable.action] = action
            it[AuditLogsTable.payload] = payload
        }
    }
}
