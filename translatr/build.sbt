import com.typesafe.sbt.packager.docker._

name := """play-translatr"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
	javaJdbc,
	cache,

	// Database
	"org.postgresql" % "postgresql" % "9.4.1210",

	// Apache Commons IO
	"commons-io" % "commons-io" % "2.5",

	"org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final"
)

// From: https://github.com/playframework/playframework/issues/3818
EclipseKeys.classpathTransformerFactories := EclipseKeys.classpathTransformerFactories.value.init

EclipseKeys.eclipseOutput := Some(".target")

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

// Java project. Don't expect Scala IDE
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes

EclipseKeys.withSource := true

// Docker
maintainer := "René Samselnig"

dockerRepository := Some("resamsel")

dockerBaseImage := "java:8-jre"

dockerExposedPorts in Docker := Seq(9000)

dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/data")

// Conflict Classes
conflictClassExcludes ++= Seq(
  "LICENSE",
  "reference.conf"
)