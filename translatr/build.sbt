import com.typesafe.sbt.packager.docker._

name := """translatr"""

version := "2.0.0-SNAPSHOT"

lazy val root = (project in file(".")).
	enablePlugins(PlayJava, PlayEbean, BuildInfoPlugin).
	settings(
		buildInfoKeys := Seq[BuildInfoKey](name, version)
	)

scalaVersion := "2.11.8"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
	javaJdbc,
	cache,

	// Database
	"org.postgresql" % "postgresql" % "9.4.1210",

	// OAuth for Play
	"com.feth" %% "play-authenticate" % "0.8.1-SNAPSHOT",
	"be.objectify" %% "deadbolt-java" % "2.5.0",

	// Apache Commons IO
	"commons-io" % "commons-io" % "2.5",

	// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
	"org.apache.httpcomponents" % "httpclient" % "4.5.2",

	"org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

	// https://mvnrepository.com/artifact/org.easytesting/fest-assert-core
	"org.easytesting" % "fest-assert-core" % "2.0M10"
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
maintainer := "René Samselnig <rene.samselnig@gmail.com>"

dockerRepository := Some("resamsel")

dockerBaseImage := "java:8-jre"

dockerExposedPorts in Docker := Seq(9000)

dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/data")

// Conflict Classes
conflictClassExcludes ++= Seq(
  "LICENSE",
  "reference.conf"
)

// Concat
Concat.groups := Seq(
	"styles.css" -> group(Seq(
		"stylesheets/materialize.min.css",
		"stylesheets/nprogress.css",
		"stylesheets/font-awesome.min.css",
		"stylesheets/d3.v3.css",
		"stylesheets/main.css",
		"stylesheets/media.css"
	)),
	"scripts.js" -> group(Seq(
		"javascripts/jquery.min.js",
		"javascripts/jquery.ba-bbq.min.js",
		"javascripts/materialize.min.js",
		"javascripts/jquery.autocomplete.min.js",
		"javascripts/d3.v3.min.js",
		"javascripts/moment.min.js",
		"javascripts/nprogress.js",
		"javascripts/app.js",
		"javascripts/main.js"
	))
)

// Put everything into the concat dir
Concat.parentDir := "concat"

// Allows concatenated resources to be used in dev mode
pipelineStages in Assets := Seq(concat)

pipelineStages := Seq(concat)
