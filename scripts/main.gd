extends Control

const CharacterFactory = preload("res://scripts/character.gd")
const CombatRules = preload("res://scripts/combat.gd")
const SaveManager = preload("res://scripts/save_manager.gd")

var character: Dictionary = {}
var enemy: Dictionary = {}
var game_state := "creation"
var log_text := ""
var name_input: LineEdit
var class_select: OptionButton
var status_label: Label
var story_label: Label
var log_label: RichTextLabel
var action_box: VBoxContainer

func _ready() -> void:
	randomize()
	_build_ui()
	_show_creation()

func _build_ui() -> void:
	var background := ColorRect.new()
	background.color = Color("#171b27")
	background.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(background)
	var scroll := ScrollContainer.new()
	scroll.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(scroll)
	var page := VBoxContainer.new()
	page.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	page.add_theme_constant_override("separation", 12)
	scroll.add_child(page)
	var title := Label.new()
	title.text = "D&D SOLO: A CRIPTA"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 28)
	title.add_theme_color_override("font_color", Color("#f2c879"))
	page.add_child(title)
	story_label = Label.new()
	story_label.text = "Uma pequena aventura para um herói. Crie seu personagem para começar."
	story_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	story_label.add_theme_font_size_override("font_size", 18)
	page.add_child(story_label)
	status_label = Label.new()
	status_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	status_label.add_theme_color_override("font_color", Color("#a9d7c1"))
	page.add_child(status_label)
	var name_label := Label.new()
	name_label.text = "Nome do personagem"
	page.add_child(name_label)
	name_input = LineEdit.new()
	name_input.placeholder_text = "Ex.: Mira"
	name_input.text = "Mira"
	name_input.custom_minimum_size.y = 52
	page.add_child(name_input)
	var class_label := Label.new()
	class_label.text = "Classe"
	page.add_child(class_label)
	class_select = OptionButton.new()
	class_select.add_item("Guerreiro")
	class_select.add_item("Ladino")
	class_select.add_item("Clérigo")
	class_select.custom_minimum_size.y = 52
	page.add_child(class_select)
	action_box = VBoxContainer.new()
	action_box.add_theme_constant_override("separation", 10)
	page.add_child(action_box)
	var log_title := Label.new()
	log_title.text = "Diário da aventura"
	log_title.add_theme_font_size_override("font_size", 20)
	page.add_child(log_title)
	log_label = RichTextLabel.new()
	log_label.bbcode_enabled = true
	log_label.fit_content = true
	log_label.scroll_active = false
	log_label.custom_minimum_size.y = 150
	page.add_child(log_label)

func _add_button(label_text: String, callback: Callable) -> void:
	var button := Button.new()
	button.text = label_text
	button.custom_minimum_size.y = 54
	button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	button.pressed.connect(callback)
	action_box.add_child(button)

func _clear_actions() -> void:
	for child in action_box.get_children():
		child.queue_free()

func _show_creation() -> void:
	game_state = "creation"
	story_label.text = "A vila de Brumafria está sendo aterrorizada por criaturas vindas de uma cripta antiga. Escolha seu herói."
	status_label.text = "Guerreiro: resistente • Ladino: ágil • Clérigo: equilibrado"
	_clear_actions()
	_add_button("Criar personagem e iniciar", _start_adventure)
	if SaveManager.has_save():
		_add_button("Carregar jogo salvo", _load_game)

func _start_adventure() -> void:
	var chosen_class := class_select.get_item_text(class_select.selected)
	character = CharacterFactory.create_character(name_input.text, chosen_class)
	enemy = {"name": "Goblin da Cripta", "hp": 10, "max_hp": 10, "ac": 12, "attack_bonus": 3}
	log_text = "[b]A aventura começou![/b]\nVocê recebeu uma poção de cura. Explore a cripta com cuidado.\n"
	_show_exploration()

func _show_exploration() -> void:
	game_state = "exploration"
	story_label.text = "Você chega à entrada da cripta. Um goblin bloqueia a passagem, protegendo um baú antigo."
	_update_status()
	_clear_actions()
	_add_button("Enfrentar o goblin", _start_combat)
	_add_button("Usar poção de cura", _use_potion)
	_add_button("Salvar jogo", _save_game)
	_add_button("Voltar à criação", _show_creation)
	_update_log()

func _start_combat() -> void:
	game_state = "combat"
	story_label.text = "O goblin rosna e ergue sua arma. Você age primeiro!"
	_show_combat_actions()
	_update_status()

func _show_combat_actions() -> void:
	_clear_actions()
	if int(enemy.get("hp", 0)) <= 0:
		_victory()
		return
	if int(character.get("hp", 0)) <= 0:
		_game_over()
		return
	_add_button("Atacar", _player_turn)
	_add_button("Usar poção de cura", _use_potion)
	_add_button("Salvar jogo", _save_game)
	_add_button("Fugir para a entrada", _show_exploration)

