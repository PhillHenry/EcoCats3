val Http4sVersion          = "0.23.7"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"
val Fs2KafkaVersion        = "3.0.0-M9"

lazy val root = (project in file("."))
  .settings(
    organization := "uk.co.odinconsultants",
    name         := "ecocats3",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "3.1.0",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "org.scalameta"         %% "munit"               % MunitVersion           % Test,
      "org.typelevel"         %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"         % "logback-classic"     % LogbackVersion,
      "com.github.fd4s"       %% "fs2-kafka"           % Fs2KafkaVersion,
    ),
    testFrameworks += new TestFramework("munit.Framework"),
  )
