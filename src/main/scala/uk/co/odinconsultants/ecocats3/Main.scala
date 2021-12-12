package uk.co.odinconsultants.ecocats3

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    Ecocats3Server.stream[IO].compile.drain.as(ExitCode.Success)

