# OpenWakeWord Godot Android Plugin
This is a Godot Android plugin for [OpenWakeWord](https://github.com/dscripka/openWakeWord). It is essentially the 
[OpenWakeWord for Android](https://github.com/hasanatlodhi/OpenwakewordforAndroid/tree/main) project modified to be a Godot package.

This package is currently configured to recognize the wake word "Galaxy" for my own personal project. You will need to train your own model and re-build the package to recognize a different wake word.

Tested in Godot 4.3, on an Android phone and with a Meta Quest 3 headset!

## Building this plugin
- In a terminal window, navigate to the project's root directory and run the following command:
```
./gradlew assemble
```
- On successful completion of the build, the output files can be found in
  [`plugin/demo/addons`](plugin/demo/addons)

## Using this plugin
You can use the included [Godot demo project](plugin/demo/project.godot) as a test.

- Open the demo in Godot (4.3 or higher)
- Navigate to `Project` -> `Project Settings...` -> `Plugins`, and ensure the plugin is enabled
- Install the Godot Android build template by clicking on `Project` -> `Install Android Build Template...`
- In `Project` -> `Export...`, under `Android (Runnable)`, tick `Record Audio` on
- Connect an Android device to your machine and run the demo
- In the app, press the `Start` button and say "Galaxy". If it works, a label that says "WAKE WORD DETECTED" should appear

## Pre-trained models
The original [OpenWakeWord](https://github.com/dscripka/openWakeWord) project has some pre-trained models. Please see their page for more details.

## Training your own wake word model
- Use the [Google Colab](https://colab.research.google.com/drive/1q1oe2zOyZp7UsB3jJiQ1IFn8z5YfjwEb?usp=sharing) from the [OpenWakeWord](https://github.com/dscripka/openWakeWord) project to train your own model (please see OpenWakeWord's "Training New Models" section for more information)
- In this project, open the folder `plugin/src/main/assets/`
- You can remove any models from this folder that you don't need, such as `galaxy.onnx`. **PLEASE NOTE**: you DO need `embeddings_model.onnx` and `melspectrogram.onnx`
- In the demo project, update the `_on_start_stop_button_toggled()` function in `main.gd` with the filename of your model
- Rebuild the plugin

## Known Issues
None that I know of, but please submit issues and pull requests!

## Ideas
- For ease of use, I don't want to require re-building the plugin when adding/removing models. Looking into options for this.