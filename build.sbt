lazy val root = (project in file(".")).
  settings(
    name := "actor-macro",
    version := "0.0.1",
    scalaVersion := "2.11.7"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka"           %%  "akka-actor"                  % "2.4.1",
  "com.typesafe.akka"           %%  "akka-stream-experimental"    % "2.0.3",
  "com.twitter"                 %%  "util-collection"             % "6.27.0"
)