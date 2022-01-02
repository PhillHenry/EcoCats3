package uk.co.odinconsultants.ecocats3

import cats.effect.{Concurrent, IO, IOApp, Ref}
import cats.implicits.*
import fs2.Stream
import fs2.Pipe
import fs2.Chunk
import io.circe.{Decoder, Encoder}
import org.http4s.*
import org.http4s.Method.*
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits.*
import uk.co.odinconsultants.ecocats3.Jokes.Joke
import org.http4s.client.Client

object MyMain extends IOApp.Simple {
  val dsl = new Http4sClientDsl[IO]{}
  import dsl.*

  val Request: Request[IO] = GET(uri"https://icanhazdadjoke.com/")

  def run: IO[Unit] = {
    val stream: Stream[IO, Client[IO]] = Stream.resource(EmberClientBuilder.default[IO].build)
    val responseStream: Stream[IO, Response[IO]] = stream.flatMap( _.stream(Request) )
    makeCall(responseStream, printChunk)
  }

  def makeCall[T](responseStream: Stream[IO, Response[IO]], chunking: ChunkFunc[T]): IO[Unit] = {
    val pipe: Pipe[IO, Response[IO], T] = chunkingPipe(chunking)
    val call: Stream[IO, T] = pipe(responseStream)
    call.handleError(t => t.printStackTrace()).compile.drain
  }

  type StreamType = Byte

  def parsing(stream: Stream[IO, Chunk[StreamType]]): Stream[IO, StreamType] = {
    val seed = false
    val s = stream.unchunks.evalMapAccumulate(seed){ case (b: Boolean, c: StreamType) =>
      val newB = if (c == '<') false else if (c == '>') true else b
      println(s"c = ${new String(Array(c))} ($c), newB = $newB")
      IO((newB, if (b) c else -1))
    }
    s.flatMap { case (b, x) =>
      println(s"b = $b")
      if (b && x != -1) Stream.eval(IO(x)) else Stream.empty
    }
  }

  def chunkingPipe[T](chunking: ChunkFunc[T]): Pipe[IO, Response[IO], T] = _.flatMap { response =>
    response.body.chunks.flatMap(chunking)
  }

  type ChunkFunc[T] = Chunk[StreamType] => Stream[IO, T]

  val printChunk: ChunkFunc[Unit] = chunk => Stream.eval(IO(print(new String(chunk.toArray))))
}
