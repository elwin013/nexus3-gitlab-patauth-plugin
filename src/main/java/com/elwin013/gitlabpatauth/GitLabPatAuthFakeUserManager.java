package com.elwin013.gitlabpatauth;

import org.sonatype.nexus.security.config.CUser;
import org.sonatype.nexus.security.config.CUserRoleMapping;
import org.sonatype.nexus.security.config.SecurityConfigurationManager;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.*;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Fake user manager based on default AbstractReadOnlyUserManager to provide
 * possibility to log in using NPM Bearer Token Realm.</p>
 *
 * <p>User manager is required to satisfy UserPrincipalsHelper.findUserManager used in
 * authorizing token realm.</p>
 */
@Typed(UserManager.class)
@Named("Fake GitLab PAT Auth Source")
public class GitLabPatAuthFakeUserManager extends AbstractReadOnlyUserManager {
    final SecurityConfigurationManager configuration;

    @Inject
    public GitLabPatAuthFakeUserManager(final SecurityConfigurationManager configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public String getSource() {
        return DEFAULT_SOURCE;
    }

    @Override
    public String getAuthenticationRealmName() {
        return GitlabPatAuthRealm.NAME;
    }

    @Override
    public Set<User> listUsers() {
        return new HashSet<>();
    }

    @Override
    public Set<String> listUserIds() {
        return new HashSet<>();
    }

    @Override
    public Set<User> searchUsers(UserSearchCriteria criteria) {
        return new HashSet<>();
    }

    @Override
    public User getUser(String userId) throws UserNotFoundException {
        // userId is username
        CUser cUser = configuration.readUser(userId);
        if (cUser == null) {
            return null;
        }

        return toUser(userId, cUser);
    }

    public User getUser(String userId, Set<String> set) throws UserNotFoundException {
        return this.getUser(userId);
    }

    @Override
    public boolean isConfigured() {
        return true;
    }


    private User toUser(String userId, CUser cUser) {
        User user = new User();
        user.setUserId(cUser.getId());
        user.setVersion(cUser.getVersion());
        user.setFirstName(cUser.getFirstName());
        user.setLastName(cUser.getLastName());
        user.setEmailAddress(cUser.getEmail());
        user.setSource(DEFAULT_SOURCE);
        user.setStatus(UserStatus.valueOf(cUser.getStatus()));

        try {
            CUserRoleMapping rolesForUser = configuration.readUserRoleMapping(userId, DEFAULT_SOURCE);
            if (rolesForUser != null) {
                Set<RoleIdentifier> roles = new HashSet<>();
                for (String role : rolesForUser.getRoles()) {
                    roles.add(new RoleIdentifier(role, DEFAULT_SOURCE));
                }
                user.setRoles(roles);
            }
        } catch (NoSuchRoleMappingException e) {
            log.debug("Cannot get role mapping for user {}", userId);
        }
        return user;
    }
}
