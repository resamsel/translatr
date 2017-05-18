// The scoverage plugin only works for Scala, so this is a dead end

//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

// There is an issue with the latest version of sbt-coveralls and Play projects created using the default templates on 2.4.x. In order to work around this, we suggest you use the following versions of sbt-coverage/sbt-coveralls in your plugins.sbt
// -- https://github.com/scoverage/sbt-coveralls#play-framework-integration
//resolvers += Classpaths.sbtPluginReleases
//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.1")
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")