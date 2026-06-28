package com.dominik.bluepuls.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.dominik.bluepuls.R
import com.dominik.bluepuls.presentation.AuthViewModel
import com.dominik.bluepuls.presentation.components.AuthForm

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    AuthForm(
        submitLabel = stringResource(R.string.auth_register),
        bottomLinkLabel = stringResource(R.string.auth_to_login),
        isLoading = isLoading,
        errorMessage = errorMessage,
        onSubmit = { name, email, password, confirmPassword ->
            viewModel.register(name, email, password, confirmPassword)
        },
        onBottomLinkClick = onNavigateToLogin,
        isRegister = true
    )
}
