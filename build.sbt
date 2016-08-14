organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers += "spray repo" at "http://repo.spray.io"

//http://www.scala-sbt.org/1.0/docs/Library-Dependencies.html#The++key
libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    //Spray
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-http" % sprayV,
    "io.spray"            %%  "spray-httpx" % sprayV,
    "io.spray"            %%  "spray-util" % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.2",
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    //akka
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-slf4j"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    //Unit test
    "org.specs2"          %% "specs2-core"    % "3.8.4" % "test",
    "org.specs2"          %% "specs2-mock" % "3.8.4" % "test",
    "org.mockito"         % "mockito-core" % "1.10.19" % "test",
    //db
    "org.reactivemongo"   %%  "reactivemongo" % "0.11.14",
    //logging
    "ch.qos.logback" %  "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
    //Disambiguation
    "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4",
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
  )

}

Revolver.settings
