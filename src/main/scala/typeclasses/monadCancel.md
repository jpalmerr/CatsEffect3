# Monad Cancel

```scala
sealed trait Outcome[F[_], E, A]
final case class Succeeded[F[_], E, A](fa: F[A]) extends Outcome[F, E, A]
final case class Errored[F[_], E, A](e: E) extends Outcome[F, E, A]
final case class Canceled[F[_], E, A]() extends Outcome[F, E, A]
```

We have to worry about cancellation as well as exceptions...

The `MonadCancel` typeclass addresses this by extending `MonadError`

```scala worksheet
import cats.effect._
import cats.effect.syntax.all._

openFile.bracket(fis => readFromFile(fis))(fis => closeFile(fis))
```

^ if `openFile` runs, then `closeFile` will be run, no matter what

## Self-Cancelation

```scala
MonadCancel[F].canceled >> fa
```

fa will never be run, result in a canceled evaluation

