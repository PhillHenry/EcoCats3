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
      val request: Request[IO] = GET(uri"https://icanhazdadjoke.com/")
      val joke: Stream[IO, Response[IO]] = client.stream(request)
      val printed = joke.flatMap { response =>
        response.body.flatMap { b =>
          Stream.eval(IO(print(b)))
        }
      }.handleError(t => t.printStackTrace())
      printed
    }
    val output = call.compile.drain
    output
  }
}
