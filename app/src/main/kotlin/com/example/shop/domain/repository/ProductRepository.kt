package com.example.shop.domain.repository

import com.example.shop.db.ProductsTable
import com.example.shop.domain.model.Product
import com.example.shop.domain.model.asPlain
import com.example.shop.domain.model.asText
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

class ProductRepository {
    fun list(): List<Product> = transaction {
        ProductsTable.selectAll().orderBy(ProductsTable.id).map { it.toProduct() }
    }

    fun findById(id: Long): Product? = transaction {
        ProductsTable.selectAll().where { ProductsTable.id eq id }.singleOrNull()?.toProduct()
    }

    fun create(name: String, description: String, price: BigDecimal, stock: Int): Product = transaction {
        val id = ProductsTable.insert {
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.price] = price
            it[ProductsTable.stock] = stock
            it[ProductsTable.createdAt] = LocalDateTime.now()
            it[ProductsTable.updatedAt] = LocalDateTime.now()
        }[ProductsTable.id]
        findById(id)!!
    }

    fun update(id: Long, name: String, description: String, price: BigDecimal, stock: Int): Product? = transaction {
        val updated = ProductsTable.update({ ProductsTable.id eq id }) {
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.price] = price
            it[ProductsTable.stock] = stock
            it[ProductsTable.updatedAt] = LocalDateTime.now()
        }
        if (updated == 0) null else findById(id)
    }

    fun delete(id: Long): Boolean = transaction {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }

    fun fetchForOrder(productIds: List<Long>): List<ProductStockRow> = transaction {
        ProductsTable.selectAll().where { ProductsTable.id inList productIds }.forUpdate().map {
            ProductStockRow(
                id = it[ProductsTable.id],
                name = it[ProductsTable.name],
                price = it[ProductsTable.price],
                stock = it[ProductsTable.stock],
            )
        }
    }

    fun decreaseStock(productId: Long, quantity: Int) = transaction {
        val currentStock = ProductsTable
            .selectAll()
            .where { ProductsTable.id eq productId }
            .single()[ProductsTable.stock]

        ProductsTable.update({ ProductsTable.id eq productId }) {
            it[stock] = currentStock - quantity
            it[updatedAt] = LocalDateTime.now()
        }
    }

    private fun ResultRow.toProduct() = Product(
        id = this[ProductsTable.id],
        name = this[ProductsTable.name],
        description = this[ProductsTable.description],
        price = this[ProductsTable.price].asPlain(),
        stock = this[ProductsTable.stock],
        createdAt = this[ProductsTable.createdAt].asText(),
        updatedAt = this[ProductsTable.updatedAt].asText(),
    )
}

data class ProductStockRow(val id: Long, val name: String, val price: BigDecimal, val stock: Int)
