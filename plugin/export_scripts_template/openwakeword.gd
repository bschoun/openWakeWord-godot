extends Node
class_name OpenWakeWord

var _plugin_name = "OpenWakeWord"
var _android_plugin			# Reference to the plugin
var _timer : Timer			# Timer that runs for 1 second after a detection

var detected : bool = false 	# True for 1 second after a detection

signal word_detected(index)		# Signal emitted when word is detected, index of word detected
signal permission_granted       # Signal emitted when audio permission is granted

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
	if permission == "android.permission.RECORD_AUDIO":
		if granted:
			_audio_permission_granted()
			# We no longer need to subscribe to this signal
			get_tree().on_request_permissions_result.disconnect(_on_request_permissions_result)
		
	
func _audio_permission_granted():
	
	# Start our plugin, and subscribe to the wakeword_detected signal
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.connect("wakeword_detected", _on_wakeword_detected)
		permission_granted.emit()
	else:
		printerr("Couldn't find plugin " + _plugin_name)
			
			
func start_detection(models : Array, chunk_size : int):
	
	# The plugin
	if not _android_plugin: 
		print("_android_plugin in null, cannot start detection.")
		return
		
	# Start detection of words
	if not _android_plugin.isDetecting():
		print("starting detection")
		_android_plugin.startDetection(models, chunk_size)
	else:
		print("already detecting")
		
		
func stop_detection():
	if not _android_plugin:
		print("_android_plugin is null, cannot stop detection.")
		return
	
	if _android_plugin.isDetecting():
		print("stopping detection")
		_android_plugin.stopDetection()
	else:
		print("not detecting yet")
			
			
func _on_wakeword_detected(index):
	# To not have multiple detections at once, disallow detection after the initial detection for 1 second
	if not detected:
		detected = true
		print("wakeword detected " + str(index))
		word_detected.emit()
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
