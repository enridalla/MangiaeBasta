package com.example.mangiaebasta.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mangiaebasta.viewmodels.ProfileEditUiState
import com.example.mangiaebasta.viewmodels.ProfileViewModel

@Composable
fun ProfileEditScreen(
    profileViewModel: ProfileViewModel,
    navController: NavController
) {
    val state by profileViewModel.editUi.collectAsState(initial = null)
    val isLoading by profileViewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    // Scaffold senza padding verticale di sistema
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
            if (isLoading || state == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val s = state!!
                var firstName by remember { mutableStateOf(s.firstName) }
                var lastName by remember { mutableStateOf(s.lastName) }
                var cardFullName by remember { mutableStateOf(s.cardFullName) }
                var cardNumber by remember { mutableStateOf(s.cardNumber) }
                var expireMonth by remember { mutableStateOf(s.cardExpireMonth.toString()) }
                var expireYear by remember { mutableStateOf(s.cardExpireYear.toString()) }
                var cvv by remember { mutableStateOf(s.cardCVV) }
                var orderStatus by remember { mutableStateOf(s.orderStatus) }
                var menuName by remember { mutableStateOf(s.menuName ?: "") }

                fun onlyDigits(input: String, maxLen: Int? = null): String {
                    val digits = input.filter { it.isDigit() }
                    return maxLen?.let { digits.take(it) } ?: digits
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                    OutlinedTextField(
                        value = orderStatus,
                        onValueChange = { orderStatus = it },
                        label = { Text("Stato ordine") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = menuName,
                        onValueChange = { menuName = it },
                        label = { Text("Menu ordinato") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val m = expireMonth.toIntOrNull() ?: 0
                            val y = expireYear.toIntOrNull() ?: 0
                            profileViewModel.updateProfile(
                                ProfileEditUiState(
                                    firstName = firstName,
                                    lastName = lastName,
                                    cardFullName = cardFullName,
                                    cardNumber = cardNumber,
                                    cardExpireMonth = m,
                                    cardExpireYear = y,
                                    cardCVV = cvv,
                                    orderStatus = orderStatus,
                                    menuName = menuName.ifBlank { null }
                                )
                            )
                            navController.navigate("profileInfoScreen") {
                                popUpTo("profileEditScreen") { inclusive = true }
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