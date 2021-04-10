import cats.effect.{IO, Resource}
import java.io._

/**
 * We consider opening a stream to be a side-effect action,
 * so we have to encapsulate those actions in their own IO instances.
 *
 * For this, we will make use of cats-effect Resource, that allows to orderly create, use and then release resources.
 */

def inputStream(f: File): Resource[IO, FileInputStream] =
  Resource.make {
    IO(new FileInputStream(f))                         // build
  } { inStream =>
    IO(inStream.close()).handleErrorWith(_ => IO.unit) // release
  }

def outputStream(f: File): Resource[IO, FileOutputStream] =
  Resource.make {
    IO(new FileOutputStream(f))                         // build
  } { outStream =>
    IO(outStream.close()).handleErrorWith(_ => IO.unit) // release
  }

def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
  for {
    inStream  <- inputStream(in)
    outStream <- outputStream(out)
  } yield (inStream, outStream)

/*
Usually in handleErrorWith we at least log, if not map to our own error type
Often you will see that .attempt.void is used to get the same 'swallow and ignore errors' behavior
 */
