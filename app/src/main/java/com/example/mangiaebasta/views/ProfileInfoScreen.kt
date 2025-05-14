package com.example.mangiaebasta.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.Screen
import com.example.mangiaebasta.viewmodels.ProfileViewModel

@Composable
fun ProfileInfoScreen(navController: NavHostController, vm: ProfileViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(120.dp).padding(20.dp))
        }
        Spacer(Modifier.height(20.dp))
        ProfileCard("Dati Utente") {
            ProfileRow("Nome:", vm.firstName)
            ProfileRow("Cognome:", vm.lastName)
        }
        ProfileCard("Carta di Credito") {
            ProfileRow("Intestatario:", vm.cardFullName)
            ProfileRow("Numero:", vm.cardNumber)
            ProfileRow("Scadenza:", vm.cardExpire)
            ProfileRow("CVV:", vm.cardCVV)
        }
        Button(onClick = { navController.navigate(Screen.ProfileEdit.route) }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text("Modifica Profilo")
        }
    }
}

@Composable
private fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}