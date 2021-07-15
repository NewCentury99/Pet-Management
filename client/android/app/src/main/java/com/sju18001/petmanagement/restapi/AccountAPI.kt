package com.sju18001.petmanagement.restapi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AccountAPI {
    @POST("api/account/login")
    fun signInRequest(@Body accountSignInRequestDTO: AccountSignInRequestDTO): Call<AccountSignInResponseDTO>
}