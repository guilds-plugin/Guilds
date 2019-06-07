package me.glaremasters.guilds.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Relocation {
    private final String pattern;
    private final String relocatedPattern;
    private final Collection<String> includes;
    private final Collection<String> excludes;

    private Relocation(String pattern, String relocatedPattern, Collection<String> includes, Collection<String> excludes) {
        this.pattern = requireNonNull(pattern, "pattern").replace("{}", ".");
        this.relocatedPattern = requireNonNull(relocatedPattern, "relocatedPattern").replace("{}", ".");
        this.includes = includes != null ? Collections.unmodifiableList(new LinkedList<>(includes)) : Collections.emptyList();
        this.excludes = excludes != null ? Collections.unmodifiableList(new LinkedList<>(excludes)) : Collections.emptyList();
    }

    public Relocation(String pattern, String relocatedPattern) {
        this(pattern, relocatedPattern, null, null);
    }

    public String getPattern() {
        return pattern;
    }

    public String getRelocatedPattern() {
        return relocatedPattern;
    }

    public Collection<String> getIncludes() {
        return includes;
    }

    public Collection<String> getExcludes() {
        return excludes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pattern;
        private String relocatedPattern;
        private final List<String> includes = new ArrayList<>();
        private final List<String> excludes = new ArrayList<>();

        private Builder() {}

        public Builder pattern(String pattern) {
            this.pattern = requireNonNull(pattern, "pattern");
            return this;
        }

        public Builder relocatedPattern(String relocatedPattern) {
            this.relocatedPattern = requireNonNull(relocatedPattern, "relocatedPattern");
            return this;
        }

        public Builder include(String include) {
            includes.add(requireNonNull(include, "include"));
            return this;
        }

        public Builder exclude(String exclude) {
            excludes.add(requireNonNull(exclude, "exclude"));
            return this;
        }

        public Relocation build() {
            return new Relocation(pattern, relocatedPattern, includes, excludes);
        }
    }
}
