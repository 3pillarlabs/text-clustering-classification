name := """information-palace"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.play" %% "play-slick" % "1.1.0",
  "mysql" % "mysql-connector-java" % "5.1.37",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.0",
  "com.rometools" % "rome" % "1.5.1",
  "org.jsoup" % "jsoup" % "1.8.3"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := false
