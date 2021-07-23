/*
A purely functional synchronization primitive which represents a single value which may not yet be available.

When created, a Deferred is empty. It can then be completed exactly once, and never be made empty again.
 */

/*
Expected behavior of get

get on an empty Deferred will block until the Deferred is completed
get on a completed Deferred will always immediately return its content
get is cancelable and on cancelation it will unsubscribe the registered listener,
    an operation that's possible for as long as the Deferred value isn't complete
 */

/*
Expected behavior of complete

complete(a) on an empty Deferred will set it to a, notify any and all readers currently blocked on a call to get and return true
complete(a) on a Deferred that has already been completed will not modify its content, and will result false
 */

import cats.effect.{IO, Deferred}
import cats.syntax.all._

def start(d: Deferred[IO, Int]): IO[Unit] = {
  val attemptCompletion: Int => IO[Unit] = n => d.complete(n).attempt.void

  List(
    IO.race(attemptCompletion(1), attemptCompletion(2)),
    d.get.flatMap { n => IO(println(show"Result: $n")) }
  ).parSequence.void
}

val program: IO[Unit] =
  for {
    d <- Deferred[IO, Int]
    _ <- start(d)
  } yield ()

/*
Whenever you are in a scenario when many processes can modify the same value but you only care about the first one in doing so and stop processing, then this is a great use case of Deferred[F, A].
 */