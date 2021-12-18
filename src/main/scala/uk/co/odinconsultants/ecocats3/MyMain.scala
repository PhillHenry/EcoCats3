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
    val call = stream.flatMap { (client: Client[IO]) =>
      println(s"client = $client")
      val joke: IO[Joke] = client.expect[Joke](GET(uri"https://icanhazdadjoke.com/")).handleError(t => Joke(t.getMessage))
      val printed: IO[Unit] = joke.flatMap { msg =>
        println(s"msg = $msg")
        IO.println(msg)
      }.handleError(t => t.printStackTrace())
      Stream.eval(printed)
    }
    val output: IO[Unit] = call.compile.drain
    output
  }
}
