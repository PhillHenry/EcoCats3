package uk.co.odinconsultants.ecocats3

import cats.effect.{Ref, IO}
import fs2.Stream

object MyRefStream {

  def stateStream: Stream[IO, Boolean] = {
    for {
      io <- Stream.emit(Ref.of[IO, Boolean](true))
      values <- Stream.emits(Seq(1,2,3))
      changed <- Stream.eval(io.flatMap(ref => ref.updateAndGet(_ => values % 2 == 0)))
    } yield {
      println(s"changed = $changed")
      changed
    }
  }

}
