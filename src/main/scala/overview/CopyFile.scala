package overview

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.effect.std.Semaphore

import java.io._

/**
 * Simple IO-based program to copy files. First part of cats-effect tutorial
 * at https://typelevel.org/cats-effect/tutorial/tutorial.html
 */
object CopyFile extends IOApp {

  def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
    for {
      amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
      count <- if (amount > -1) IO.blocking(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
      else IO.pure(acc) // End of read stream reached (by java.io.InputStream contract), nothing to write
    } yield count // Returns the actual amount of bytes transmitted

  def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
    for {
      buffer <- IO {
        new Array[Byte](1024 * 10)
      } // Allocated only when the IO is evaluated
      total <- transmit(origin, destination, buffer, 0L)
    } yield total

  def inputStream(f: File, guard: Semaphore[IO]): Resource[IO, FileInputStream] =
    Resource.make {
      IO(new FileInputStream(f))
    } { inStream =>
      guard.permit.use { _ =>
        IO(inStream.close()).handleErrorWith(_ => IO.unit)
      }
    }

  def outputStream(f: File, guard: Semaphore[IO]): Resource[IO, FileOutputStream] =
    Resource.make {
      IO(new FileOutputStream(f))
    } { outStream =>
      guard.permit.use{ _ =>
        IO(outStream.close()).handleErrorWith(_ => IO.unit)
      }
    }

  def inputOutputStreams(in: File, out: File, guard: Semaphore[IO]): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream <- inputStream(in, guard)
      outStream <- outputStream(out, guard)
    } yield (inStream, outStream)

  def copy(origin: File, destination: File): IO[Long] =
    for {
      guard <- Semaphore[IO](1)
      count <- inputOutputStreams(origin, destination, guard).use { case (in, out) =>
        guard.permit.use { _ =>
          transfer(in, out)
        }
      }
    } yield count

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
      else IO.unit
      orig = new File(args.head)
      dest = new File(args.tail.head)
      count <- copy(orig, dest)
      _ <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    } yield ExitCode.Success

}

/**
 * sbt
 * runMain {package}.{objectName} params
 */