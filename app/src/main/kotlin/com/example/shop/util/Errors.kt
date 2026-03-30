package com.example.shop.util

open class AppException(message: String) : RuntimeException(message)
class ValidationException(message: String) : AppException(message)
class UnauthorizedException(message: String = "Unauthorized") : AppException(message)
class ForbiddenException(message: String = "Forbidden") : AppException(message)
class NotFoundException(message: String) : AppException(message)
class ConflictException(message: String) : AppException(message)
class BusinessException(message: String) : AppException(message)
