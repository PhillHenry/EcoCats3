package uk.co.odinconsultants.ecocats3

import cats.effect.{Concurrent, IO, IOApp}
import cats.implicits.*
import fs2.Stream
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
  def run: IO[Unit] = {
    val stream: Stream[IO, Client[IO]] = Stream.resource(EmberClientBuilder.default[IO].build)
    val call: Stream[IO, IO[Unit]] = stream.flatMap { (client: Client[IO]) =>
      val joke: IO[Joke] = client.expect[Joke](GET(uri"https://icanhazdadjoke.com/"))
      val printed = joke.flatMap(msg => IO.println(msg))
      Stream.emit(printed)
    }
    val output: IO[Unit] = call.compile.drain
    output
  }
}
