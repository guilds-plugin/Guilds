package me.glaremasters.guilds.conf.objects

data class BuffCommand(
    var enabled: Boolean = true,
    var commands: List<String> = listOf("")
)
