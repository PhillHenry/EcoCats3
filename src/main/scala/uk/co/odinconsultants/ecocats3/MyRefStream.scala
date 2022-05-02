package uk.co.odinconsultants.ecocats3

import cats.effect.{Ref, IO}
import fs2.Stream

object MyRefStream {

  def stateStream: Stream[IO, Boolean] =
    for {
      ref:     Ref[IO, Boolean] <- Stream.eval(Ref.of[IO, Boolean](true)) // note `Ref.of` returns F[Ref[F, A]]. `eval` gets inside the burrito
      x:       Int              <- Stream.emits(Seq(1,2,3))
      changed: Boolean          <- Stream.eval(ref.updateAndGet(x => !x)) // note: the *result* of *eval*uating the Ref. Again, we get inside the `F`
    } yield {
      println(s"$x: changed = $changed")
      changed
    }

}
