package com.example.mangiaebasta.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mangiaebasta.viewmodels.ProfileViewModel

@Composable
fun ProfileEditScreen(navController: NavHostController, vm: ProfileViewModel) {
    var firstName   by remember { mutableStateOf(vm.firstName) }
    var lastName    by remember { mutableStateOf(vm.lastName) }
    var cardNumber  by remember { mutableStateOf(vm.cardNumber) }
    var expireMonth by remember { mutableStateOf(vm.cardExpire.take(2)) }
    var expireYear  by remember { mutableStateOf(vm.cardExpire.takeLast(2)) }
    var cvv         by remember { mutableStateOf(vm.cardCVV) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Text("Informazioni personali", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(firstName, { firstName = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(lastName, { lastName = it }, label = { Text("Cognome") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Text("Dati carta di pagamento", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(cardNumber, { cardNumber = it }, label = { Text("Numero della carta") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(expireMonth, { expireMonth = it }, label = { Text("MM") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(expireYear, { expireYear = it }, label = { Text("YY") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(cvv, { cvv = it }, label = { Text("CVV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            vm.update(firstName, lastName, cardNumber, expireMonth, expireYear, cvv); navController.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) { Text("Salva") }
    }
}