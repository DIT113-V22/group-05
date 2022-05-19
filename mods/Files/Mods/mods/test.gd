extends Reference


var mod_name: String = "test"

func init(global) -> void:
	print("Test mod")
	
	global.register_environment("test/Test", load("res://source/test.tscn"))
