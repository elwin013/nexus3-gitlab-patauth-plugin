# Changelog

All notable changes should be noted here.

## 3.0.0 - 2025/08/06

* BREAKING: Change principal from GitlabPrincipal class to SimplePrincipalCollection to prevent (de)serialization errors

## 2.0.0 - 2025/01/10

* BREAKING: Use full path of Group (`full_path`) to match against the allowed/mapped groups
* Ignore the username letter case (mimic the GitLab behavior)
* Tested with Nexus3 3.76.0

## 1.0.2 - 2023/08/08

* Bugfix for bearer token realm (including NPM)  - introducing fake user manager 
  for plugin realm to satisfy requirements of bearer token (`UserPrincipalsHelper.findUserManager`)

## 1.0.1 - 2023/07/28

Minor changes:
* added some logs,
* remove not needed wrapper for Jackson object mapper,
* remove not needed parameter in GitlabApi.

## 1.0.0 - 2023/07/27

First release.

