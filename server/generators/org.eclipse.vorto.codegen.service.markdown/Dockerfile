FROM java:8
VOLUME /tmp/generator/markdown
ADD target/vorto-markdown.jar .
ENTRYPOINT /bin/bash -c "sleep 100; java -jar vorto-markdown.jar --spring.profiles.active=docker"
