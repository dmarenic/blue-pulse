package com.dominik.bluepuls.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dominik.bluepuls.R
import androidx.compose.ui.res.stringResource
import com.dominik.bluepuls.ui.theme.SurfaceAccent
import com.dominik.bluepuls.ui.theme.SurfaceBlue01
import com.dominik.bluepuls.ui.theme.SurfaceBlue03
import com.dominik.bluepuls.ui.theme.SurfaceWhite
import com.dominik.bluepuls.ui.theme.SurfaceWhiteA50

/**
 * Zajednička forma za prijavu i registraciju.
 * Login i Register ekran su prije bili ~95% identični - sada oba koriste ovu komponentu
 * pa nema dupliciranja koda.
 */
@Composable
fun AuthForm(
    submitLabel: String,
    bottomLinkLabel: String,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: (name: String, email: String, password: String, confirmPassword: String) -> Unit,
    onBottomLinkClick: () -> Unit,
    isRegister: Boolean = false
) {
    // rememberSaveable -> unos preživi rotaciju / povratak iz pozadine.
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SurfaceWhite,
        unfocusedTextColor = SurfaceWhite,
        focusedBorderColor = SurfaceAccent,
        unfocusedBorderColor = SurfaceBlue01
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBlue03)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            color = SurfaceWhite,
            style = MaterialTheme.typography.displayMedium // Tektur - sportski display font
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Ime - samo pri registraciji.
        if (isRegister) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.auth_name), color = SurfaceWhiteA50) },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email), color = SurfaceWhiteA50) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password), color = SurfaceWhiteA50) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        // Potvrda lozinke - samo pri registraciji (isti stil kao ostala polja).
        if (isRegister) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.auth_confirm_password), color = SurfaceWhiteA50) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onSubmit(name, email, password, confirmPassword) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBlue01),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = SurfaceAccent, modifier = Modifier.size(24.dp))
            } else {
                Text(submitLabel, color = SurfaceWhite)
            }
        }

        TextButton(onClick = onBottomLinkClick) {
            Text(bottomLinkLabel, color = SurfaceAccent)
        }
    }
}
