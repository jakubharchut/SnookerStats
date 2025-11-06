package com.example.snookerstats.domain.use_case

import com.example.snookerstats.domain.model.Response
import javax.inject.Inject

class ValidateRegisterInputUseCase @Inject constructor() {
    operator fun invoke(email: String, password: String, confirmPassword: String): Response<Unit> {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            return Response.Error("Wszystkie pola muszą być wypełnione.")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Response.Error("Nieprawidłowy format adresu e-mail.")
        }
        if (password != confirmPassword) {
            return Response.Error("Hasła nie są zgodne.")
        }
        if (password.length < 6) {
            return Response.Error("Hasło musi mieć co najmniej 6 znaków.")
        }
        return Response.Success(Unit)
    }
}
