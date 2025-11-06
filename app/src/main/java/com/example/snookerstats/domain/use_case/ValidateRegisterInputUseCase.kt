package com.example.snookerstats.domain.use_case

import com.example.snookerstats.domain.model.Response
import java.util.regex.Pattern
import javax.inject.Inject

class ValidateRegisterInputUseCase @Inject constructor() {

    // Standardowy, sprawdzony Regex do walidacji e-maili
    private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    operator fun invoke(email: String, password: String, confirmPassword: String): Response<Unit> {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || confirmPassword.isBlank()) {
            return Response.Error("Wszystkie pola muszą być wypełnione.")
        }
        if (!EMAIL_ADDRESS_PATTERN.matcher(trimmedEmail).matches()) {
            return Response.Error("Nieprawidłowy format adresu e-mail.")
        }
        if (trimmedPassword != confirmPassword.trim()) {
            return Response.Error("Hasła nie są zgodne.")
        }
        if (trimmedPassword.length < 6) {
            return Response.Error("Hasło musi mieć co najmniej 6 znaków.")
        }
        return Response.Success(Unit)
    }
}
