package me.naotiki.ese.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.enterprise.test.FileProperty
import java.io.File

abstract class RunEsePluginTask: DefaultTask() {

    @get:Input
    abstract val eseClientPath : Property<String>

    @TaskAction
    fun runEsePlugin(){
        println(
            inputs.files.singleFile.absolutePath)
        ProcessBuilder(eseClientPath.get(),"-p","")
    }
}