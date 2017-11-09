/**
  * Created by sai on 16/10/17.
  */
package v1.post

import sys.process._
import com.typesafe.config.{Config, ConfigFactory}
import play.api.mvc.Action

object SpawnProcess {

  def runCommand(command: String) = {

    val conf: Config = ConfigFactory.load()
    val scriptLocation = conf.getString("props.scaleConsumers.location")

    val pb: ProcessBuilder = Process(scriptLocation.toString +" " +command.toString )
    val p: Process = pb.run()
  }

  def runBashFile(parameter: String): Unit ={
    val conf: Config = ConfigFactory.load()
    val scriptLocation = conf.getString("props.increaseExecutors.location")

    val pb: ProcessBuilder = Process(scriptLocation.toString +" " +parameter.toString)
    val p: Process = pb.run()
  }

}