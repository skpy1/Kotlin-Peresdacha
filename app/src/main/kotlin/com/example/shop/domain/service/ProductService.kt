package com.example.shop.domain.service

import com.example.shop.domain.model.CreateProductRequest
import com.example.shop.domain.model.Product
import com.example.shop.domain.model.UpdateProductRequest
import com.example.shop.domain.repository.ProductRepository
import com.example.shop.util.NotFoundException
import com.example.shop.util.ValidationException
import java.math.BigDecimal

class ProductService(private val productRepository: ProductRepository) {
    fun list(): List<Product> = productRepository.list()
    fun get(id: Long): Product = productRepository.findById(id) ?: throw NotFoundException("Product $id not found")

    fun create(request: CreateProductRequest): Product {
        validate(request.name, request.stock, request.price)
        return productRepository.create(request.name.trim(), request.description.trim(), request.price.toBigDecimal(), request.stock)
    }

    fun update(id: Long, request: UpdateProductRequest): Product {
        validate(request.name, request.stock, request.price)
        return productRepository.update(id, request.name.trim(), request.description.trim(), request.price.toBigDecimal(), request.stock)
            ?: throw NotFoundException("Product $id not found")
    }

    fun delete(id: Long) {
        if (!productRepository.delete(id)) throw NotFoundException("Product $id not found")
    }

    private fun validate(name: String, stock: Int, price: String) {
        if (name.isBlank()) throw ValidationException("Product name is required")
        if (stock < 0) throw ValidationException("Stock cannot be negative")
        if (BigDecimal(price) <= BigDecimal.ZERO) throw ValidationException("Price must be greater than zero")
    }
}
