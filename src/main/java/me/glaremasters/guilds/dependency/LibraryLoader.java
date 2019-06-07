package me.glaremasters.guilds.dependency;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import static java.util.Objects.requireNonNull;

public class LibraryLoader {
    private static Method classLoaderAddUrlMethod;

    private static Constructor<?> jarRelocatorConstructor;
    private static Method jarRelocatorRunMethod;

    private static Constructor<?> relocationConstructor;

    private final Plugin plugin;
    private final URLClassLoader classLoader;
    private final Path saveDirectory;
    private final List<String> repositories = new ArrayList<>();

    public LibraryLoader(Plugin plugin) {
        this.plugin = requireNonNull(plugin, "module");

        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Unsupported class loader, URLClassLoader is required");
        }

        this.classLoader = (URLClassLoader) classLoader;
        saveDirectory = plugin.getDataFolder().toPath().resolve("lib");
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void addRepository(String url) {
        repositories.add(requireNonNull(url, "url").endsWith("/") ? url.substring(0, url.length() - 1) : url);
    }

    public void relocate(Path input, Path output, Collection<Relocation> relocations) {
        requireNonNull(input, "input");
        requireNonNull(output, "output");
        requireNonNull(relocations, "relocations");

        initializeJarRelocator();

        try {
            List<Object> objects = new ArrayList<>();
            for (Relocation relocation : relocations) {
                String pattern = relocation.getPattern();
                String relocatedPattern = relocation.getRelocatedPattern();
                Collection<String> includes = relocation.getIncludes();
                Collection<String> excludes = relocation.getExcludes();

                objects.add(relocationConstructor.newInstance(pattern, relocatedPattern, includes, excludes));
            }

            jarRelocatorRunMethod.invoke(jarRelocatorConstructor.newInstance(input.toFile(), output.toFile(), objects));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public Path downloadLibrary(Library library) {
        String path = requireNonNull(library, "library").getGroupId().replace('.', '/') + '/' + library.getArtifactId() + '/' + library.getVersion();
        String name = library.getArtifactId() + '-' + library.getVersion() + ".jar";

        Path file = saveDirectory.resolve(path + '/' + name);
        if (Files.exists(file)) {
            return file;
        }

        String remotePath = path + '/' + library.getArtifactId() + '-' + library.getVersion();
        if (library.getClassifier() != null) {
            remotePath += '-' + library.getClassifier();
        }
        remotePath += ".jar";

        List<String> urls = new LinkedList<>(library.getUrls());
        for (String repository : repositories) {
            urls.add(repository + '/' + remotePath);
        }
        urls.add("https://repo1.maven.org/maven2/" + remotePath);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Path dir = file.getParent();

        for (String url : urls) {
            plugin.getLogger().info("Downloading library " + url);

            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "LibraryLoader");

                try (InputStream in = connection.getInputStream()) {
                    int len;
                    byte[] buf = new byte[4096];
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }

                    byte[] jar = out.toByteArray();
                    byte[] checksum = digest.digest(jar);
                    if (!Arrays.equals(checksum, library.getChecksum())) {
                        plugin.getLogger().warning("** INVALID CHECKSUM **");
                        plugin.getLogger().warning("Expected: " + Base64.getEncoder().encodeToString(library.getChecksum()));
                        plugin.getLogger().warning("Returned: " + Base64.getEncoder().encodeToString(checksum));
                        continue;
                    }

                    Files.createDirectories(dir);

                    Path tmpFile = dir.resolve(name + ".tmp");
                    tmpFile.toFile().deleteOnExit();

                    try {
                        Files.write(tmpFile, jar);
                        Files.move(tmpFile, file);

                        return file;
                    } finally {
                        try {
                            Files.deleteIfExists(tmpFile);
                        } catch (IOException e) {
                            plugin.getLogger().log(Level.WARNING, "Couldn't delete temporary file: " + tmpFile, e);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    plugin.getLogger().warning(connection.getURL().getHost() + ": " + e.getMessage());
                } catch (FileNotFoundException ignored) {
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Couldn't download " + url + " due to an unexpected error!", e);
            }
        }

        throw new LibraryNotFoundException(library.toString());
    }

    public void loadLibrary(Path path) {
        requireNonNull(path, "path");

        synchronized (LibraryLoader.class) {
            if (classLoaderAddUrlMethod == null) {
                try {
                    classLoaderAddUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    classLoaderAddUrlMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            classLoaderAddUrlMethod.invoke(classLoader, path.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public void loadLibrary(Library library) {
        Path file = downloadLibrary(requireNonNull(library, "library"));

        if (library.hasRelocations()) {
            String name = library.getArtifactId() + '-' + library.getVersion() + "-relocated.jar";

            Path dir = file.getParent();
            Path relocatedFile = dir.resolve(name);

            if (!Files.exists(relocatedFile)) {
                Path tmpFile = dir.resolve(name + ".tmp");

                try {
                    relocate(file, tmpFile, library.getRelocations());
                    Files.move(tmpFile, relocatedFile);

                    file = relocatedFile;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        Files.deleteIfExists(tmpFile);
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.WARNING, "Couldn't delete temporary file: " + tmpFile, e);
                    }
                }
            } else {
                file = relocatedFile;
            }
        }

        loadLibrary(file);
    }

    /**
     * Initializes Luck's Jar Relocator library used to perform jar relocations against downloaded libraries.
     *
     * The relocator library and its dependencies are automatically downloaded if needed. The relocator classes are
     * loaded by an isolated class loader to prevent polluting the class space of other plugins.
     */
    private void initializeJarRelocator() {
        synchronized (LibraryLoader.class) {
            if (jarRelocatorConstructor == null) {
                try {
                    // Download the jar relocator library and its dependencies.
                    URL[] urls = new URL[] {
                            downloadLibrary(
                                    Library.builder()
                                            .groupId("org.ow2.asm")
                                            .artifactId("asm")
                                            .version("6.0")
                                            .checksum("3Ylxx0pOaXiZqOlcquTqh2DqbEhtxrl7F5XnV2BCBGE=")
                                            .build()).toUri().toURL(),

                            downloadLibrary(
                                    Library.builder()
                                            .groupId("org.ow2.asm")
                                            .artifactId("asm-commons")
                                            .version("6.0")
                                            .checksum("8bzlxkipagF73NAf5dWa+YRSl/17ebgcAVpvu9lxmr8=")
                                            .build()).toUri().toURL(),

                            downloadLibrary(
                                    Library.builder()
                                            .groupId("me.lucko")
                                            .artifactId("jar-relocator")
                                            .version("1.3")
                                            .checksum("mmz3ltQbS8xXGA2scM0ZH6raISlt4nukjCiU2l9Jxfs=")
                                            .build()).toUri().toURL(),
                    };

                    // Create an isolated class loader to prevent conflicts with other plugins.
                    ClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader().getParent());

                    Class<?> jarRelocatorClass = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
                    Class<?> relocationClass = classLoader.loadClass("me.lucko.jarrelocator.Relocation");

                    Constructor<?> jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);
                    Method jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");

                    Constructor<?> relocationConstructor = relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class);

                    // Finally we do the field assignments.
                    // To prevent possibly corrupting state, this should always be the last thing we do!
                    LibraryLoader.jarRelocatorConstructor = jarRelocatorConstructor;
                    LibraryLoader.jarRelocatorRunMethod = jarRelocatorRunMethod;
                    LibraryLoader.relocationConstructor = relocationConstructor;
                } catch (MalformedURLException | ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
