package com.ddia.master

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF07111D)
private val Panel = Color(0xFF0C1928)
private val Gold = Color(0xFFD6A65A)
private val Parchment = Color(0xFFF1E5D0)
private val Muted = Color(0xFFB9C2D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("crypt_save", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Night, surface = Panel, primary = Gold,
                onPrimary = Night, onBackground = Parchment, onSurface = Parchment, secondary = Muted)) {
                NarrationScreen(
                    initialLog = prefs.getString("log", "") ?: "",
                    onSave = { log -> prefs.edit().putString("log", log).apply() }
                )
            }
        }
    }
}

@Composable
private fun NarrationScreen(initialLog: String, onSave: (String) -> Unit) {
    var log by remember { mutableStateOf(initialLog.ifBlank {
        "Após horas de viagem por uma trilha íngreme, você avista as ruínas de um antigo forte no topo da colina. A lua cheia ilumina as torres quebradas e o vento sopra entre as muralhas desmoronadas, trazendo um som distante, como se algo se movesse lá dentro.\n\nO caminho à frente é estreito e rochoso. O portão principal está entreaberto."
    }) }
    var lastChoice by remember { mutableStateOf("O silêncio envolve as ruínas.") }
    fun narrate(text: String) { log = (log + "\n\n" + text).takeLast(12000) }

    Column(Modifier.fillMaxSize().background(Night).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().height(58.dp).border(1.dp, Gold.copy(alpha = .7f), RoundedCornerShape(10.dp))
            .background(Panel, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = Gold, fontSize = 32.sp)
            Box(Modifier.padding(horizontal = 10.dp).size(36.dp).border(1.dp, Gold, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text("✦", color = Gold, fontSize = 24.sp)
            }
            Column(Modifier.weight(1f)) {
                Text("O Mestre", color = Parchment, fontSize = 21.sp, fontFamily = FontFamily.Serif)
                Text("AVENTURAS GANHAM VIDA", color = Gold, fontSize = 9.sp, letterSpacing = 1.3.sp)
            }
            TextButton(onClick = { onSave(log); lastChoice = "Histórico salvo neste aparelho." }) { Text("Salvar", color = Gold) }
        }

        Box(Modifier.fillMaxWidth().weight(0.34f).heightIn(min = 130.dp)
            .border(1.dp, Gold.copy(alpha = .75f), RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF17334C), Color(0xFF0A1827), Color(0xFF251D21))), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("☾", color = Color(0xFFE5D7B8), fontSize = 36.sp)
                Text("RUÍNAS DO FORTE", color = Parchment, fontSize = 19.sp, fontFamily = FontFamily.Serif, letterSpacing = 2.sp)
                Text("Torres partidas • névoa • luzes distantes", color = Muted, fontSize = 11.sp)
                Text("━━━━━━━━━━━━━━━━━━━━", color = Gold.copy(alpha = .7f), fontSize = 12.sp)
            }
        }

        Column(Modifier.fillMaxWidth().weight(0.42f).heightIn(min = 190.dp)
            .border(1.dp, Gold.copy(alpha = .8f), RoundedCornerShape(12.dp))
            .background(Panel, RoundedCornerShape(12.dp)).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✧  NARRAÇÃO DO MESTRE", color = Gold, fontSize = 12.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("HISTÓRICO", color = Muted, fontSize = 9.sp)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Gold.copy(alpha = .35f))
            // O painel permanece fixo; apenas o texto dentro dele rola e mantém todo o histórico.
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                Text(log, color = Parchment, fontSize = 16.sp, lineHeight = 24.sp, fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(10.dp))
                Text(lastChoice, color = Muted, fontSize = 12.sp)
            }
        }

        Text("ESCOLHA O QUE FAZER", color = Gold, fontSize = 12.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
        StoryChoice("Seguir pelo portão principal", "A entrada parece ser a rota mais direta.") {
            narrate("Você se aproxima do portão principal. As dobradiças rangem e uma sombra se move além da entrada.")
            lastChoice = "Você escolheu: seguir pelo portão principal."
        }
        StoryChoice("Contornar pelas ruínas", "Talvez exista uma entrada menos vigiada.") {
            narrate("Você contorna a muralha quebrada e encontra marcas recentes na lama. Algo esteve aqui há pouco.")
            lastChoice = "Você escolheu: contornar pelas ruínas."
        }
        StoryChoice("Procurar uma rota pela floresta", "Pode ser mais seguro, mas é mais demorado.") {
            narrate("Você entra entre as árvores e encontra uma passagem estreita atrás do forte.")
            lastChoice = "Você escolheu: procurar uma rota pela floresta."
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { lastChoice = "A rolagem ainda será conectada ao motor de regras." }, modifier = Modifier.weight(1f)) { Text("Rolar d20") }
            Button(onClick = { onSave(log); lastChoice = "Histórico salvo neste aparelho." }, modifier = Modifier.weight(1f)) { Text("Salvar jogo") }
        }
    }
}

@Composable
private fun StoryChoice(title: String, subtitle: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(9.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .75f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Parchment)) {
        Column(Modifier.fillMaxWidth()) {
            Text(title, fontFamily = FontFamily.Serif, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}
