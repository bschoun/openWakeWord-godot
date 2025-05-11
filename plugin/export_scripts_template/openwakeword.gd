extends Node
class_name OpenWakeWord

var _plugin_name = "OpenWakeWord"
var _android_plugin			# Reference to the plugin
var _timer : Timer			# Timer that runs for 1 second after a detection

var detected : bool = false 	# True for 1 second after a detection

#region Signals

signal word_detected			# Signal emitted when word is detected
signal started_detection		# Signal emitted when we start detection
signal stopped_detection		# Signal emitted when we stop detection
signal resumed_detection		# Signal emitted when we resume detection. TODO: maybe there's a better name for this

#endregion


func _ready() -> void:
	
	# Catch any permissions granted/denied
	get_tree().on_request_permissions_result.connect(_on_request_permissions_result)
	
	# Make sure we have the "RECORD_AUDIO" permissions
	# If it's already granted, OS.request_permission() will return true
	if OS.request_permission("RECORD_AUDIO"):
		_audio_permission_granted()
		
		# We no longer need to subscribe to this signal
		get_tree().on_request_permissions_result.disconnect(_on_request_permissions_result)
		

func _on_request_permissions_result(permission: String, granted: bool):
	if permission == "RECORD_AUDIO" and granted:
		_audio_permission_granted()
		# We no longer need to subscribe to this signal
		get_tree().on_request_permissions_result.disconnect(_on_request_permissions_result)
		
	
func _audio_permission_granted():
	
	# Start our plugin, and subscribe to the wakeword_detected signal
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.connect("wakeword_detected", _on_wakeword_detected)
	else:
		printerr("Couldn't find plugin " + _plugin_name)
			
			
func start_detection(model_name):
	if not _android_plugin:
		printerr("OpenWakeWord plugin is null. Make sure you've granted the RECORD_AUDIO permission.")
		return
		
	# Start detection of word "Galaxy"
	if not _android_plugin.isDetecting():
		print("starting detection")
		_android_plugin.startDetection(model_name)
		started_detection.emit()
	else:
		print("already detecting")
		
		
func stop_detection():
	if _android_plugin:
		if _android_plugin.isDetecting():
			print("stopping detection")
			_android_plugin.stopDetection()
			stopped_detection.emit()
		else:
			print("not detecting yet")
			
			
func _on_wakeword_detected():
	# To not have multiple detections at once, disallow detection after the initial detection for 1 second
	if not detected:
		detected = true
		print("wakeword detected")
		
		# Start a 1 second timer
		_timer = Timer.new()
		add_child(_timer)
		_timer.autostart = false
		_timer.timeout.connect(_on_timer_timeout)
		_timer.start(1.0)
		
		
func _on_timer_timeout():
	# Remove timer
	if _timer != null:
		_timer.queue_free()
		_timer = null
		
	# Resume detection
	detected = false
	resumed_detection.emit()
