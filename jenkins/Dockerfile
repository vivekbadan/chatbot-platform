# FROM jenkins/jenkins:lts-jdk17

# USER root

# RUN apt-get update \
#     && apt-get install -y --no-install-recommends docker.io \
#     && rm -rf /var/lib/apt/lists/*

# COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
# RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# USER jenkins

FROM jenkins/jenkins:lts-jdk17

USER root

RUN apt-get update && \
    apt-get install -y docker.io docker-cli && \
    which docker && \
    docker --version && \
    java -version && \
    rm -rf /var/lib/apt/lists/*

# Keep plugins.txt in project, but skip plugin pre-install for now
# COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
# RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

USER jenkins

