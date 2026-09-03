package com.autodiag.wican.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContributionConsentScreen(
    onGrant: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Sdílení anonymizovaných diagnostických dat", style = MaterialTheme.typography.titleLarge)
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "AutoDiag může přispívat do sdíleného datasetu, který se používá " +
                "k ověřování přesnosti PID vzorců, rozšiřování pokrytí DTC kódů a " +
                "zlepšování rozpoznávání vozidel. Nic z toho se neděje bez tvého " +
                "výslovného souhlasu a kdykoli to můžeš v Nastavení vypnout."
        )
        Text(modifier = Modifier.padding(top = 16.dp), text = "Co se odesílá, když souhlasíš:", style = MaterialTheme.typography.titleMedium)
        listOf(
            "souhrnné statistiky PID za relaci (min/max/průměr, ne každý vzorek)",
            "kódy DTC a počet jejich výskytů",
            "prvních 10 znaků VIN (výrobce + typ vozidla + rok modelu) — bez čísla podvozku",
            "verze firmwaru adaptéru a software řídicí jednotky, pokud jsou dostupné",
            "měsíc, ve kterém byla data pořízena (ne přesný čas)"
        ).forEach { Text(modifier = Modifier.padding(top = 4.dp), text = "•  $it") }
        Text(modifier = Modifier.padding(top = 16.dp), text = "Co se neodesílá nikdy:", style = MaterialTheme.typography.titleMedium)
        listOf(
            "posledních 7 znaků VIN (číslo podvozku identifikující konkrétní vozidlo)",
            "poloha vozidla",
            "poznámky, štítky nebo jiný volný text",
            "surová data CAN sběrnice mimo agregované statistiky",
            "cokoliv, dokud aktivně neudělíš souhlas na této obrazovce"
        ).forEach { Text(modifier = Modifier.padding(top = 4.dp), text = "•  $it") }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDecline) { Text("Nesouhlasím") }
            Button(onClick = onGrant) { Text("Souhlasím a chci přispívat") }
        }
    }
}
