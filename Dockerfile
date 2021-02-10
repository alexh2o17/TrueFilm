FROM mozilla/sbt AS BUILD
WORKDIR /app
COPY . .
RUN sbt compile
CMD ["sbt run", "sbt clean test"]
