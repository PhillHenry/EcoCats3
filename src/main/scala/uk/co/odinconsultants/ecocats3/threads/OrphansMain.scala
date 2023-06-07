package uk.co.odinconsultants.ecocats3.threads
import cats.effect.IO
import cats.effect.IOApp.Simple
import cats.effect.kernel.Resource
import scala.concurrent.duration._

object OrphansMain extends Simple {
  override def run: IO[Unit] = {
    val background: Resource[IO, Unit] = for {
      x <- IO.println("heartbeat").background
    } yield {
      println("ended")
    }
//    val x = (IO.println("heartbeat") *> IO.sleep(1 second)).foreverM
    background.use(_ => IO.println("finished"))
  }
}
