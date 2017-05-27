import com.typesafe.sbt.packager.docker._
import de.johoop.findbugs4sbt._
import de.johoop.findbugs4sbt.FindBugs._

name := """translatr"""

version := "2.1.0-SNAPSHOT"

lazy val root = (project in file(".")).
	enablePlugins(PlayJava, PlayEbean, BuildInfoPlugin).
	settings(
		buildInfoKeys := Seq[BuildInfoKey](name, version)
	)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
	javaJdbc,

	cache,
	"com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0",

	// Database
	"org.postgresql" % "postgresql" % "42.1.1",

	// OAuth for Play
	"com.feth" %% "play-authenticate" % "0.8.3",
	"be.objectify" %% "deadbolt-java" % "2.5.5",

	// Apache Commons IO
	"commons-io" % "commons-io" % "2.5",

	// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
	"org.apache.httpcomponents" % "httpclient" % "4.5.3",

	// https://mvnrepository.com/artifact/io.getstream.client/stream-repo-apache
	"io.getstream.client" % "stream-repo-apache" % "1.2.2",

	"org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

	"io.swagger" %% "swagger-play2" % "1.5.3",
	"org.webjars" % "swagger-ui" % "2.2.10",

	// https://mvnrepository.com/artifact/org.easytesting/fest-assert-core
	"org.easytesting" % "fest-assert-core" % "2.0M10" % "test",
	"org.mockito" % "mockito-core" % "2.7.22" % "test"
)

//
// Eclipse
//
// From: https://github.com/playframework/playframework/issues/3818
//
EclipseKeys.classpathTransformerFactories := EclipseKeys.classpathTransformerFactories.value.init

EclipseKeys.eclipseOutput := Some(".target")

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

// Java project. Don't expect Scala IDE
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes

EclipseKeys.withSource := true

//
// Docker
//
maintainer := "René Panzar <rene.panzar@gmail.com>"

dockerRepository := Some("resamsel")

dockerBaseImage := "java:8-jre"

dockerExposedPorts in Docker := Seq(9000)

dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/data")

//
// Concat
//
Concat.groups := Seq(
	"styles.css" -> group(Seq(
		"stylesheets/materialize.min.css",
		"stylesheets/nprogress.css",
		"stylesheets/font-awesome.min.css",
		"stylesheets/d3.v3.css",
		"stylesheets/codemirror.css",
		"stylesheets/codemirror.translatr.css",
		"stylesheets/main.css",
		"stylesheets/editor.css",
		"stylesheets/template.css",
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
		"javascripts/codemirror.js",
		"javascripts/codemirror.xml.js",
		"javascripts/underscore-min.js",
		"javascripts/backbone-min.js",
		"javascripts/backbone.undo.js",
		"javascripts/backbone-pageable.min.js",
		"javascripts/app.js",
		"javascripts/main.js",
		"javascripts/notification.js",
		"javascripts/editor.js"
	))
)

// Put everything into the concat dir
Concat.parentDir := "concat"

// Allows concatenated resources to be used in dev mode
pipelineStages in Assets := Seq(concat)

pipelineStages := Seq(concat)

//
// JaCoCo test coverage
//
jacoco.settings

jacoco.excludes in jacoco.Config := Seq(
	"router.*",
	"views.html.*", // should probably not be excluded
	"*.Reverse*",
	"*.routes"
)

// Unfortunately, this is really needed
parallelExecution in jacoco.Config := false

//
// FindBugs
//
findbugsSettings

findbugsReportType := Some(ReportType.Html)

findbugsReportPath := Some(crossTarget.value / "findbugs" / "report.html")

//
// Conflict classes
//
conflictClassExcludes ++= Seq(
  "LICENSE",
  "reference.conf"
)