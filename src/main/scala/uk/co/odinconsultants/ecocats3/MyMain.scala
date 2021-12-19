package uk.co.odinconsultants.ecocats3

import cats.effect.{Concurrent, IO, IOApp}
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
    val call = stream.flatMap { streamChunks(Request, printChunk) }.handleError(t => t.printStackTrace())
    val output = call.compile.drain
    output
  }

  def streamChunks[T](request: Request[IO], chunking: ChunkFunc[T]): Client[IO] => Stream[IO, T] =
    client =>
      val jokeStream: Stream[IO, Response[IO]] = client.stream(request)
      chunkingPipe(chunking)(jokeStream)

  def chunkingPipe[T](chunking: ChunkFunc[T]): Pipe[IO, Response[IO], T] = _.flatMap { response =>
    response.body.chunks.flatMap(chunking)
  }

  type ChunkFunc[T] = Chunk[Byte] => Stream[IO, T]

  val printChunk: ChunkFunc[Unit] = chunk => Stream.eval(IO(print(new String(chunk.toArray))))
}
