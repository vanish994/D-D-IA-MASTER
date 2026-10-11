extends RefCounted
class_name SoloCharacter

const DEFAULTS := {
	"name": "Aventureiro",
	"class_name": "Guerreiro",
	"level": 1,
	"max_hp": 12,
	"hp": 12,
	"armor_class": 16,
	"attack_bonus": 5,
	"damage_dice": "1d8+3",
	"gold": 10,
	"potions": 1
}

static func create_character(character_name: String, character_class: String) -> Dictionary:
	var data: Dictionary = DEFAULTS.duplicate(true)
	data["name"] = character_name.strip_edges()
	if data["name"].is_empty():
		data["name"] = "Aventureiro"
	data["class_name"] = character_class
	match character_class:
		"Guerreiro":
			data["max_hp"] = 12
			data["armor_class"] = 16
			data["attack_bonus"] = 5
			data["damage_dice"] = "1d8+3"
		"Ladino":
			data["max_hp"] = 10
			data["armor_class"] = 14
			data["attack_bonus"] = 5
			data["damage_dice"] = "1d6+3"
		"Clérigo":
			data["max_hp"] = 11
			data["armor_class"] = 16
			data["attack_bonus"] = 4
			data["damage_dice"] = "1d8+2"
	data["hp"] = data["max_hp"]
	return data
