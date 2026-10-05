package me.glaremasters.guilds.conf.objects

data class GuildBuff(
    var identifier: String = "",
    var locked: BuffSettings = BuffSettings("Special Buff", "FEATHER", listOf("")),
    var unlocked: BuffSettings = BuffSettings("Special Buff", "FEATHER", listOf("")),
    var price: Double = 200.00,
    var effects: List<String> = listOf("FAST_DIGGING;1;60", "SPEED;2;30"),
    var permission: String = "example.perm.here",
    var clicker: BuffCommand = BuffCommand(true, listOf("")),
    var guild: BuffCommand = BuffCommand(true, listOf(""))
)
