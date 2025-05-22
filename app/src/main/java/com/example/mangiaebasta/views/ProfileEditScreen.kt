package com.example.mangiaebasta.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mangiaebasta.models.Profile
import com.example.mangiaebasta.viewmodels.ProfileViewModel
import com.example.mangiaebasta.Screen

@Composable
fun ProfileEditScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val profile by profileViewModel.profile.collectAsState(initial = null)
    val isLoading by profileViewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading || profile == null) {
                LoadingScreen()
            } else {
                val currentProfile = profile!!

                // Gestione dei valori nulli dal profilo
                var firstName by remember { mutableStateOf(currentProfile.firstName ?: "") }
                var lastName by remember { mutableStateOf(currentProfile.lastName ?: "") }
                var cardFullName by remember { mutableStateOf(currentProfile.cardFullName ?: "") }
                var cardNumber by remember { mutableStateOf(currentProfile.cardNumber?.toString() ?: "") }
                var expireMonth by remember { mutableStateOf(currentProfile.cardExpireMonth?.toString() ?: "") }
                var expireYear by remember { mutableStateOf(currentProfile.cardExpireYear?.toString() ?: "") }
                var cvv by remember { mutableStateOf(currentProfile.cardCVV?.toString() ?: "") }

                // Funzione per accettare solo caratteri numerici
                fun onlyDigits(input: String, maxLen: Int? = null): String {
                    val digits = input.filter { it.isDigit() }
                    return maxLen?.let { digits.take(it) } ?: digits
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sezione dati personali
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Cognome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Sezione dati carta
                    OutlinedTextField(
                        value = cardFullName,
                        onValueChange = { cardFullName = it },
                        label = { Text("Intestatario carta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = onlyDigits(it) },
                        label = { Text("Numero carta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = expireMonth,
                            onValueChange = { expireMonth = onlyDigits(it, 2) },
                            label = { Text("Mese scadenza") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expireYear,
                            onValueChange = { expireYear = onlyDigits(it, 4) },
                            label = { Text("Anno scadenza") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = onlyDigits(it, 3) },
                        label = { Text("CVV") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // Crea un nuovo profilo con i dati aggiornati
                            val updatedProfile = Profile(
                                firstName = firstName.takeIf { it.isNotBlank() },
                                lastName = lastName.takeIf { it.isNotBlank() },
                                cardFullName = cardFullName.takeIf { it.isNotBlank() },
                                cardNumber = cardNumber.toLongOrNull(),
                                cardExpireMonth = expireMonth.toIntOrNull(),
                                cardExpireYear = expireYear.toIntOrNull(),
                                cardCVV = cvv.toIntOrNull(),
                                uid = currentProfile.uid
                            )

                            // Aggiorna il profilo
                            profileViewModel.updateProfile(updatedProfile)

                            // Torna alla schermata di visualizzazione
                            navController.navigate(Screen.ProfileInfo.route) {
                                popUpTo(Screen.ProfileInfo.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salva")
                    }
                }
            }
        }
    }
}