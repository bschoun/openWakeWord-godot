extends OpenWakeWord

@export var label : Label
@export var button : Button

# TODO: replace with array of names of your models in assets folder
var models = Array(["down.onnx", "up.onnx", "sit.onnx", "galaxy.onnx"])

func _ready() -> void:
	super()
	label.text = ""
	button.text = "Start"

func _on_wakeword_detected(index):
	super(index)
	label.text = "WAKE WORD DETECTED: " + models[index]
	
func _on_timer_timeout():
	super()
	label.text = ""

func _on_start_stop_button_toggled(toggled_on: bool) -> void:
	if toggled_on:
		# Choose a chunk size that suits your needs. Must be > 400 and must be a multiple of 80
		# Default for most examples is 1280. I use 720 to detect shorter phrases.
		var chunk_size = 720 
		start_detection(models, chunk_size)
		button.text = "Stop"
	else:
		stop_detection()
		button.text = "Start"
