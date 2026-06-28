package com.dominik.bluepuls.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.dominik.bluepuls.R
import com.dominik.bluepuls.presentation.AuthViewModel
import com.dominik.bluepuls.presentation.components.AuthForm

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    AuthForm(
        submitLabel = stringResource(R.string.auth_login),
        bottomLinkLabel = stringResource(R.string.auth_to_register),
        isLoading = isLoading,
        errorMessage = errorMessage,
        onSubmit = { _, email, password, _ -> viewModel.login(email, password) },
        onBottomLinkClick = onNavigateToRegister
    )
}
