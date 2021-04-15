// fibers

import cats.effect.kernel.Fiber
import cats.effect.{MonadCancel, Spawn, Temporal}
import cats.effect.syntax.all._
import cats.syntax.all._

trait Server[F[_]] {
  def accept: F[Connection[F]]
}

trait Connection[F[_]] {
  def read: F[Array[Byte]]
  def write(bytes: Array[Byte]): F[Unit]
  def close: F[Unit]
}

def endpoint[F[_]: Spawn]( // spawn semantic actions as-needed
                           server: Server[F])(
                           body: Array[Byte] => F[Array[Byte]])
: F[Unit] = {

  def handle(conn: Connection[F]): F[Unit] = {
    for {
      request <- conn.read
      response <- body(request)
      _ <- conn.write(response)
    } yield ()
  }

  // wrapped in uncancellable to avoid resource leaks
  val handler: F[Fiber[F, Throwable, Unit]] = MonadCancel[F] uncancelable { poll =>
    poll(server.accept) flatMap { conn =>
      handle(conn).guarantee(conn.close).start // "a functional effect is something that *will* run => start
    }
  }
  // once we have our fiber we can join, cancel etc :)

  handler.foreverM // we're going to keep doing it indefinitely (like an infinite loop of >> calls)
}