import cats.effect.IO
import cats.effect.unsafe.implicits.global

val run = for {
  fib <- (IO.uncancelable(_ =>
    IO.canceled >> IO.println("This will print as cancelation is suppressed")
  ) >> IO.println(
    "This will never be called as we are canceled as soon as the uncancelable block finishes")).start
  res <- fib.join
} yield res

run.unsafeRunSync()