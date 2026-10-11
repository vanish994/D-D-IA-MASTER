extends RefCounted
class_name SoloCombat

const Rules = preload("res://scripts/rules_engine.gd")

static func roll_die(sides: int) -> int:
	return Rules.roll_die(sides)

static func player_attack(character: Dictionary, enemy: Dictionary) -> Dictionary:
	var result: Dictionary = Rules.resolve_attack(
		int(character.get("attack_bonus", 0)),
		int(enemy.get("ac", 12)),
		int(character.get("damage_die", 8)),
		int(character.get("damage_bonus", 3))
	)
	if result["hit"]:
		enemy["hp"] = maxi(0, int(enemy.get("hp", 0)) - int(result["damage"]))
	result["enemy_hp"] = int(enemy.get("hp", 0))
	return result

static func enemy_attack(character: Dictionary, enemy: Dictionary) -> Dictionary:
	var result: Dictionary = Rules.resolve_attack(
		int(enemy.get("attack_bonus", 3)),
		int(character.get("armor_class", 10)),
		int(enemy.get("damage_die", 6)),
		int(enemy.get("damage_bonus", 1))
	)
	if result["hit"]:
		character["hp"] = maxi(0, int(character.get("hp", 0)) - int(result["damage"]))
	result["player_hp"] = int(character.get("hp", 0))
	return result
