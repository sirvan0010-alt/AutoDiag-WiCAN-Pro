package com.autodiag.wican.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoTooltip(
    text: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Vysvětlení"
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { androidx.compose.material3.Text(text) } },
        state = androidx.compose.material3.rememberTooltipState()
    ) {
        IconButton(onClick = {}, modifier = modifier.size(32.dp)) {
            Icon(Icons.Default.HelpOutline, contentDescription = contentDescription)
        }
    }
}
