# Better console
Kotlin version of the plugin https://github.com/osp54/BetterConsole

Makes using the console more comfortable:
![Example](https://user-images.githubusercontent.com/86189625/183602444-f9608826-5152-4a82-a672-a1adbec56228.mp4)

### Setup

Clone this repository first.
To edit the plugin display name and other data, take a look at `plugin.json`.

### Building a Jar

If you use Linux / Mac OS:
`chmod +x ./gradlew`

Then:
`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


### Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins/mods by running the `mods` command.
