package me.glaremasters.guilds.conf.objects

internal data class BuffNav(
    var next: BuffNavItem = BuffNavItem("EMPTY_MAP", "Next"),
    var previous: BuffNavItem = BuffNavItem("EMPTY_MAP", "Previous")
)
