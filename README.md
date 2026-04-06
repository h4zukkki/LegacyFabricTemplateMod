# Legacy-Fabric Mod Template

A customized fabric mod template using [Kotlin](https://kotlinlang.org/)
and [Kotlin build script](https://docs.gradle.org/current/userguide/kotlin_dsl.html) with my own tweaks.

## Features

1. **Automatically mixins collection**  
   Adding mixins manually is **annoying**, so I've implemented logic in `build.gradle.kts` to **automatically** collect
   all Mixin from source directory, also support multi `*.mixins.json` in your project, you can improve it in any way
   you want, even using ASM to verify `@Mixin`, `@Inject` annotations if your project is complex.

2. **Version Catalog (libs.versions.toml)**  
   I migrated version number declarations from `gradle.properties` to `gradle/libs.versions.toml`, you can see
   difference in `build.gradle.kts`, it makes the build script **much cleaner** and easier to manage.

## Setup

For setup instructions please see [Legacy Fabric](https://github.com/Legacy-Fabric/fabric-example-mod)
