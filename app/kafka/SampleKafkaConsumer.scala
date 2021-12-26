package kafka

import akka.Done
import akka.actor.CoordinatedShutdown
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.Logging

import java.time.Duration
import java.util.Properties
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.{IterableHasAsScala, SetHasAsJava}
import scala.util.{Failure, Success}

@Singleton
class SampleKafkaConsumer @Inject()(coordinatedShutdown: CoordinatedShutdown) extends Logging {

  logger.info("SampleKafkaConsumer starts")

  private val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  private val stopConsumer: AtomicBoolean = new AtomicBoolean(false)

  private val properties = new Properties()
  properties.put("bootstrap.servers", "localhost:6003")
  properties.put("group.id", s"sample-group-id")
  properties.put("key.deserializer", classOf[StringDeserializer])
  properties.put("value.deserializer", classOf[StringDeserializer])

  val kafkaConsumer = new KafkaConsumer[String, String](properties)
  kafkaConsumer.subscribe(Set("sample-topic").asJava)

  Future {
    while (!stopConsumer.get()) {
      kafkaConsumer.poll(Duration.ofSeconds(60)).asScala
        .foreach(r => {
          logger.info(s"SampleKafkaConsumer receives record: $r")
        })
    }
    logger.info(s"SampleKafkaConsumer quits 'while(true)' loop.")
  }(executionContext)
  .andThen(_ => kafkaConsumer.close())(executionContext)
  .andThen {
    case Success(_) =>
      kafkaConsumer.close()
    case Failure(e) =>
      kafkaConsumer.close()
  }(executionContext)

  coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceStop, "SampleKafkaConsumer-stop"){() =>
    logger.info("Shutdown-task[SampleKafkaConsumer-stop] starts.")
    stopConsumer.set(true)
    Future{ Done }(executionContext).andThen{
      case Success(_) => logger.info("Shutdown-task[SampleKafkaConsumer-stop] succeed.")
      case Failure(e) => logger.error("Shutdown-task[SampleKafkaConsumer-stop] fails.", e)
    }(executionContext)
  }
}
