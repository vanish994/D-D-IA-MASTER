extends RefCounted
class_name SoloRulesEngine

# Núcleo determinístico das regras. A IA pode narrar o resultado,
# mas não deve calcular nem alterar os valores retornados aqui.

static func roll_die(sides: int) -> int:
	if sides < 1:
		push_error("Um dado precisa ter pelo menos 1 lado.")
		return 0
	return randi_range(1, sides)

static func roll_d20_test(modifier: int, difficulty_class: int) -> Dictionary:
	var roll := roll_die(20)
	var total := roll + modifier
	return {
		"roll": roll,
		"modifier": modifier,
		"total": total,
		"dc": difficulty_class,
		"success": roll == 20 or (roll != 1 and total >= difficulty_class),
		"natural_1": roll == 1,
		"natural_20": roll == 20
	}

static func resolve_attack(attack_bonus: int, target_ac: int, damage_die: int, damage_bonus: int) -> Dictionary:
	var roll := roll_die(20)
	var critical := roll == 20
	var hit := critical or (roll != 1 and roll + attack_bonus >= target_ac)
	var damage := 0
	if hit:
		damage = roll_die(damage_die) + damage_bonus
		if critical:
			# Acerto crítico dobra os dados, não o modificador fixo.
			damage += roll_die(damage_die)
		damage = maxi(0, damage)
	return {
		"roll": roll,
		"attack_bonus": attack_bonus,
		"total": roll + attack_bonus,
		"target_ac": target_ac,
		"hit": hit,
		"critical": critical and hit,
		"damage": damage
	}
