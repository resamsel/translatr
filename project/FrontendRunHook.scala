import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

/**
  * Frontend build play run hook.
  * https://www.playframework.com/documentation/2.6.x/SBTCookbook
  */
object FrontendRunHook {
  def apply(base: File): PlayRunHook = {
    object UIBuildHook extends PlayRunHook {

      var processUi: Option[Process] = None
      var processAdmin: Option[Process] = None

      /**
        * Change these commands if you want to use Yarn.
        */
      var npmInstall: String = FrontendCommands.dependencyInstall
      var npmRunUi: String = FrontendCommands.serveUi
      var npmRunAdmin: String = FrontendCommands.serveAdmin

      // Windows requires npm commands prefixed with cmd /c
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        npmInstall = "cmd /c" + npmInstall
        npmRunUi = "cmd /c" + npmRunUi
        npmRunAdmin = "cmd /c" + npmRunAdmin
      }

      /**
        * Executed before play run start.
        * Run npm install if node modules are not installed.
        */
      override def beforeStarted(): Unit = {
        Process(npmInstall, base / "ui").!
      }

      /**
        * Executed after play run start.
        * Run npm start
        */
      override def afterStarted(): Unit = {
        processUi = Option(
          Process(npmRunUi, base / "ui").run
        )
        processAdmin = Option(
          Process(npmRunAdmin, base / "ui").run
        )
      }

      /**
        * Executed after play run stop.
        * Cleanup frontend execution processes.
        */
      override def afterStopped(): Unit = {
        processUi.foreach(_.destroy())
        processUi = None
        processAdmin.foreach(_.destroy())
        processAdmin = None
      }

    }

    UIBuildHook
  }
}
