import play.sbt.PlayImport.javaJdbc

name := """translatr"""

version := "3.0.3"

lazy val root = (project in file("."))
	.configs(IntegrationTest)
	.enablePlugins(PlayJava, PlayEbean, BuildInfoPlugin)
	.settings(
		Defaults.itSettings,
		buildInfoKeys := Seq[BuildInfoKey](name, version)
	)

scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
	javaJdbc,

	guice,

	"com.typesafe.play" %% "play-json" % "2.6.14",
	"com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.11.2",

	ehcache,
//	"com.typesafe.play.modules" %% "play-modules-redis" % "2.6.0",

	// Database
	"org.postgresql" % "postgresql" % "42.1.3",

	// OAuth for Play
	"org.pac4j" %% "play-pac4j" % "10.0.1",
	"org.pac4j" % "pac4j-oauth" % "4.0.3",
	"org.pac4j" % "pac4j-oidc" % "4.0.3" exclude("commons-io" , "commons-io"),
	"org.pac4j" % "pac4j-sql" % "4.0.3",
	"be.objectify" %% "deadbolt-java" % "2.7.1",
	// https://mvnrepository.com/artifact/org.apache.shiro/shiro-core
	"org.apache.shiro" % "shiro-core" % "1.5.3",

	// Apache Commons IO
	"commons-io" % "commons-io" % "2.7",

	// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
	"org.apache.httpcomponents" % "httpclient" % "4.5.3",

	// https://mvnrepository.com/artifact/io.getstream.client/stream-repo-apache
	"io.getstream.client" % "stream-repo-apache" % "1.3.0",

	// https://mvnrepository.com/artifact/org.jsoup/jsoup
	"org.jsoup" % "jsoup" % "1.10.3",

	"io.prometheus" % "simpleclient_common" % "0.8.1",
	"io.prometheus" % "simpleclient_hotspot" % "0.8.1",

	"org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

	// https://mvnrepository.com/artifact/io.swagger/swagger-play2
	"io.swagger" %% "swagger-play2" % "1.7.1",
//	"org.webjars" % "swagger-ui" % "2.2.10",

	"com.typesafe.play" %% "play-test" % play.core.PlayVersion.current % "it",
	"org.assertj" % "assertj-core" % "3.15.0" % "it,test",
	"org.mockito" % "mockito-core" % "2.8.47" % "it,test"
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
