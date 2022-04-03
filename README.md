### THIS IS A BUNGEE PLUGIN! Install in 'bungee_command/plugins' not 'bukkit_command/plugins'

# EaglerMOTD

### This plugin can add animated MOTDs to your Eaglercraft server

![EaglerMOTD Sample](https://i.gyazo.com/4e0105720c866990c82b221fe82f7cc9.gif)

**It can also add custom "Accept:" query handlers for 3rd party sites to gather more information about your server**

## How to Install

**Download [EaglerMOTD.jar](https://raw.githubusercontent.com/LAX1DUDE/eaglercraft-motd/main/EaglerMOTD.jar) and place it in your EaglercraftBungee '/plugins' directory. Then, restart EaglercraftBungee**

You will find a new 'EaglerMOTD' folder in the plugins folder you put the jar in, once you finish restarting your server. This contains the plugin's configuration files, you can edit any of them and then type `motd-reload` in the EaglercraftBungee console to reload all the variables.

## Configuration Guide

### Messages.json:

```json
{
  "close_socket_after": 1200,
  "max_sockets_per_ip": 10,
  "max_total_sockets": 256,
  "allow_banned_ips": false,
  "messages": {
    "all": [
      {
        "name": "default",
        "frames": [ 
          "frames.frame1",
          "frames.frame2",
          "frames.frame3",
          "frames.frame4"
        ],
        "interval": 8,
        "random": false,
        "shuffle": false,
        "timeout": 500,
        "weight": 1.0,
        "next": "any"
      }
    ]
  }
}
```

- `close_socket_after` Defines the maximum number of ticks (1/20ths of a second) that an animated MOTD should be displayed to a player (default 1200 ticks, which is 60 seconds)

- `max_sockets_per_ip` Defines the maximum number of MOTD connections that a single IP address can open (does not apply to local IPs like 127.0.0.1 and 192.168.0.0 have no limit)

- `max_total_sockets` Defines the maximum number of MOTD connections that can be open on the server at any given time

- `allow_banned_ips` Defines if animated MOTDs should play for banned IPs

- `messages` Defines the list of possible messages to display on every listener
 
    - `all` Defines the list of possible messages to display on all listeners
 
    - To define a list for a specific listener, type the hostname of the listener. The default host defined in a fresh default `config.yml` in EaglercraftBungee is `0.0.0.0:25565` so to define a list of messages for that specific listener you would use `"0.0.0.0:25565"` instead of `"all"` to add the messages. Messages defined as `"all"` will be added to the list of messages for all listeners.

   - The list contains a set of JSON objects each containing these fields:
        - `name` *(Optional)* Defines the name of the message, used for specifying a `next` property in a different message to jump to this message once it has timed out
        - `frames` Defines a list of strings specifying the list of frames in the animation of this message. They are defined in `file.frame` format where `file` is the file the frame is located in (which would be `file.json`) and `frame` is the name of the frame in that file to use
        - `interval` *(Optional)* defines the delay between frames. Setting to `0` (default) disables the animation and only shows the first frame
        - `random` *(Optional)* defines if the animation should begin on a random frame (`true`) or the first frame (`false`, default)
        - `shuffle` *(Optional)* defines if the animation should switch between frames sequentially (`false`, default) or randomly (`true`)
        - `timeout` *(Optional)* defines how many ticks the animation should play before stopping or switching to `"next"` (default 500)
        - `weight` *(Optional)* defines the random probability of choosing this message over any other messages in `"all"` and the list of messages for the specific listener the message is playing on. Default is 1.0
        - `next` *(Optional)* defines the `"name"` of the message to play once this message times out (`timeout`). Set to `"any"` to pick any message, or set to `null` to stop the animation once `timeout` is reached. Default is null

### Frames.json:

**You can name this file anything you want, and you can create more than one. In 'messages.json' just define the frame as, for example, 'file.name' and it will look in a file called 'file.json' for a frame named 'name'. The example 'frames.frame1' in 'messages.json' will load 'frame1' from this default file called 'frames.json'**

```json
{
	"frame1": { 
		"icon": "server-animation.png",
		"icon_spriteX": 0,
		"icon_spriteY": 0,
		"online": "default",
		"max": "default",
		"players": "default",
		"text0": "&7An Eaglercraft server",
		"text1": "&0!!!!&8Running EaglerMOTD plugin"
	},
	"frame2": { 
		"icon": "server-animation.png",
		"icon_spriteX": 1,
		"icon_spriteY": 0,
		"icon_color": [ 1.0, 0.0, 0.0, 0.15 ],
		"online": 10,
		"players": [ "fake player 1", "fake player 2" ],
		"text0": "&6&nAn&r &7Eaglercraft server",
		"text1": "&0!!&8!&0!&8Running EaglerMOTD plugin"
	},
	"frame3": { 
		"icon": "server-animation.png",
		"icon_spriteX": 2,
		"icon_spriteY": 0,
		"icon_color": [ 1.0, 0.0, 0.0, 0.15 ],
		"icon_tint": [ 0.8, 0.8, 1.0 ],
		"online": 20,
		"players": [],
		"text0": "&7An &6&nEaglercraft&r &7server",
		"text1": "&0!&8!!&0!&8Running EaglerMOTD plugin"
	},
	"frame4": { 
		"icon": "server-animation.png",
		"icon_spriteX": 3,
		"icon_spriteY": 0,
		"icon_color": [ 1.0, 1.0, 0.0, 0.15 ],
		"icon_tint": [ 0.8, 0.8, 1.0, 0.8 ],
		"online": 30,
		"players": "default",
		"text0": "&7An Eaglercraft &6&nserver&r",
		"text1": "&8!!!&0!&8Running EaglerMOTD plugin"
	}
}
```

**Every frame will retrieve the values from the previous frame in the message for the default value for every variable**

- `text0` *(Optional)* Changes the first line of text in the server's current MOTD

- `text1` *(Optional)* Changes the second line of text in the server's current MOTD

- `online` *(Optional)* Changes the number of online players, use `"default"` to reset it

- `max` *(Optional)* Changes the max number of players in the MOTD, use `"default"` to reset it

- `players` *(Optional)* Changes the list of players shown when the mouse is hovering over the online/max count in the multiplayer screen. use `"default"` to reset it and show the real list of players instead of spoofing it

- `icon` *(Optional)* A JPEG/PNG/BMP/GIF to display as the server icon. The icon must be at least 64x64 pixels. **Transparency is supported.** The top left 64x64 pixels of the image are displayed if the image is larger than 64x64 pixels. **Animated GIF files are not supported, they load but only display the first frame.** Setting the icon resets the values of `icon_spriteX`, `icon_spriteY`, `icon_color`, `icon_tint`, `icon_flipX`, `icon_flipY`, and `icon_rotate` **to their default values.** Setting to `"none"` will reset everything and set the icon to be black and 100% transparent. **The icon's file name is relative to the folder EaglercraftBungee is currently running in**, not the folder where the JSON file is stored.

    - `icon_spriteX` *(Optional)* defines the X coordinate to read a 64x64 pixel portion of the current `"icon"` file, if the file is larger than 64x64. The value is multiplied by 64 to get the exact pixel coordinate in the image to read from. `"icon_spriteX": 2` will read a 64x64 pixel portion of a larger image beginning at 128 pixels X and 0 pixels Y of the current `"icon"` file. Default is 0
    - `icon_spriteY` *(Optional)* defines the Y coordinate to read a 64x64 pixel portion of the current `"icon"` file, if the file is larger than 64x64. The value is multiplied by 64 to get the exact pixel coordinate in the image to read from. `"icon_spriteY": 2` will read a 64x64 pixel portion of a larger image beginning at 0 pixels X and 128 pixels Y of the current `"icon"` file. Default is 0. Setting, for example, `"icon_spriteX": 1` and `"icon_spriteY": 2` will read a 64x64 pixel portion of the current `"icon"` file beginning at 64 pixels X and 128 pixels Y.

    - `icon_pixelX` *(Optional)* defines the exact X pixel coordinate to read a 64x64 pixel portion of the current `"icon"` file, if the file is larger than 64x64. Unlike `icon_spriteX`, setting this value will not multiply the input value by 64, it will read from the exact coordinate in the image. **Overrides icon_spriteX**
    - `icon_pixelY` *(Optional)* defines the exact Y pixel coordinate to read a 64x64 pixel portion of the current `"icon"` file, if the file is larger than 64x64. Unlike `icon_spriteY`, setting this value will not multiply the input value by 64, it will read from the exact coordinate in the image. **Overrides icon_spriteY**

    - `icon_color` *(Optional)* mixes an RGBA color with the current `"icon"`, or displays the color directly if `"icon"` is `"none"`. `[1.0, 1.0, 1.0]` displays white, `[0.0, 0.0, 0.0]` displays black, `[0.0, 1.0, 0.0]` displays green, `[0.0, 1.0, 0.0, 0.5]` displays green with 50% transparency, blending it with the color of the current `"icon"` file's pixels if it is set.

    - `icon_tint` *(Optional)* multiplies an RGBA color by all the pixels in the current `"icon"` and/or `"icon_color"`. `[0.0, 0.0, 1.0]` would make the icon's pixels blue-colored, `[1.0, 0.0, 0.0]` would make the icon's pixels red-colored, `[0.0, 1.0, 0.0, 0.5]` would make the icon's pixels green-colored and 50% transparent. `[2.0, 2.0, 2.0]` would double the brightness of the icon.

    - `icon_flipX` *(Optional)* flips the pixels of the icon displayed horizontally
 
    - `icon_flipY` *(Optional)* flips the pixels of the icon displayed vertically
 
    - `icon_rotate` *(Optional)* rotates the icon 90&deg;, 180&deg;, or 270&deg; clockwise (`0` = 0&deg;, `1` = 90&deg;, `2` = 180&deg;, `3` = 270&deg;)

### Queries.json:

**This has nothing to do with MOTD, skip this part if you're just trying to add an animated MOTD**

**This file allows you to configure custom** `Accept:` **handlers to EaglercraftBungee to provide more custom statistics to 3rd party server lists and crawlers**

```json
{
	"queries": { 
		"ExampleQuery1": { 
			"type": "ExampleQuery1_result",
			"string": "This is a string"
		},
		"ExampleQuery2": { 
			"type": "ExampleQuery2_result",
			"txt": "query2.txt"
		},
		"ExampleQuery3": { 
			"type": "ExampleQuery3_result",
			"string": "This query returns binary",
			"file": "binary.dat"
		},
		"ExampleQuery4": { 
			"type": "ExampleQuery4_result",
			"json": "query4.json"
		},
		"ExampleQuery5": { 
			"type": "ExampleQuery5_result",
			"json": {
				"key1": "value1",
				"key2": "value2"
			}
		},
		"ExampleQuery6": { 
			"type": "ExampleQuery6_result",
			"json": {
				"desc": "This query returns JSON and a file",
				"filename": "test_file.dat",
				"size": 69
			},
			"file": "test_file.dat"
		}
	}
}
```

`"queries"` contains a JSON object, each variable in this JSON object is the name of a query and the value is a JSON object containing the type of response and the content of the `"data"` value in the response

**Here is an example of a server's response to a generic query:**

```json
{
	"type": "<type here>",
	"data": "<data here>",
	"vers": "0.1.0",
	"name": "EaglercraftBungee Server",
	"time": 1648946954405,
	"brand": "Eagtek",
	"cracked": true
}
```

`"type"` **is just a generic string you can set to hint to the client what kind of response you are sending, and** `"data"` **stores the actually data of the response, and can be either a string or JSON object**

**Binary WebSocket packets can also be sent to a client, their format can be completely arbitrary and arrive containing the same raw unformatted data that was sent. They accompany a regular JSON response, the** `"type"` **and/or** `"data"` **value of the JSON response can be used to hint to the client when a raw binary packet is present**

The `"vers"`, `"name"`, `"time"`, `"brand"`, and `"cracked"` values are added internally by the server and cannot be changed

### Queries.json Format:

The file contains a JSON object with a `"queries"` value which contains a map of keys matching `Accept:` types to JSON objects

The JSON objects contain the `"type"` of response to send and what the `"data"` for that response should be. Also, a path to a binary file can be specified.

**All files specified in queries.json are RELOADED AUTOMATICALLY when changes are detected.** This allows you to dynamically update the response for certain queries without `motd-reload` because you can just edit the file the query is configured to read and it will update the version cached in memory automatically.

To add or edit an entry in queries.json, you need to define:
- `type`, which determines the string in the `"type"` field of the response sent to the client

Then, you must define **one** of:
- `json` *(object)* A JSON object to send as `"data"` in the response
- `json` *(string)* A path to a file (relative to your EaglercraftBungee folder) to parse as a JSON object and send as JSON as `"data"` in the response
- `txt` A path to a text file (relative to your EaglercraftBungee folder) to send as a string as `"data"` in the response
- `string` A string to send as `"data"` in the response

Optionally, you can define:
- `file` A path to an additional binary file (relative to your EaglercraftBungee folder) to send as a binary WebSocket packet after sending the first JSON response containing the `"data"` from the required `json`, `txt`, or `string` value. This file is also reloaded automatically

**Use** `motd-reload` **to reload queries.json**

## Compiling and Contributing

First, download the latest [EaglercraftBungee jar](https://github.com/LAX1DUDE/eaglercraft/blob/main/stable-download/java/bungee_command/bungee-dist.jar) in stable-download on [LAX1DUDE/eaglercraft](https://github.com/LAX1DUDE/eaglercraft/)

**Make a new java project in Eclipse/IDEA/etc and add 'src' folder in this repository as the source code folder**

**Then, add your EaglercraftBungee jar ([bungee-dist.jar](https://github.com/LAX1DUDE/eaglercraft/blob/main/stable-download/java/bungee_command/bungee-dist.jar)) to the java project's Build Path and refresh**

Export the contents of 'src' folder of the project to a JAR file to compile the plugin

**For a PR:** Tabs, not spaces, and format the code like the Eclipse auto format tool on factory settings.
