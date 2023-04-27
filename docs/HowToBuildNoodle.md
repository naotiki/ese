# How to build an Ese Plugins
(There is an example project in [ese-lib](/ese-lib))

## Configure Ese Gradle Plugin
Add this code to `plugins` block in `build.gradle.kts`.
```kotlin
id("me.naotiki.ese.gradle-plugin") version "0.0.1-dev2"
```
Add `esePlugin` block in `build.gradle.kts` to configure build of Ese Plugins.

This is one of configuring examples.
```kotlin
esePlugin {
    pluginClass.set("Main")
    pluginName.set("Test")
}
```

`pluginClass` is FQDN name of ese plugin class. (Required)

`pluginName` is name of ese plugin. (Required)

## Implement Ese Plugin
Create a class named same name of `pluginClass` configured by build.gradle.kts `esePlugin` block.
It should be implementing `EsePlugin` interface.
```kotlin
import me.naotiki.ese.core.api.EsePlugin
class Main : EsePlugin {
    override suspend fun init(user: User) {
        //...
    }
}
```
You can do install processes in `init` function

## Build Ese Plugin
Run `createEsePlugin`
```shell
./gradlew createEsePlugin
```
On Success, `<pluginName>.ndl` file is Generated  into `build/ese/` directory.