name := "activator-oauth2-api"

version := "1.0"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Spray Repo" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  val sprayVersion = "1.3.3"
  val json4sVersion = "3.2.11"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,
    "org.json4s" %% "json4s-native" % json4sVersion,
    "org.json4s" %% "json4s-ext" % json4sVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.github.nscala-time" %% "nscala-time" % "1.2.0",
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "org.clapper" %% "grizzled-slf4j" % "1.0.2",
    "ch.qos.logback" % "logback-classic" % "1.0.0",
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "io.spray" %% "spray-testkit" % sprayVersion % "test"
  )
}
