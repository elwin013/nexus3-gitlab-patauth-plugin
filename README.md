# nexus3-gitlab-patauth-plugin

A plugin for Sonatype Nexus OSS that adds an authentication realm allowing to authenticate using GitLab username and
personal access token (PAT) with scopes:
* `read_user` to get information about the user,
* `read_api` to get information about available groups for the user who is logging in.

Successfully logged-in user groups will be mapped into roles (if a role in Nexus is created). For more details on that
and additional configuration - see below.

The reason for `read_api` scope is security - it is safer to use only non-admin user credentials in the other services.

Verified working with Nexus 3.42.0, 3.56.0 and 3.58.1.

## Setup

### Installation

In below steps `${NEXUS_DIR}` is a Nexus directory with binaries. In most cases it is `/opt/sonatype/nexus` or `/opt/nexus`.

#### 1. Add plugin to Nexus

In general, do the steps mentioned in [this nexus development guide](https://sonatype-nexus-community.github.io/nexus-development-guides/plugin-install.html).

Simplest solution is to run single command to download `jar` into the `${NEXUS_DIR}/deploy` folder.
Example of command for current version below - it assumes that your `${NEXUS_DIR}` is `/opt/sonatype/nexus`.
Please note that in some cases it can be `/opt/nexus` - you should tweak it accordingly.

```sh
wget -O /opt/sonatype/nexus/deploy/nexus3-gitlab-patauth-plugin-1.0.2.jar https://github.com/elwin013/nexus3-gitlab-patauth-plugin/releases/download/v1.0.2/nexus3-gitlab-patauth-plugin-1.0.2.jar
```

#### 2. Create configuration

Create `${NEXUS_DIR}/etc/gitlabpatauth.properties` configuration. You can copy `gitlabpatauth.properties.README`
from this repository and tweak it to your needs. For details see the aforementioned file and _Configuration_ section
below.

#### 3. Restart nexus :-)

Restart Nexus to let it pick up the newly installed plugin.

#### 4. Enable _GitLab PAT Authentication Realm_

Go to Nexus Administration -> Security -> Realms and enable _GitLab PAT Authentication Realm_ (move it to the right) and
save. It should go after the built-in realms.

#### 5. Create roles that should be mapped and add permissions

Go to Administration -> Security -> Roles and create any roles (with type "Nexus role") that you want to be mapped.
Please note that `id` of the role needs to be equal to the path of the GitLab group.

### Configuration

Please see `gitlabpatauth.properties.README` for the properties file with default values. Copy this file to
`${NEXUS_DIR}/etc/gitlabpatauth.properties` and tweak them accordingly.

Available properties:
* `gitlab.url` - URL of Gitlab instance
* `gitlab.cache-ttl-minutes` - Time to cache login info of a user
* `gitlab.group-minimal-access-level` - Minimal user's access level for group - groups, where the user has access above
   or equal to this level, will be fetched from GitLab
* `gitlab.groups-allowed` - Comma separated list of allowed groups to login
* `gitlab.groups-mapped` - Comma separated list of mapped groups to roles

For default values please see `gitlabpatauth.properties.README` file.

## Usage

### Generate Personal Access Token

Below steps must be done by every user who wants to access Nexus:
1. Go to `${GITLAB_URL}/-/profile/personal_access_tokens` (where `${GITLAB_URL}` is your GitLab instance)
2. Add a new personal access token with any name scopes `read_user` and `read_api`
3. Save the generated token

### Login to Nexus

After generating the token users can log in to Nexus using their Gitlab username and token. That pair can be also used
for logging in to download dependencies (for example in Maven's `settings.xml` or when using `npm login`).

## Development

For development purposes, there is Dockerfile and docker-compose.yml file in this repository.  This allows quickly
spinning up a Nexus instance with a preinstalled plugin. Nexus instance will be available on port 8081 and debugging port
(for Java remote debug) is 8082.

Docker compose configuration creates docker volume for `nexus-data` - thanks to that there is no need to set up
Nexus from scratch every time the container is recreated.

There are run configurations for Intellij that allow to spin up it with one click (`run nexus with plugin` which
run packaging and recreates the container) and connect remote debugging (`debug plugin` configuration).

To build a plugin run `mvn clean package`.

## License

[MIT](LICENSE)