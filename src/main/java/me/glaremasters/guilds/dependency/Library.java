package me.glaremasters.guilds.dependency;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Library {
    private final Collection<String> urls;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final byte[] checksum;
    private final Collection<Relocation> relocations;

    public Library(Collection<String> urls,
                   String groupId,
                   String artifactId,
                   String version,
                   String classifier,
                   byte[] checksum,
                   Collection<Relocation> relocations) {

        this.urls = urls != null ? Collections.unmodifiableList(new LinkedList<>(urls)) : Collections.emptyList();
        this.groupId = requireNonNull(groupId, "groupId");
        this.artifactId = requireNonNull(artifactId, "artifactId");
        this.version = requireNonNull(version, "version");
        this.classifier = classifier;
        this.checksum = requireNonNull(checksum, "checksum");
        this.relocations = relocations != null ? Collections.unmodifiableList(new LinkedList<>(relocations)) : null;
    }

    public Collection<String> getUrls() {
        return urls;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public Collection<Relocation> getRelocations() {
        return relocations;
    }

    public boolean hasRelocations() {
        return !relocations.isEmpty();
    }

    @Override
    public String toString() {
        return "Library{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", classifier='" + classifier + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<String> urls = new ArrayList<>();
        private String groupId;
        private String artifactId;
        private String version;
        private String classifier;
        private byte[] checksum;
        private final List<Relocation> relocations = new ArrayList<>();

        private Builder() {}

        public Builder url(String url) {
            urls.add(requireNonNull(url, "url").toLowerCase());
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = requireNonNull(groupId, "groupId");
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = requireNonNull(artifactId, "artifactId");
            return this;
        }

        public Builder version(String version) {
            this.version = requireNonNull(version, "version");
            return this;
        }

        public Builder classifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder checksum(byte[] checksum) {
            this.checksum = requireNonNull(checksum, "checksum");
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = Base64.getDecoder().decode(requireNonNull(checksum, "checksum"));
            return this;
        }

        public Builder relocate(Relocation relocation) {
            relocations.add(requireNonNull(relocation, "relocation"));
            return this;
        }

        public Builder relocate(String pattern, String relocatedPattern) {
            return relocate(new Relocation(pattern, relocatedPattern));
        }

        public Library build() {
            return new Library(urls, groupId, artifactId, version, classifier, checksum, relocations);
        }
    }
}
