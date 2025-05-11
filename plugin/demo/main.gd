extends OpenWakeWord

@export var label : Label
@export var button : Button

func _ready() -> void:
	super()
	label.text = ""
	button.text = "Start"

func _on_wakeword_detected():
	super()
	label.text = "WAKE WORD DETECTED"
	
func _on_timer_timeout():
	super()
	label.text = ""

func _on_start_stop_button_toggled(toggled_on: bool) -> void:
	if toggled_on:
		
		# TODO: replace with name of your model in assets folder
		start_detection("galaxy.onnx")
		button.text = "Stop"
	else:
		stop_detection()
		button.text = "Start"
