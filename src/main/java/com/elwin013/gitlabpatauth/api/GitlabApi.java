package com.elwin013.gitlabpatauth.api;

import com.elwin013.gitlabpatauth.api.model.Group;
import com.elwin013.gitlabpatauth.api.model.User;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GitlabApi implements Closeable {
    public static final String PRIVATE_TOKEN = "Private-Token";
    private static final String API_NAMESPACE = "/api/v4";
    private static final TypeReference<Set<Group>> gitlabGroupTyperef = new TypeReference<Set<Group>>() {
    };
    private final HttpUtil httpClient;
    private final String hostUrl;
    private final String token;
    private final String username;

    public GitlabApi(String hostUrl, String username, String personalAccessToken) {
        this.httpClient = new HttpUtil();
        this.hostUrl = hostUrl;
        this.username = username;
        this.token = personalAccessToken;
    }

    public User getUserInfo() {
        try {
            String resultStr = httpClient.get(
                    hostUrl + API_NAMESPACE + Endpoints.USER,
                    HttpUtil.getMap(PRIVATE_TOKEN, token),
                    Collections.emptyMap()
            );

            if (resultStr != null) {
                return JacksonObjectMapper.get().readValue(resultStr, User.class);
            } else {
                return null;
            }
        } catch (IOException | URISyntaxException e) {
            return null;
        }
    }

    public Set<Group> getUserGroups(String minimalAccessLevel) {
        Set<Group> groups = new HashSet<>();
        String baseUrl = hostUrl + API_NAMESPACE + Endpoints.GROUPS + "?per_page=100";
        try {
            Set<Group> page = null;
            int pageNumber = 1;
            do {
                String resultStr = httpClient.get(
                        baseUrl + getParametersForGroupEndpoint(minimalAccessLevel, pageNumber),
                        HttpUtil.getMap(PRIVATE_TOKEN, token),
                        Collections.emptyMap()
                );

                if (resultStr != null) {
                    page = JacksonObjectMapper.get().readValue(resultStr, gitlabGroupTyperef);
                    groups.addAll(page);
                }
                pageNumber++;
            } while(page != null && !page.isEmpty());

        } catch (IOException | URISyntaxException e) {
            return Collections.emptySet();
        }

        return groups;

    }

    private String getParametersForGroupEndpoint(String minimalAccessLevel, int pageNumber) {
        return "&page=" + pageNumber + (minimalAccessLevel != null ? "&min_access_level=" + minimalAccessLevel : "");
    }

    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }


    private static final class Endpoints {
        static final String USER = "/user";
        static final String GROUPS = "/groups";
    }

}