func _player_turn() -> void:
	var result: Dictionary = CombatRules.player_attack(character, enemy)
	if result["hit"]:
		log_text += "Você acertou (d20 %d + bônus = %d) e causou %d de dano.\n" % [result["roll"], result["total"], result["damage"]]
	else:
		log_text += "Seu ataque errou (d20 %d + bônus = %d).\n" % [result["roll"], result["total"]]
	if int(enemy["hp"]) <= 0:
		_update_status()
		_update_log()
		_victory()
		return
	var counter: Dictionary = CombatRules.enemy_attack(character, enemy)
	if counter["hit"]:
		log_text += "O goblin acertou (d20 %d + bônus = %d) e causou %d de dano.\n" % [counter["roll"], counter["total"], counter["damage"]]
	else:
		log_text += "O goblin errou o ataque (d20 %d + bônus = %d).\n" % [counter["roll"], counter["total"]]
	_update_status()
	_update_log()
	if int(character["hp"]) <= 0:
		_game_over()
	else:
		_show_combat_actions()

func _use_potion() -> void:
	if int(character.get("potions", 0)) <= 0:
		log_text += "Você não tem mais poções.\n"
	else:
		var healed := mini(8, int(character["max_hp"]) - int(character["hp"]))
		if healed <= 0:
			log_text += "Sua vida já está cheia.\n"
		else:
			character["hp"] = int(character["hp"]) + healed
			character["potions"] = int(character["potions"]) - 1
			log_text += "Você bebe uma poção e recupera %d PV.\n" % healed
	_update_status()
	_update_log()
	if game_state == "combat":
		if int(character["hp"]) <= 0:
			_game_over()
		else:
			_show_combat_actions()
	else:
		_show_exploration()

func _victory() -> void:
	game_state = "victory"
	character["gold"] = int(character.get("gold", 0)) + 15
	story_label.text = "Vitória! O goblin caiu e o baú contém 15 peças de ouro. Você concluiu esta pequena aventura."
	log_text += "[b]Vitória![/b] Você ganhou 15 peças de ouro. Salve seu progresso para continuar depois.\n"
	_clear_actions()
	_add_button("Salvar vitória", _save_game)
	_add_button("Jogar novamente", _show_creation)
	_update_status()
	_update_log()

func _game_over() -> void:
	game_state = "game_over"
	story_label.text = "Você caiu em combate. Seu progresso pode ser salvo e carregado mais tarde."
	log_text += "[b]Derrota.[/b] Descanse e tente novamente.\n"
	_clear_actions()
	_add_button("Carregar jogo salvo", _load_game)
	_add_button("Nova aventura", _show_creation)
	_update_status()
	_update_log()

func _save_game() -> void:
	if character.is_empty():
		log_text += "Crie um personagem antes de salvar.\n"
		_update_log()
		return
	var success := SaveManager.save_game(character, enemy, game_state)
	log_text += "Jogo salvo neste dispositivo.\n" if success else "Falha ao salvar o jogo.\n"
	_update_log()
	_update_status()

func _load_game() -> void:
	var data := SaveManager.load_game()
	if data.is_empty():
		log_text += "Nenhum salvamento válido foi encontrado.\n"
		_update_log()
		return
	character = data.get("character", {})
	enemy = data.get("enemy", {})
	game_state = str(data.get("state", "exploration"))
	log_text += "Salvamento carregado.\n"
	_update_status()
	_update_log()
	match game_state:
		"combat":
			story_label.text = "Jogo carregado. O combate continua!"
			_show_combat_actions()
		"victory":
			_victory_screen_loaded()
		"game_over":
			_game_over()
		_:
			_show_exploration()

func _victory_screen_loaded() -> void:
	story_label.text = "Sua vitória foi carregada. A cripta está segura."
	_clear_actions()
	_add_button("Salvar vitória", _save_game)
	_add_button("Nova aventura", _show_creation)

func _update_status() -> void:
	if character.is_empty():
		status_label.text = ""
		return
	status_label.text = "%s — %s | PV %d/%d | CA %d | Ouro %d | Poções %d\nInimigo: %s — PV %d/%d" % [
		character.get("name", "Herói"), character.get("class_name", "Aventureiro"),
		character.get("hp", 0), character.get("max_hp", 0), character.get("armor_class", 10),
		character.get("gold", 0), character.get("potions", 0), enemy.get("name", "nenhum"),
		enemy.get("hp", 0), enemy.get("max_hp", 0)
	]

func _update_log() -> void:
	log_label.text = log_text
