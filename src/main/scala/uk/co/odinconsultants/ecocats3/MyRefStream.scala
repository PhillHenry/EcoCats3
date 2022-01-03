package uk.co.odinconsultants.ecocats3

import cats.effect.{Ref, IO}
import fs2.Stream

object MyRefStream {

  def stateStream: Stream[IO, Boolean] = {
    for {
      ref <- Stream.eval(Ref.of[IO, Boolean](true))
      x <- Stream.emits(Seq(1,2,3))
      changed <- Stream.eval(ref.updateAndGet(x => !x))
    } yield {
      println(s"$x: changed = $changed")
      changed
    }
  }

}
