package com.example.mangiaebasta.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.Screen
import com.example.mangiaebasta.viewmodels.OrderInfoUiState
import com.example.mangiaebasta.viewmodels.ProfileViewModel

@Composable
fun ProfileInfoScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    val profile by profileViewModel.profile.collectAsState(initial = null)
    val orderInfo by profileViewModel.orderInfo.collectAsState(initial = null)
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isProfileComplete by profileViewModel.isProfileComplete.collectAsState()

    // Ricarica i dati quando la schermata viene visualizzata
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        profileViewModel.loadOrderInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Avatar
        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(20.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (!isProfileComplete) {
            // Mostra la schermata di completamento profilo
            EmptyProfileView(onCompleteProfile = {
                navController.navigate(Screen.ProfileEdit.route)
            })
        } else {
            profile?.let { p ->
                ProfileCard("Dati Utente") {
                    p.firstName?.let { ProfileRow("Nome:", it) }
                    p.lastName?.let { ProfileRow("Cognome:", it) }
                }

                ProfileCard("Carta di Credito") {
                    p.cardFullName?.let { ProfileRow("Intestatario:", it) }
                    p.cardNumber?.let { ProfileRow("Numero:", it.toString()) }
                    if (p.cardExpireMonth != null && p.cardExpireYear != null) {
                        ProfileRow("Scadenza:", "${p.cardExpireMonth}/${p.cardExpireYear}")
                    }
                    p.cardCVV?.let { ProfileRow("CVV:", it.toString()) }
                }

                Spacer(Modifier.height(24.dp))

                // Card Ordine, sempre visibile
                OrderSection(orderInfo)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate(Screen.ProfileEdit.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Modifica Profilo")
                }
            }
        }
    }
}

@Composable
fun EmptyProfileView(onCompleteProfile: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profilo non completo",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Per poter utilizzare tutte le funzionalità dell'app, è necessario completare il tuo profilo con le informazioni personali e di pagamento.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCompleteProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Completa il profilo")
            }
        }
    }
}

@Composable
fun OrderSection(orderInfo: OrderInfoUiState?) {
    ProfileCard("Ordine") {
        if (orderInfo == null) {
            Text(
                text = "Ordine non ancora effettuato",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        } else {
            ProfileRow("Stato ordine:", orderInfo.orderStatus)
            orderInfo.menuName?.let { ProfileRow("Menu ordinato:", it) }
        }
    }
}

@Composable
private fun ProfileCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}