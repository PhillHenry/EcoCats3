package uk.co.odinconsultants.ecocats3

import cats.effect.{Concurrent, IO, IOApp}
import cats.implicits.*
import fs2.Stream
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
    val call = stream.flatMap { streamChunks(Request, printChunk) }
    val output = call.compile.drain
    output
  }

  def streamChunks(request: Request[IO], chunking: ChunkFunc): Client[IO] => Stream[IO, Unit] =
    client =>
      val joke: Stream[IO, Response[IO]] = client.stream(request)
      val printed: Stream[IO, Unit] = joke.flatMap { response =>
        response.body.chunks.flatMap(chunking)
      }.handleError(t => t.printStackTrace())
      printed

  type ChunkFunc = Chunk[Byte] => Stream[IO, Unit]

  val printChunk: ChunkFunc = chunk => Stream.eval(IO(print(new String(chunk.toArray))))
}
