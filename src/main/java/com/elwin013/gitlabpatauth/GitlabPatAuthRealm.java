package com.elwin013.gitlabpatauth;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.nexus.security.user.UserStatus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Named
@Description("GitLab PAT Authentication Realm")
public class GitlabPatAuthRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(GitlabPatAuthRealm.class);
    public static final String NAME = "GitlabPatAuthRealm";
    private final GitlabApiWrapper apiWrapper;
    private final UserManager userManager;


    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    public GitlabPatAuthRealm(GitlabApiWrapper apiWrapper, UserManager userManager) {
        this.apiWrapper = apiWrapper;
        this.userManager = userManager;
        this.setName(NAME);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        AuthorizationInfo info = null;
        Object principal = principalCollection.getPrimaryPrincipal();
        if (principal instanceof GitlabPrincipal) {
            GitlabPrincipal gitlabPrincipal = (GitlabPrincipal) principal;
            LOG.info("doGetAuthorizationInfo for user {} with roles {}", gitlabPrincipal.getUsername(), gitlabPrincipal.getGroups());
            info = new SimpleAuthorizationInfo(gitlabPrincipal.getGroups().stream().map(GitlabPrincipal.Group::getPath).collect(Collectors.toSet()));
        }
        return info;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String username = token.getUsername();
        String password = String.valueOf((char[]) token.getCredentials());

        GitlabPrincipal principal = apiWrapper.login(username, password);
        if (principal != null) {
            createOrUpdateUser(principal);
            return new SimpleAuthenticationInfo(principal, password, getName());
        }
        return null;
    }

    private void createOrUpdateUser(GitlabPrincipal principal) {
        String username = principal.getUsername();
        boolean userExists = userManager.listUserIds().contains(username);
        if (!userExists) {
            creatUser(principal);
        } else {
            updateUser(principal);
        }
    }

    private void updateUser(GitlabPrincipal principal) {
        try {
            User user = userManager.getUser(principal.getUsername());
            user.setRoles(principal.getGroups().stream().map(
                    s -> new RoleIdentifier(UserManager.DEFAULT_SOURCE, s.getPath())
            ).collect(Collectors.toSet()));
            userManager.updateUser(user);
        } catch (UserNotFoundException e) {
            // should not happen
        }
    }

    private void creatUser(GitlabPrincipal principal) {
        User user = new User();
        user.setUserId(principal.getUsername());
        user.setFirstName(principal.getUsername());
        user.setLastName(principal.getUsername());
        user.setStatus(UserStatus.active);
        user.setEmailAddress(principal.getEmail());
        user.setRoles(principal.getGroups().stream().map(
                s -> new RoleIdentifier(UserManager.DEFAULT_SOURCE, s.getPath())
        ).collect(Collectors.toSet()));
        // random UUID as a password to disallow changing password by user
        userManager.addUser(user, UUID.randomUUID().toString());
    }
}
