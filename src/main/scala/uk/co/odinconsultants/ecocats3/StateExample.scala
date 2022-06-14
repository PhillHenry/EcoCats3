package uk.co.odinconsultants.ecocats3

import cats.data.State

/**
  * thanh — 06/11/2022
Yes, of course, I'm working on a Repl for an lambda calculus interpreter. Just using readLine and println at the moment (so without cats-effect IO).
It works fine until I want to add an Env which is just a Map[String, Expr] which I want to use StateMonad to store that data
I did make an example with State like this
  * SystemFw — 06/11/2022
yeah, embedding side-effects directly into State is going to be pretty confusing
  I mean, for real code I'd recommend IO and Ref without State, yes
ofc, there are various things you can explore that might be instructive
because as far as I can see there is a fundamental misunderstanding in that example
which is that the whole loop needs to be in State if you want it to be stateful across iterations
  in particular you need to find a way for println and readLine to return State
you can do that by embedding the side effects in there (frowned upon, but it's just for learning)
or by converting the whole thing to StateT[IO (works, but it's kinda complicated)
or by ditching State in favour of Ref, and sticking to IO
  */
object StateExample {
  type Env = Map[String, Int]

  type Event = Int | (String, Int)

  def add(event: Event): State[Env, Int] = State[Env, Int] { env =>
    println(s"add($event), env = $env")
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
    yield b

  def loopBrief: State[Env, Int] =
    for
      _ <- add("x", 3)
      _ <- add("y", 3)
      _ <- add("z", 4)
      b <- add("w", 5)
    yield b

  def loopBriefDesugared: State[Env, Int] =
    add("x", 3).flatMap(
      _ => add("y", 3).flatMap(
        _ => add("z", 4).flatMap(
          _ => add("w", 5))))

  def main(args: Array[String]): Unit = {
    val looped = loop.run(Map.empty)    // this runs nothing!
    println("------")
    println(s"value = ${looped.value}") // note that the looped.value is what calls the lazy adds!
    println("------")
    val loopedBrief = loopBrief.run(Map.empty)
    println(s"value = ${loopedBrief.value}")
    println(loop.run(Map.empty) == loopedBrief)
    println("------")
    println(s"desugared value = ${loopBriefDesugared.run(Map.empty).value}")
  }
}
