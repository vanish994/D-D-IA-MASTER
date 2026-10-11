extends RefCounted
class_name SoloCombat

static func roll_die(sides: int) -> int:
	return randi_range(1, sides)

static func player_attack(character: Dictionary, enemy: Dictionary) -> Dictionary:
	var d20 := roll_die(20)
	var total: int = d20 + int(character.get("attack_bonus", 0))
	var hit := d20 == 20 or (d20 != 1 and total >= int(enemy.get("ac", 12)))
	var damage := 0
	if hit:
		damage = roll_die(8) + 3
		if d20 == 20:
			damage += roll_die(8) + 3
		enemy["hp"] = max(0, int(enemy.get("hp", 0)) - damage)
	return {"hit": hit, "roll": d20, "total": total, "damage": damage, "enemy_hp": int(enemy.get("hp", 0))}

static func enemy_attack(character: Dictionary, enemy: Dictionary) -> Dictionary:
	var d20 := roll_die(20)
	var total: int = d20 + int(enemy.get("attack_bonus", 3))
	var hit := d20 == 20 or (d20 != 1 and total >= int(character.get("armor_class", 10)))
	var damage := 0
	if hit:
		damage = roll_die(6) + 1
		if d20 == 20:
			damage += roll_die(6) + 1
		character["hp"] = max(0, int(character.get("hp", 0)) - damage)
	return {"hit": hit, "roll": d20, "total": total, "damage": damage, "player_hp": int(character.get("hp", 0))}
