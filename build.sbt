name := "TrueFilm"

version := "0.1"

scalaVersion := "2.12.10"

val CirceVersion      = "0.13.0"
val DoobieVersion     = "0.9.4"
val ZIOVersion        = "1.0.4"
val PureConfigVersion = "0.14.0"

libraryDependencies ++= Seq(
  // ZIO
  "dev.zio"          %% "zio"              % ZIOVersion,
  "dev.zio"          %% "zio-test"         % ZIOVersion % "test",
  "dev.zio"          %% "zio-test-sbt"     % ZIOVersion % "test",
  // Circe
  "io.circe" %% "circe-generic"        % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion,
  // Doobie
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-h2"   % DoobieVersion,
  //pure config
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  // log4j
  "org.slf4j" % "slf4j-log4j12" % "1.7.30",
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))