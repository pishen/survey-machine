name := "survey-machine"

version := "1.0"

libraryDependencies ++= Seq(
  "org.neo4j" % "neo4j" % "2.0.1",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.rockymadden.stringmetric" %% "stringmetric-core" % "0.27.2",
  "org.apache.lucene" % "lucene-core" % "4.6.1",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.6.1",
  "org.apache.lucene" % "lucene-queryparser" % "4.6.1"
)
