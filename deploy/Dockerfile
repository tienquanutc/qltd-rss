FROM adoptopenjdk/openjdk8-openj9:jdk8u212-b03_openj9-0.14.0-alpine-slim
RUN mkdir /app
WORKDIR /app/
ADD . /app/
EXPOSE 5000
CMD java -Dapp.name=quannt-rss -Xmx1g -XX:+IdleTuningGcOnIdle -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar qldt-rss.jar
