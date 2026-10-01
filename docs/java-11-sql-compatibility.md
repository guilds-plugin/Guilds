# SQL backends do not work on Java 11

**Status:** open, pre-existing, not introduced by the async-persistence work.
**Affects:** `MYSQL`, `MARIADB`, `SQLITE`. **Does not affect:** `JSON`.

## What happens

The plugin compiles with `options.release.set(11)` in `build.gradle.kts`, and the build has a
`testJava11` task that runs the suite on a real Java 11 JVM. `jdbi3-core` is pinned at `3.53.0`
(`gradle/libs.versions.toml`) and ships Java 17 bytecode:

```text
$ javap -v org.jdbi.v3.core.Jdbi.class | grep major
  major version: 61          # 61 = Java 17; a Java 11 JVM stops at 55
```

`DatabaseManager` holds a `Jdbi` field, and its constructor runs `Jdbi.create(hikari)` followed by
`jdbi.installPlugin(new SqlObjectPlugin())`, which `GuildAdapter` needs at adapter construction too. So
every SQL path touches a Java 17 class, and on a Java 11 JVM that fails with:

```text
java.lang.UnsupportedClassVersionError: org/jdbi/v3/core/spi/JdbiPlugin has been compiled by a more
recent version of the Java Runtime (class file version 61.0), this version of the Java Runtime only
recognizes class file versions up to 55.0
```

The bytecode version is verified; the trace above is the expected shape of the failure, not output
captured from a Java 11 server, because no Java 11 runtime with a SQL backend is available here. What is
verified is that no SQL path can avoid loading the class: a version 61 class file is definitionally
unloadable by a version 55 JVM, and the Jdbi calls are unconditional. The SQL backends cannot be opened
at all on Java 11.

## Why it has not been noticed

Nothing in the suite exercises a real SQL connection, and no untagged test loads `DatabaseManager`, so
`testJava11` passes on a green build while the runtime path is broken. This branch added two tests that
*do* load it, to assert that a failed backend setup closes its connection pool. They are tagged
`java17classpath` and excluded from `testJava11`, which is the exclusion that surfaces the problem at
all:

```kotlin
tasks.named<Test>("testJava11") {
    useJUnitPlatform { excludeTags(java17ClasspathTag) }
}
```

**The exclusion is not evidence that the SQL path works on Java 11.** It is the opposite: it is the
only part of the suite that touches the problem, and it is excluded because the answer is already known
to be no. Nothing in this repository's tests exercises SQL on Java 11, and a green `testJava11` says
nothing about it.

## Options

1. **Drop Java 11 support.** Paper has required Java 21 since 1.20.5, so a server old enough to run
   Java 11 cannot run a current Paper at all. This is the honest fix if the plugin targets current
   servers: raise `options.release`, drop the `testJava11` task, and remove the `spigot.api.test` pin
   that exists only to keep that task loadable.
2. **Pin an older jdbi.** Versions before the Java 17 bump would load on Java 11. This keeps Java 11
   working and costs an upgrade path on a dependency that is currently current.

Option 1 is the better trade. Option 2 is only worth taking if Java 11 servers are still a target, and
it should come with a real SQL integration test, since nothing would otherwise catch the next bump.

## Not covered here

- No integration test runs against a real MySQL, MariaDB or SQLite database. Every SQL behaviour in
  the suite is verified against mocks, so driver-level and dialect-level behaviour is untested
  regardless of the Java version.
- The `spigot.api.test` pin is 1.16.5, chosen so the test classpath loads on Java 11. It diverges
  from the API the plugin compiles against, and the tests are limited to API stable since 1.8.
