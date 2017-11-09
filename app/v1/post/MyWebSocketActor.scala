package v1.post

import akka.actor._
import com.typesafe.config.{Config, ConfigFactory}
import play.api.libs.json.{JsValue, Json}

import scalaj.http.Http
import java.util

import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.util.control.Breaks._
import scala.collection.JavaConverters._
import org.apache.kafka.common.TopicPartition

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>

      val conf: Config = ConfigFactory.load()
      val bootstrapServers: String = conf.getString("props.bootstrapServers")
      import java.util.Properties

      val TOPIC="test"

      val  props = new Properties()
      props.put("bootstrap.servers", bootstrapServers)
      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      props.put("group.id", "cons")
      props.put("auto.offset.reset", "latest")
      val consumer = new KafkaConsumer[String, String](props)
      //consumer.subscribe(util.Collections.singletonList(TOPIC))
      val consumerTopicPartition = new TopicPartition(TOPIC, 0)
      consumer.assign(util.Collections.singleton(consumerTopicPartition))

      val  inputLagConsumerProps = new Properties()
      inputLagConsumerProps.put("bootstrap.servers", bootstrapServers)
      inputLagConsumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      inputLagConsumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      inputLagConsumerProps.put("group.id", "inputLagConsumer")
      inputLagConsumerProps.put("auto.offset.reset", "latest")
      val lagConsumer = new KafkaConsumer[String, String](inputLagConsumerProps)

      val lagConsumerTopicPartition = new TopicPartition("input", 0)
      lagConsumer.assign(util.Collections.singleton(lagConsumerTopicPartition))


      breakable{
        while(!msg.isEmpty){
          val records=consumer.poll(100)
          if (records.count() == 0){
            val infoToKeepWebsocketAlive: String = "{ \"time\":" + System.currentTimeMillis() + "," + "\"lag\":" + 0  + ", \"inputRowsPerSecond\":" + 0 +
              ", \"processedRowsPerSecond\":" + 0 + "}"
            out ! (infoToKeepWebsocketAlive.toString)
            break()
          }

          for (record<-records.asScala){

            lagConsumer.seekToEnd(util.Collections.singleton(lagConsumerTopicPartition))

            val latestOffset = lagConsumer.position(lagConsumerTopicPartition)

            val queryStat = record.value().toString
            val responseJson: JsValue = Json.parse(queryStat)
            val inputRowsPerSecond: Option[Double] = (responseJson \ "inputRowsPerSecond").asOpt[Double]
            val processedRowsPerSecond: Option[Double] = (responseJson \ "processedRowsPerSecond").asOpt[Double]
            val finalOffset: Option[Long] = (responseJson \ "sources" \ 0 \ "endOffset" \ "input" \ "0").asOpt[Long]
            val latestKnownOffset: Long = (responseJson \ "latestOffset" \ "latestOffset").as[Long]

            val irs: Double = inputRowsPerSecond match {
              case Some(inputRowsPerSecond) => inputRowsPerSecond
              case None => 0
            }

            val prs: Double = processedRowsPerSecond match {
              case Some(processedRowsPerSecond) => processedRowsPerSecond
              case None => 0
            }
            val fOffset: Long = finalOffset match {
              case Some(finalOffset) => finalOffset
              case None => 0
            }
            //	  val lagCase = latestOffset - fOffset
            val lagCase = latestKnownOffset - fOffset



            val lag  = {
              if(lagCase < 0 || fOffset == 0) 0 else lagCase
            }

            if(lag != 0){
              val monitoringInfo: String = "{ \"time\":" + System.currentTimeMillis() + "," + "\"lag\":" + lag  + ", \"inputRowsPerSecond\":" + math.round(irs) +
                ", \"processedRowsPerSecond\":" + math.round(prs) + "}"

              out ! (monitoringInfo.toString())
            }else{println("0Lag")}

            consumer.close()
            lagConsumer.close()
            //Thread.sleep(50)
          }

        }
      }//breakable
  }
}
