package com.elwin013.gitlabpatauth;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Singleton
@Named
public class GitlabPatAuthConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GitlabPatAuthConfig.class);
    private static final String CONFIG_FILE_NAME = "gitlabpatauth.properties";
    private static final String DEFAULT_GITLAB_URL = "https://gitlab.com";
    private static final String DEFAULT_GITLAB_LOGIN_CACHE_TTL = "1";
    private static final String DEFAULT_GROUP_MINIMAL_ACCESS_LEVEL_KEY = "10"; // Guest

    private static final String GITLAB_URL_KEY = "gitlab.url";
    private static final String LOGIN_CACHE_TTL_KEY = "gitlab.cache-ttl-minutes";
    private static final String GROUP_MINIMAL_ACCESS_LEVEL_KEY = "gitlab.group-minimal-access-level";
    private static final String GROUPS_ALLOWED_KEY = "gitlab.groups-allowed";
    private static final String GROUPS_MAPPED_KEY = "gitlab.groups-mapped";

    private final Properties props;

    public GitlabPatAuthConfig() {
        props = new Properties();
        Path path = Paths.get(".", "etc", CONFIG_FILE_NAME);
        InputStream io = null;
        try {
            io = Files.newInputStream(path);
            props.load(io);
            LOG.info("Using custom values: gitlab.url={}, gitlab.cache-ttl-minutes={}.", getGitlabUrl(), getLoginCacheTtl());
        } catch (IOException e) {
            LOG.warn("Cannot read properties from path {}", path.toAbsolutePath());
            LOG.warn("Using default values: gitlab.url={}, gitlab.cache-ttl-minutes={}.", DEFAULT_GITLAB_URL, DEFAULT_GITLAB_LOGIN_CACHE_TTL);
        } finally {
            if (io != null) {
                try {
                    io.close();
                } catch (IOException e) {
                    LOG.warn("Cannot close input stream");
                }
            }
        }
    }

    public String getGitlabUrl() {
        return props.getProperty(GITLAB_URL_KEY, DEFAULT_GITLAB_URL);
    }

    public Duration getLoginCacheTtl() {
        try {
            return Duration.parse("PT" + props.getProperty(LOGIN_CACHE_TTL_KEY, DEFAULT_GITLAB_LOGIN_CACHE_TTL) + "M");
        } catch (Exception e) {
            LOG.error("Error while parsing value for LOGIN_CACHE_TTL",e);
            return Duration.parse("PT" + DEFAULT_GITLAB_LOGIN_CACHE_TTL + "M");
        }
    }

    public String getMinimalAccessLevel() {
        return props.getProperty(GROUP_MINIMAL_ACCESS_LEVEL_KEY, DEFAULT_GROUP_MINIMAL_ACCESS_LEVEL_KEY);
    }

    public List<String> getGroupsAllowed() {
        return splitPropertyToList(GROUPS_ALLOWED_KEY);
    }

    public List<String> getMappedGroups() {
        return splitPropertyToList(GROUPS_MAPPED_KEY);
    }

    private List<String> splitPropertyToList(String propertyKey) {
        String prop = props.getProperty(propertyKey);
        if (prop == null || prop.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(prop.split(","));
        }
    }
}
