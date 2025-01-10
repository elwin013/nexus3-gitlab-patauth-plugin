package com.elwin013.gitlabpatauth;

import com.elwin013.gitlabpatauth.api.GitlabApi;
import com.elwin013.gitlabpatauth.api.model.Group;
import com.elwin013.gitlabpatauth.api.model.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("GitlabApiWrapper")
public class GitlabApiWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(GitlabApiWrapper.class);

    private final Cache<String, GitlabPrincipal> principalCache;
    private final String minimalAccessLevel;
    private final List<String> groupsAllowed;
    private final List<String> mappedGroups;
    private final String gitlabUrl;

    @Inject
    public GitlabApiWrapper(GitlabPatAuthConfig config) {
        this.gitlabUrl = config.getGitlabUrl();
        this.minimalAccessLevel = config.getMinimalAccessLevel();
        this.groupsAllowed = config.getGroupsAllowed();
        this.mappedGroups = config.getMappedGroups();
        this.principalCache = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getLoginCacheTtl())
                .build();
    }

    public GitlabPrincipal login(String username, String personalAccessToken) {
        String cacheKey = username + "|" + personalAccessToken;
        GitlabPrincipal cachedLogin = principalCache.getIfPresent(cacheKey);
        if (cachedLogin != null) {
            return cachedLogin;
        }
        try (GitlabApi api = new GitlabApi(gitlabUrl, personalAccessToken)) {
            User currentUser = api.getUserInfo();

            if (currentUser == null || !username.equalsIgnoreCase(currentUser.getUsername())) {
                return null;
            }
            Set<Group> groups = api.getUserGroups(minimalAccessLevel);

            if (!groupsAllowed.isEmpty() &&
                    groups.stream().noneMatch(group -> groupsAllowed.contains(group.getFullPath()))) {
                return null;
            }

            if (!mappedGroups.isEmpty()) {
                groups = groups.stream().filter(group -> mappedGroups.contains(group.getFullPath())).collect(Collectors.toSet());
            }

            GitlabPrincipal principal = new GitlabPrincipal(
                    currentUser.getEmail(),
                    currentUser.getUsername(),
                    personalAccessToken,
                    groups.stream()
                            .map(group -> new GitlabPrincipal.Group(group.getFullPath(), group.getName()))
                            .collect(Collectors.toSet())
            );

            principalCache.put(cacheKey, principal);
            return principal;
        } catch (IOException e) {
            LOG.error(String.format("Authorization for user %s failed", username), e);
            return null;
        }
    }
}
