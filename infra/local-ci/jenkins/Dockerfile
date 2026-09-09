FROM jenkins/jenkins:2.568.3-jdk21

USER root

ARG TARGETARCH
ARG DOCKER_VERSION=27.5.1
ARG COMPOSE_VERSION=v2.32.4

RUN apt-get update \
    && apt-get install --yes --no-install-recommends ca-certificates curl \
    && case "$TARGETARCH" in \
         amd64) docker_arch=x86_64; compose_arch=x86_64 ;; \
         arm64) docker_arch=aarch64; compose_arch=aarch64 ;; \
         *) echo "Unsupported architecture: $TARGETARCH" >&2; exit 1 ;; \
       esac \
    && curl --fail --silent --show-error --location \
       "https://download.docker.com/linux/static/stable/${docker_arch}/docker-${DOCKER_VERSION}.tgz" \
       --output /tmp/docker.tgz \
    && tar --extract --gzip --file /tmp/docker.tgz --directory /tmp \
    && mv /tmp/docker/docker /usr/local/bin/docker \
    && mkdir -p /usr/local/lib/docker/cli-plugins \
    && curl --fail --silent --show-error --location \
       "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-${compose_arch}" \
       --output /usr/local/lib/docker/cli-plugins/docker-compose \
    && chmod +x /usr/local/bin/docker /usr/local/lib/docker/cli-plugins/docker-compose \
    && rm -rf /var/lib/apt/lists/* /tmp/docker /tmp/docker.tgz

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY casc.yaml /usr/share/jenkins/ref/casc.yaml
