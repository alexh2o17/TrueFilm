name := "TrueFilm"

version := "0.1"

scalaVersion := "2.12.10"

val CirceVersion      = "0.13.0"
val DoobieVersion     = "0.9.4"
val ZIOVersion        = "1.0.4"
val PureConfigVersion = "0.14.0"
val ZIOInterop        = "2.2.0.1"


libraryDependencies ++= Seq(
  // ZIO
  "dev.zio"          %% "zio"              % ZIOVersion,
  "dev.zio"          %% "zio-streams"      % ZIOVersion,
  "dev.zio"          %% "zio-interop-cats" % ZIOInterop,
  "dev.zio"          %% "zio-test"         % ZIOVersion % "test",
  "dev.zio"          %% "zio-test-sbt"     % ZIOVersion % "test",
  "com.nrinaudo" %% "kantan.csv" % "0.6.1",
  // Circe
  "io.circe" %% "circe-generic"        % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion,
  // Doobie
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-h2"   % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres"   % DoobieVersion,
  "org.postgresql" % "postgresql" % "42.2.18",
  "com.opentable.components" % "otj-pg-embedded" % "0.13.3" % Test,
  //fs2
  "co.fs2" %% "fs2-core" % "2.4.4",
  "co.fs2" %% "fs2-io" % "2.4.4",
  "org.gnieh" %% "fs2-data-csv" % "0.9.0",
  "org.gnieh" %% "fs2-data-xml" % "0.9.0",

//pure config
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  // log4j
  "org.slf4j" % "slf4j-log4j12" % "1.7.30",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))