/**
  * Frontend build commands.
  * Change these if you are using some other package manager. i.e: Yarn
  */
object FrontendCommands {
  val dependencyInstall: String = "npm install"
  val test: String = "npm run test"
  val serveUi: String = "npm run start"
  val serveAdmin: String = "npm run start:admin"
  val build: String = "npm run build:prod"
}
