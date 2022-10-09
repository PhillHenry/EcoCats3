package uk.co.odinconsultants.ecocats3.fs2fun

import scala.concurrent.duration.*
import cats.effect.IO
import cats.implicits.*
import fs2.{Chunk, Pipe, Pure, Stream}
import fs2.kafka.{AutoOffsetReset, CommittableOffset, CommittableProducerRecords, ConsumerSettings, KafkaConsumer}
import munit.CatsEffectSuite
import uk.co.odinconsultants.dreadnought.docker.KafkaAntics
import uk.co.odinconsultants.dreadnought.docker.KafkaAntics.{consume, produceMessages}
import com.comcast.ip4s.*

/**
  * Attempt to modernize the code in:
  * https://github.com/fd4s/fs2-kafka/issues/127
  */
final class RebalanceSpec extends CatsEffectSuite {
  type Consumer       = KafkaConsumer[IO, String, String]
  type ConsumerStream = Stream[IO, CommittableProducerRecords[IO, String, String]]

  test("rebalance process should consume only once") {
    val topic = "topic"
    KafkaAntics.createCustomTopic(topic, partitions = 6)
    val address = ip"127.0.0.1"
    val port = port"9092"
    Stream
      .unfoldEval[IO, Int, Int](0)(i => IO.pure(Some(i -> (i + 1))))
      .take(1024 * 16)
      .chunkN(32)
      .zipLeft(Stream.repeatEval(IO.sleep(50.millis)))
      .map(xs => {

          produceMessages(address, port, topic)
      })
      .compile
      .drain
      .map(x => println(s"producer completed $x"))
      .unsafeRunAndForget()

    val commit: Pipe[IO, Chunk[CommittableOffset[IO]], Unit] =
      _.flatMap(chunk => Stream.eval(IO(chunk.map(_.commit))))

    val bootstrapServer  = s"${address}:${port.value}"
    val consumerSettings =
      ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers(bootstrapServer)
        .withGroupId("group_PH")

    val cstream: Stream[IO, Int] = consume(consumerSettings, topic)
      .groupWithin(1000, 5.second)
      .flatMap(c =>{
          val offsetStream: Stream[Pure, Chunk[CommittableOffset[IO]]] = Stream(c.map(_.offset))
          commit(offsetStream)
            .map(_ => println(s"committed chunk of ${c.size}"))
            .flatMap(_ => Stream.chunk(c))
        }
      )
      .map(_.record.value.toInt)
      .interruptAfter(120.seconds)

    val s1: Stream[IO, Int] = cstream
    // will trigger rebalance
    val s2: Stream[IO, Int] = cstream.delayBy(7.seconds)

    val (c1, c2) = (s1.compile.toVector, s2.compile.toVector).parTupled.unsafeRunSync()

    assert(c1.intersect(c2).isEmpty)
  }
}
