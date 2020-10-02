import play.sbt.PlayImport.javaJdbc

name := """translatr"""

version := "3.0.3"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .enablePlugins(PlayJava, PlayEbean, BuildInfoPlugin)
  .settings(
    scalaVersion := "2.13.2",
    Defaults.itSettings,
    buildInfoKeys := Seq[BuildInfoKey](name, version)
  )

libraryDependencies ++= Seq(
  javaJdbc,

  guice,

  "com.typesafe.play" %% "play-json" % "2.8.1",
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-joda
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.10.5",

  ehcache,
  //	"com.typesafe.play.modules" %% "play-modules-redis" % "2.6.0",

  // Database
  "org.postgresql" % "postgresql" % "42.1.3",

  // OAuth for Play
  "org.pac4j" %% "play-pac4j" % "10.0.1",
  "org.pac4j" % "pac4j-http" % "4.0.3",
  "org.pac4j" % "pac4j-oauth" % "4.0.3",
  "org.pac4j" % "pac4j-oidc" % "4.0.3" exclude("commons-io", "commons-io"),
  "org.pac4j" % "pac4j-sql" % "4.0.3",
  // https://mvnrepository.com/artifact/org.apache.shiro/shiro-core
  "org.apache.shiro" % "shiro-core" % "1.5.3",

  // Apache Commons IO
  "commons-io" % "commons-io" % "2.7",
  // https://mvnrepository.com/artifact/org.apache.commons/commons-text
  "org.apache.commons" % "commons-text" % "1.9",

  // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",

  // https://mvnrepository.com/artifact/io.getstream.client/stream-repo-apache
  "io.getstream.client" % "stream-repo-apache" % "1.3.0",

  // https://mvnrepository.com/artifact/org.jsoup/jsoup
  "org.jsoup" % "jsoup" % "1.10.3",

  "io.prometheus" % "simpleclient_common" % "0.8.1",
  "io.prometheus" % "simpleclient_hotspot" % "0.8.1",

  "org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

  // https://mvnrepository.com/artifact/io.swagger.core.v3/swagger-annotations
  "io.swagger.core.v3" % "swagger-annotations" % "2.1.4",

  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "it",
  "org.assertj" % "assertj-core" % "3.15.0" % "it,test",
  "org.mockito" % "mockito-core" % "2.8.47" % "it,test"
)

dependencyOverrides ++= Seq(
  // INFO: Necessary because: Scala module 2.10.3 requires Jackson Databind version >= 2.10.0 and < 2.11.0
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.5",
)

// re-create maven directory structure
unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "java"
unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "test" / "java"

// shares contents of src/test/java with src/it/java
dependencyClasspath in IntegrationTest := (dependencyClasspath in IntegrationTest).value ++ (exportedProducts in Test).value

//
// Docker
//
maintainer := "René Panzar <rene.panzar@gmail.com>"

dockerRepository := Some("resamsel")

dockerBaseImage := "java:8-jre"

dockerExposedPorts in Docker := Seq(9000)

dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/data")

//
// Tests
//
fork in Test := false
fork in IntegrationTest := false

//
// JaCoCo test coverage
//
//jacoco.settings
//
//// Unfortunately, this is really needed
//parallelExecution in jacoco.Config := false
//
//fork in jacoco.Config := false
//
//jacoco.excludes in jacoco.Config := Seq(
//	"router.*",
//	"views.html.*", // should probably not be excluded
//	"*.Reverse*",
//	"*.routes",
//	"*.scala"
//)

buildInfoOptions += BuildInfoOption.BuildTime
