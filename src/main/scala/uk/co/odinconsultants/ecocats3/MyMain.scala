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

  def htmlFlag(isText: IO[Ref[IO, Boolean]], c: StreamType): IO[Boolean] = {
    isText.flatMap { ref =>
      val flag = if (c == '<') {
        println(s"Found '<' ref = $ref")
        ref.updateAndGet(_ => false)
      } else if (c == '>') {
        println(s"Found '>' ref = ${ref.get}")
        ref.updateAndGet(_ => true)
      } else {
        println(s"Found $c")
        ref.updateAndGet(identity)
      }
      flag
    }
  }

  type StreamType = Byte

  def parsing(stream: Stream[IO, Chunk[StreamType]]) = {
    for {
      isText <- Stream.emit(Ref.of[IO, Boolean](true))
      chunk <- stream
      c <- Stream.chunk(chunk)
      isHtml <- Stream.eval(htmlFlag(isText, c))
    } yield {
      println(s"isHtml = $isHtml")
      if (isHtml) "" else c
    }
  }

  def chunkingPipe[T](chunking: ChunkFunc[T]): Pipe[IO, Response[IO], T] = _.flatMap { response =>
    response.body.chunks.flatMap(chunking)
  }

  type ChunkFunc[T] = Chunk[StreamType] => Stream[IO, T]

  val printChunk: ChunkFunc[Unit] = chunk => Stream.eval(IO(print(new String(chunk.toArray))))
}
