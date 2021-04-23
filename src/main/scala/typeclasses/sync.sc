import cats.effect.{IO, Sync}

import java.util.concurrent.atomic.AtomicLong
import scala.io.Source

/*
Sync is the synchronous FFI for suspending side-effectful operations
The means of suspension is dependent on whether the side effect you want to suspend is blocking or not
*/

// If your side effect is not thread-blocking then you can use Sync[F].delay

val counter = new AtomicLong()

val inc: IO[Long] = Sync[IO].delay(counter.incrementAndGet())

// If your side effect is thread blocking then you should use Sync[F].blocking

val contents: IO[String] = Sync[IO].blocking(Source.fromFile("file").mkString)

/*
A downside of thread-blocking calls is that the fiber executing them is not cancelable until the blocking call completes.

For long operations use

Sync[F].interruptible
 */
