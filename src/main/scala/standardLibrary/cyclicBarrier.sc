/*
A re-usable synchronization primitive that allows a set of fibers to wait until they've all reached the same point.

A cyclic barrier is initialized with a positive integer n
and fibers which call await are semantically blocked until n of them have invoked await,
at which point all of them are unblocked and the cyclic barrier is reset.
 */

import cats.implicits._
import cats.effect._
import cats.effect.std.CyclicBarrier
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

val run = (for {
  b <- CyclicBarrier[IO](2)
  f1 <- (IO.println("fast fiber before barrier") >>
    b.await >>
    IO.println("fast fiber after barrier")
    ) .start
  f2 <- (IO.sleep(1.second) >>
    IO.println("slow fiber before barrier") >>
    IO.sleep(1.second) >>
    b.await >>
    IO.println("slow fiber after barrier")
    ).start
  _ <- (f1.join, f2.join).tupled
} yield ())

run.unsafeRunSync()

/*
fast fiber before barrier
slow fiber before barrier
slow fiber after barrier
fast fiber after barrier
 */

// notice how they finish together :)