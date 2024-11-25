package io.nx.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import java.io.File
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import com.google.gson.Gson
import com.google.gson.GsonBuilder

abstract class CreateNodesTask: DefaultTask() {
    @Option(option = "outputDirectory", description = "Output Directory")
    @Input
    var outputDirectory: String = ""

    @TaskAction
    fun action() {
        val projects = mutableMapOf<String, MutableMap<String, Any> >()
        // println(project.getAllTasks(true))
        project.getAllprojects().forEach { project ->
            projects.put(project.name, this.processTargetsForProject(project))
        }
        val gson = Gson()
        val json = gson.toJson(projects)
        // println(json)
        if (true) {
            val file = File(".", "${project.name}.json")
            // println(file)
            file.writeText(json)
        }
    }

    fun processTargetsForProject(project: Project): MutableMap<String, Any>  {
        val targets = mutableMapOf<String, Any>();
        val targetGroups = mutableMapOf<String, MutableList<String>>();

        var command: String;
        val operSys = System.getProperty("os.name").lowercase();
        if (operSys.contains("win")) {
            command = ".\\gradlew.bat "
        } else {
            command = "./gradlew "
        }
        command += project.getBuildTreePath()
        if (!command.endsWith(":")) {
            command += ":"
        }

        project.getTasks().forEach{ task ->
            val target = mutableMapOf<String, Any?>()
            val metadata = mutableMapOf<String, Any?>()
            var taskCommand = command.toString()
            metadata.put("description", task.getDescription())
            metadata.put("technologies", "gradle")
            val group: String? = task.getGroup();
            if (!group.isNullOrBlank()) {
                if (targetGroups.contains(group)) {
                    targetGroups.get(group)?.add(task.name)
                } else {
                    targetGroups.set(group, mutableListOf<String>(task.name))
                }
            }



            var inputs = task.getInputs().getFiles()
           /*  if (!inputs.isEmpty()) {
                println(inputs)
                inputs.forEach{ input ->
                    println("    > ${input}")
                }
            } */


            val outputs = task.getOutputs().getFiles()
            if (!outputs.isEmpty()) {
                target.put("output", outputs.map { file -> file.path })
            }

            target.put("cache", true)

            val dependsOn = task.getTaskDependencies().getDependencies(task)
            if (!dependsOn.isEmpty()) {
                target.put("dependsOn", dependsOn.map { depTask -> "${depTask.getProject().name}:${depTask.name}" })
            }
            target.put("metadata", metadata)

            taskCommand += task.name
            target.put("command", taskCommand)

            targets.put(task.name, target)
        }

        // println(targetGroups)
        return  targets
    }
}
