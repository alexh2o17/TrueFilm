ARG OPENJDK_TAG=8u232
FROM openjdk:${OPENJDK_TAG}
ARG SBT_VERSION=1.4.7

RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt

FROM mozilla/sbt
WORKDIR /app
COPY . .
RUN sbt compile
CMD ["sbt clean test", "sbt run"]


