logLevel := Level.Warn

resolvers += "spray repo" at "http://repo.spray.io"

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("io.spray" % "sbt-twirl" % "0.7.0")