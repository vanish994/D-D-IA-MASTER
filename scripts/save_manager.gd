extends RefCounted
class_name SoloSaveManager

const SAVE_PATH := "user://solo_rpg_save.json"

static func save_game(character: Dictionary, enemy: Dictionary, state: String) -> bool:
	var payload := {
		"character": character,
		"enemy": enemy,
		"state": state,
		"saved_at": Time.get_datetime_string_from_system()
	}
	var file := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file == null:
		push_error("Não foi possível abrir o arquivo de salvamento.")
		return false
	file.store_string(JSON.stringify(payload, "\t"))
	file.close()
	return true

static func load_game() -> Dictionary:
	if not FileAccess.file_exists(SAVE_PATH):
		return {}
	var file := FileAccess.open(SAVE_PATH, FileAccess.READ)
	if file == null:
		return {}
	var parsed: Variant = JSON.parse_string(file.get_as_text())
	file.close()
	if parsed is Dictionary:
		return parsed
	return {}

static func has_save() -> bool:
	return FileAccess.file_exists(SAVE_PATH)
