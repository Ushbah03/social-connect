package com.example.socialconnect1


data class ProductResponse(
    val products: List<Product>
)

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnail: String
)
