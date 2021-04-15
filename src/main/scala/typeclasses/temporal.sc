import cats.effect.{IO, Sync, Temporal}
import cats.implicits._

import scala.concurrent.duration.DurationInt

// Temporal extends Concurrent with the ability to suspend a fiber by sleeping for a specified duration

Temporal[IO].sleep(5.seconds)

val firstThing: IO[String] = IO("hello")
val secondThing: IO[Int] = IO(10)

firstThing >> Temporal[IO].sleep(5.seconds) >> secondThing

// better than
Sync[IO].delay(Thread.sleep(50))
// as it will block a thread pool (dont want to lose cpus)