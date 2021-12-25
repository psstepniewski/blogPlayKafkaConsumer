package kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.Logging

import java.time.Duration
import java.util.Properties
import java.util.concurrent.Executors
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.{IterableHasAsScala, SetHasAsJava}
import scala.util.{Failure, Success}

@Singleton
class SampleKafkaConsumer extends Logging {

  logger.info("Starting SampleKafkaConsumer")

  val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  private val properties = new Properties()
  properties.put("bootstrap.servers", "localhost:6003")
  properties.put("group.id", s"sample-group-id")
  properties.put("key.deserializer", classOf[StringDeserializer])
  properties.put("value.deserializer", classOf[StringDeserializer])

  val kafkaConsumer = new KafkaConsumer[String, String](properties)
  kafkaConsumer.subscribe(Set("sample-topic").asJava)

  Future {
    while (true) {
      kafkaConsumer.poll(Duration.ofSeconds(3)).asScala
        .foreach(r => {
          logger.info(s"SampleKafkaConsumer receive record $r")
        })
    }
  }(executionContext).andThen {
    case Success(_) => logger.info(s"SampleKafkaConsumer succeed.")
    case Failure(e) => logger.error(s"SampleKafkaConsumer fail.", e)
  }(executionContext)
}
