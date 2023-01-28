package me.glaremasters.guilds.conf.objects

internal data class MemberNav(
    var next: MemberNavItem = MemberNavItem("EMPTY_MAP", "Next"),
    var previous: MemberNavItem = MemberNavItem("EMPTY_MAP", "Previous")
)