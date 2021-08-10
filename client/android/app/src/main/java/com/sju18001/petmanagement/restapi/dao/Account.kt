package com.sju18001.petmanagement.restapi.dao

data class Account(
    var id: Long,
    val username: String,
    val email: String,
    val phone: String,
    val marketing: Boolean?,
    val nickname: String?,
    val photoUrl: String?,
    val userMessage: String?
)