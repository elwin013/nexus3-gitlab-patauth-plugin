FROM sonatype/nexus3:3.67.0

ARG RELEASE_TAG
ARG PLUGIN_NAME
ARG PACKAGE
ARG PACKAGE_DOT

USER root
RUN mkdir -p /opt/sonatype/nexus/system/${PACKAGE}/${PLUGIN_NAME}/${RELEASE_TAG}/
COPY target/feature/feature.xml /opt/sonatype/nexus/system/${PACKAGE}/${PLUGIN_NAME}/${RELEASE_TAG}/${PLUGIN_NAME}-${RELEASE_TAG}-features.xml
COPY pom.xml /opt/sonatype/nexus/system/${PACKAGE}/${PLUGIN_NAME}/${RELEASE_TAG}/${PLUGIN_NAME}-${RELEASE_TAG}.pom
RUN echo '<?xml version="1.0" encoding="UTF-8"?><metadata><groupId>${PACKAGE_DOT}</groupId><artifactId>${PLUGIN_NAME}</artifactId><versioning><release>${RELEASE_TAG}</release><versions><version>${RELEASE_TAG}</version></versions><lastUpdated>20230130132608</lastUpdated></versioning></metadata>' > /opt/sonatype/nexus/system/${PACKAGE}/${PLUGIN_NAME}/maven-metadata-local.xml
RUN echo "mvn\:${PACKAGE_DOT}/${PLUGIN_NAME}/${RELEASE_TAG} = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties

ENV INSTALL4J_ADD_VM_PARAMS="-Xms2703m -Xmx2703m -XX:MaxDirectMemorySize=2703m -Djava.util.prefs.userRoot=${NEXUS_DATA}/javaprefs -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8082"

USER nexus