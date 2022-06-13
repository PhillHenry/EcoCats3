package uk.co.odinconsultants.ecocats3

import cats.data.State

object StateExample {
  type Env = Map[String, Int]

  type Event = Int | (String, Int)

  def add(event: Event) = State[Env, Int] { env =>
    event match
      case n: Int => (env, n)
      case (name, n) => (env + (name -> n), n)
  }

  def loop: State[Env, Int] =
    for
      _ <- add(2)
      _ <- add("x", 3)
      - <- add(3)
      _ <- add("y", 3)
      _ <- add("z", 4)
      b <- add("w", 5)
    yield (b)

  def main(args: Array[String]): Unit = print(loop.run(Map.empty).value)
}
