package controllers

import javax.inject.Inject

import com.typesafe.config.{Config, ConfigFactory}
import play.api.libs.json.{JsDefined, JsValue, Json}
import play.api.mvc._
import spray.json.JsArray
import v1.post.SpawnProcess

import scalaj.http._
import scalaj.http.HttpResponse
import play.api.http.HeaderNames
//import play.libs.Json

class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def burrowPost() = Action { implicit request =>
    val conf: Config = ConfigFactory.load()
    val burrowInfo: Option[JsValue] = request.body.asJson
    val status = burrowInfo match {
      case Some(burrowInfo) => (burrowInfo \ "events" \ 0 \ "event" \ "partitions" \ 0 \ "status").get
      case None => println("None Arrived")
    }

    if(status == "STOP" || status == "WARN") {
      //TODO: Alert UI instead of Spawning
      Ok(status.toString)
      //SpawnProcess.runCommand(conf.getString("props.scaleConsumers.command"))
    }
    Ok(status.toString)
  }

  def spawnFromUI() = Action { implicit request =>
    val conf: Config = ConfigFactory.load()

    SpawnProcess.runCommand(conf.getString("props.scaleConsumers.command"))

    val origin = request.headers.get(ORIGIN).getOrElse("*")
    val burrowHost = conf.getString("props.burrow.host")
    val burrowPort = conf.getString("props.burrow.port")
    val apiVersion = conf.getString("props.burrow.version") + "/" +conf.getString("props.burrow.api")
    val cluster = conf.getString("props.burrow.cluster")
    val consumerInfo: String = Http("http://" +
      burrowHost + ":" +
      burrowPort + "/" +
      apiVersion + "/" +
      cluster + "/" + "consumer").asString.body

    val responseJson: JsValue = Json.parse(consumerInfo)
    val consumerList: Array[String] = (responseJson \ "consumers").as[Array[String]]

    Ok(consumerList.length.toString).withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
      ACCESS_CONTROL_ALLOW_METHODS -> ("OPTIONS").mkString(", "),
      ACCESS_CONTROL_MAX_AGE -> "3600",
      ACCESS_CONTROL_ALLOW_HEADERS ->  s"$ORIGIN, X-Requested-With, $CONTENT_TYPE, $ACCEPT, $AUTHORIZATION, X-Auth-Token",
      ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true")
  }

  def increaseExecutors() = Action { implicit request =>
    val conf: Config = ConfigFactory.load()

    val origin = request.headers.get(ORIGIN).getOrElse("*")

    val executorInfo = request.body.asFormUrlEncoded

    val executorSet: Set[String] = executorInfo match {
      case Some(throttleInfo) => throttleInfo.keySet
      case None => Set("None")
    }
    val executors: String = executorSet.toSeq(0)
    val executorsJson = Json.parse(executors)
    val increase = (executorsJson \ "executors").as[String]
    println(increase)
    SpawnProcess.runBashFile(increase)

    Ok("Increased Executors").withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
      ACCESS_CONTROL_ALLOW_METHODS -> ("OPTIONS").mkString(", "),
      ACCESS_CONTROL_MAX_AGE -> "3600",
      ACCESS_CONTROL_ALLOW_HEADERS ->  s"$ORIGIN, X-Requested-With, $CONTENT_TYPE, $ACCEPT, $AUTHORIZATION, X-Auth-Token",
      ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true")
  }

  def increaseThrottle() = Action { implicit request =>
    val conf: Config = ConfigFactory.load()
    val bootstrapServers: String = conf.getString("props.bootstrapServers")
    val origin = request.headers.get(ORIGIN).getOrElse("*")

    val throttleInfo = request.body.asFormUrlEncoded

    val throttleSet: Set[String] = throttleInfo match {
      case Some(throttleInfo) => throttleInfo.keySet
      case None => Set("None")
    }
    val throttle: String = throttleSet.toSeq(0)
    val throttleJson = Json.parse(throttle)
    val increase = (throttleJson \ "throttle").as[String]

    /*val command: String = "/home/kusumakar/work/kafka/bin/kafka-producer-perf-test.sh --num-records 10000000 --topic input --throughput " +
      increase +
      " --payload-file /home/kusumakar/work/kafka/headtxn.csv --producer-props acks=1 bootstrap.servers=" + bootstrapServers*/

    val command: String = "/usr/local/kafka/lastest/bin/kafka-producer-perf-test.sh --num-records 10000000 --topic input --throughput " +
      increase +
      " --payload-file /home/ubuntu/theDemo/headtxn.csv --producer-props acks=1 bootstrap.servers=" + bootstrapServers

    SpawnProcess.runCommand(command)
    Ok("Done").withHeaders(
      ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
      ACCESS_CONTROL_ALLOW_METHODS -> ("OPTIONS").mkString(", "),
      ACCESS_CONTROL_MAX_AGE -> "3600",
      ACCESS_CONTROL_ALLOW_HEADERS ->  s"$ORIGIN, X-Requested-With, $CONTENT_TYPE, $ACCEPT, $AUTHORIZATION, X-Auth-Token",
      ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true")
  }
}

