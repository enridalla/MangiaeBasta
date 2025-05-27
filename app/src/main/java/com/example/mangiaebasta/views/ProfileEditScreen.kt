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
import androidx.compose.ui.graphics.Color
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
    val updateResult by profileViewModel.updateResult.collectAsState()
    val validationError by profileViewModel.validationError.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    // Gestione dei risultati
    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            when (result) {
                is ProfileViewModel.UpdateResult.Success -> {
                    snackbarHostState.showSnackbar(
                        message = "Profilo aggiornato con successo!",
                        duration = SnackbarDuration.Short
                    )
                    // Naviga dopo aver mostrato lo snackbar
                    kotlinx.coroutines.delay(1500) // Aspetta che lo snackbar sia visibile
                    profileViewModel.clearUpdateResult()
                    navController.navigate(Screen.ProfileInfo.route) {
                        popUpTo(Screen.ProfileInfo.route) { inclusive = true }
                    }
                }
                is ProfileViewModel.UpdateResult.Error -> {
                    // L'alert di errore sarà mostrato come AlertDialog
                }
            }
        }
    }

    // Alert di errore per l'aggiornamento
    updateResult?.let { result ->
        if (result is ProfileViewModel.UpdateResult.Error) {
            AlertDialog(
                onDismissRequest = { profileViewModel.clearUpdateResult() },
                title = { Text("Errore") },
                text = { Text(result.message) },
                confirmButton = {
                    TextButton(onClick = { profileViewModel.clearUpdateResult() }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    // Alert di errore per la validazione
    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { profileViewModel.clearValidationError() },
            title = { Text("Dati non validi") },
            text = { Text(validationError!!) },
            confirmButton = {
                TextButton(onClick = { profileViewModel.clearValidationError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

                // Stati per gli errori dei campi
                var firstNameError by remember { mutableStateOf(false) }
                var lastNameError by remember { mutableStateOf(false) }
                var cardFullNameError by remember { mutableStateOf(false) }
                var cardNumberError by remember { mutableStateOf(false) }
                var expireMonthError by remember { mutableStateOf(false) }
                var expireYearError by remember { mutableStateOf(false) }
                var cvvError by remember { mutableStateOf(false) }

                // Funzione per accettare solo caratteri numerici
                fun onlyDigits(input: String, maxLen: Int? = null): String {
                    val digits = input.filter { it.isDigit() }
                    return maxLen?.let { digits.take(it) } ?: digits
                }

                // Funzione per validare i campi in tempo reale
                fun validateFields() {
                    firstNameError = firstName.isBlank() || firstName.length < 2
                    lastNameError = lastName.isBlank() || lastName.length < 2
                    cardFullNameError = cardFullName.isBlank() || cardFullName.length < 4
                    cardNumberError = cardNumber.isBlank() || cardNumber.length < 13 || cardNumber.length > 19

                    val monthInt = expireMonth.toIntOrNull()
                    expireMonthError = monthInt == null || monthInt < 1 || monthInt > 12

                    val yearInt = expireYear.toIntOrNull()
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    expireYearError = yearInt == null || yearInt < currentYear || yearInt > currentYear + 20

                    cvvError = cvv.isBlank() || cvv.length < 3 || cvv.length > 4
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Sezione dati personali
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            firstNameError = false // Reset errore quando l'utente modifica
                        },
                        label = { Text("Nome") },
                        singleLine = true,
                        isError = firstNameError,
                        supportingText = if (firstNameError) {
                            { Text("Il nome deve contenere almeno 2 caratteri", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = {
                            lastName = it
                            lastNameError = false
                        },
                        label = { Text("Cognome") },
                        singleLine = true,
                        isError = lastNameError,
                        supportingText = if (lastNameError) {
                            { Text("Il cognome deve contenere almeno 2 caratteri", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Sezione dati carta
                    OutlinedTextField(
                        value = cardFullName,
                        onValueChange = {
                            cardFullName = it
                            cardFullNameError = false
                        },
                        label = { Text("Intestatario carta") },
                        singleLine = true,
                        isError = cardFullNameError,
                        supportingText = if (cardFullNameError) {
                            { Text("L'intestatario deve contenere almeno 4 caratteri", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            cardNumber = onlyDigits(it)
                            cardNumberError = false
                        },
                        label = { Text("Numero carta") },
                        singleLine = true,
                        isError = cardNumberError,
                        supportingText = if (cardNumberError) {
                            { Text("Il numero carta deve essere tra 13 e 19 cifre", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = expireMonth,
                            onValueChange = {
                                expireMonth = onlyDigits(it, 2)
                                expireMonthError = false
                            },
                            label = { Text("Mese scadenza") },
                            singleLine = true,
                            isError = expireMonthError,
                            supportingText = if (expireMonthError) {
                                { Text("1-12", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = expireYear,
                            onValueChange = {
                                expireYear = onlyDigits(it, 4)
                                expireYearError = false
                            },
                            label = { Text("Anno scadenza") },
                            singleLine = true,
                            isError = expireYearError,
                            supportingText = if (expireYearError) {
                                { Text("Anno non valido", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = {
                            cvv = onlyDigits(it, 4)
                            cvvError = false
                        },
                        label = { Text("CVV") },
                        singleLine = true,
                        isError = cvvError,
                        supportingText = if (cvvError) {
                            { Text("CVV deve essere di 3 o 4 cifre", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // Valida i campi prima dell'invio
                            validateFields()

                            // Se ci sono errori, non procedere
                            if (firstNameError || lastNameError || cardFullNameError ||
                                cardNumberError || expireMonthError || expireYearError || cvvError) {
                                return@Button
                            }

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
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (isLoading) "Salvando..." else "Salva")
                    }
                }
            }
        }
    }
}