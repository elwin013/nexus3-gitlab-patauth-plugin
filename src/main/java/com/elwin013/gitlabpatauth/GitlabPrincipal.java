package com.elwin013.gitlabpatauth;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class GitlabPrincipal implements Serializable {
    private final String email;
    private final String username;
    private final String password;
    private final Set<Group> groups;

    public GitlabPrincipal(String email, String username, String password, Set<Group> groups) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.groups = groups;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return username;
    }

    public static class Group implements Serializable {
        private final String path;
        private final String name;

        public Group(String path, String name) {
            this.path = path;
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return Objects.equals(path, group.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path);
        }

        @Override
        public String toString() {
            return "Group{" +
                    "path='" + path + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}