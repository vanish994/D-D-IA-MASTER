package com.ddia.master

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class Hero(
    val name: String,
    val role: String,
    val maxHp: Int,
    val attackBonus: Int,
    val damage: Int,
    val armor: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("crypt_save", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme {
                GameScreen(
                    savedName = prefs.getString("name", "") ?: "",
                    savedRole = prefs.getString("role", "Guerreiro") ?: "Guerreiro",
                    savedHp = prefs.getInt("hp", -1),
                    savedGoblinHp = prefs.getInt("goblinHp", -1),
                    savedLog = prefs.getString("log", "") ?: "",
                    onSave = { name, role, hp, goblinHp, log ->
                        prefs.edit().putString("name", name).putString("role", role)
                            .putInt("hp", hp).putInt("goblinHp", goblinHp)
                            .putString("log", log).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun GameScreen(
    savedName: String,
    savedRole: String,
    savedHp: Int,
    savedGoblinHp: Int,
    savedLog: String,
    onSave: (String, String, Int, Int, String) -> Unit
) {
    val roles = listOf("Guerreiro", "Ladino", "Clérigo")
    var name by remember { mutableStateOf(savedName) }
    var role by remember { mutableStateOf(if (savedRole in roles) savedRole else "Guerreiro") }
    val hero = remember(role, name) {
        when (role) {
            "Ladino" -> Hero(name.ifBlank { "Aventureiro" }, role, 10, 5, 6, 14)
            "Clérigo" -> Hero(name.ifBlank { "Aventureiro" }, role, 11, 4, 6, 16)
            else -> Hero(name.ifBlank { "Aventureiro" }, role, 13, 5, 8, 16)
        }
    }
    var hp by remember { mutableIntStateOf(if (savedHp >= 0) savedHp else hero.maxHp) }
    var goblinHp by remember { mutableIntStateOf(if (savedGoblinHp >= 0) savedGoblinHp else 10) }
    var potions by remember { mutableIntStateOf(1) }
    var log by remember {
        mutableStateOf(savedLog.ifBlank {
            "Você chega à entrada de uma cripta esquecida. Um goblin bloqueia o corredor. O que fará?"
        })
    }
    var lastRoll by remember { mutableStateOf("Nenhuma rolagem ainda.") }
    var created by remember { mutableStateOf(savedName.isNotBlank()) }
    val dead = hp <= 0
    val won = goblinHp <= 0

    fun addLog(message: String) {
        log = "$message\n\n$log".take(3500)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("A CRIPTA", style = MaterialTheme.typography.headlineLarge)
            Text("RPG solo • protótipo Android", style = MaterialTheme.typography.bodyMedium)

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Seu aventureiro", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nome do personagem") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Text("Classe")
                    roles.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = role == option, onClick = {
                                val oldMax = hero.maxHp
                                role = option
                                hp = if (hp == oldMax) when (option) {
                                    "Ladino" -> 10
                                    "Clérigo" -> 11
                                    else -> 13
                                } else hp.coerceAtMost(when (option) {
                                    "Ladino" -> 10
                                    "Clérigo" -> 11
                                    else -> 13
                                })
                            })
                            Text(option)
                        }
                    }
                    Button(onClick = {
                        created = true
                        hp = when (role) { "Ladino" -> 10; "Clérigo" -> 11; else -> 13 }
                        goblinHp = 10
                        potions = 1
                        addLog("${hero.name}, ${role.lowercase()}, entra na cripta. Um goblin surge das sombras!")
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Criar / reiniciar aventura")
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ficha", style = MaterialTheme.typography.titleLarge)
                    Text("Nome: ${hero.name}  •  Classe: $role")
                    Text("PV: ${hp.coerceAtLeast(0)} / ${hero.maxHp}    CA: ${hero.armor}")
                    Text("Bônus de ataque: +${hero.attackBonus}    Poções: $potions")
                    HorizontalDivider()
                    Text("Goblin: ${goblinHp.coerceAtLeast(0)} / 10 PV")
                    Text(if (won) "Vitória! O corredor está livre." else if (dead) "Você caiu. Reinicie a aventura para tentar novamente." else "O combate continua.")
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ações", style = MaterialTheme.typography.titleLarge)
                    Button(
                        enabled = created && !won && !dead,
                        onClick = {
                            val die = Random.nextInt(1, 21)
                            val total = die + hero.attackBonus
                            lastRoll = "Ataque: d20 ($die) + ${hero.attackBonus} = $total"
                            if (die == 20 || total >= 12) {
                                val damage = if (die == 20) hero.damage + Random.nextInt(1, 5) else hero.damage
                                goblinHp = (goblinHp - damage).coerceAtLeast(0)
                                addLog("$lastRoll. Acerto! Você causa $damage de dano.")
                            } else {
                                addLog("$lastRoll. Você erra o goblin.")
                            }
                            if (goblinHp > 0) {
                                val enemyRoll = Random.nextInt(1, 21)
                                if (enemyRoll + 4 >= hero.armor) {
                                    val damage = Random.nextInt(1, 5)
                                    hp = (hp - damage).coerceAtLeast(0)
                                    addLog("O goblin ataca (d20 $enemyRoll + 4) e causa $damage de dano.")
                                } else addLog("O goblin ataca (d20 $enemyRoll + 4), mas erra.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Atacar (rola os dados)") }
                    OutlinedButton(
                        enabled = created && !dead && !won && potions > 0 && hp < hero.maxHp,
                        onClick = {
                            val heal = minOf(6, hero.maxHp - hp)
                            hp += heal
                            potions--
                            addLog("Você usa uma poção e recupera $heal PV.")
                            val enemyRoll = Random.nextInt(1, 21)
                            if (enemyRoll + 4 >= hero.armor) {
                                val damage = Random.nextInt(1, 5)
                                hp = (hp - damage).coerceAtLeast(0)
                                addLog("O goblin aproveita a abertura e causa $damage de dano.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Beber poção (cura até 6 PV)") }
                    OutlinedButton(
                        onClick = {
                            lastRoll = "d20 = ${Random.nextInt(1, 21)}"
                            addLog("Você testa a sorte: $lastRoll")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Rolar d20") }
                    Text(lastRoll, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { onSave(name, role, hp, goblinHp, log); addLog("Jogo salvo neste aparelho.") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salvar partida neste aparelho") }
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Diário da aventura", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(log)
                }
            }
            Text("Versão inicial: regras simplificadas para testar a aventura. Ainda não é uma implementação completa das regras oficiais de D&D 2024.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
