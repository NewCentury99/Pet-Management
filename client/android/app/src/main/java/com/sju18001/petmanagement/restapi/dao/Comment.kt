package com.sju18001.petmanagement.restapi.dao

data class Comment(
    val id: Long,
    val author: Account,
    val postId: Long?,
    val parentCommentId: Long?,
    var contents: String,
    val timestamp: String,
    val edited: Boolean
)
