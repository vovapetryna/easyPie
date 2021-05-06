import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent._

import java.io.{File, FileInputStream, FileOutputStream}
import scala.concurrent.ExecutionContext

// --- Resourse monad (cares about asquire and release)
def inputStream[F[_]: Sync](f: File, semaphore: Semaphore[F]): Resource[F, FileInputStream] = Resource.make {
  Sync[F].delay(new FileInputStream(f))
} { is =>
  semaphore.withPermit {
    Sync[F].delay(is.close).handleErrorWith { ex => println(ex.getMessage); Sync[F].unit }
  }
}

def outputStream[F[_]: Sync](f: File, semaphore: Semaphore[F]): Resource[F, FileOutputStream] = Resource.make {
  Sync[F].delay(new FileOutputStream(f))
} { os =>
  semaphore.withPermit {
    Sync[F].delay(os.close).handleErrorWith { ex => println(ex.getMessage); Sync[F].unit }
  }
}

def ioStreams[F[_]: Sync](origin: File, destination: File, semaphore: Semaphore[F]): Resource[F, (FileInputStream, FileOutputStream)] = for {
  is <- inputStream(origin, semaphore)
  os <- outputStream(destination, semaphore)
} yield (is, os)

// --- stack safe IO, >> _.flatMap(_ => f)
def transmit[F[_]: Sync](origin: FileInputStream, destination: FileOutputStream, buffer: Array[Byte], acc: Long): F[Long] =
  for {
    amount <- Sync[F].delay(origin.read(buffer, 0, buffer.size))
    count <-
      if (amount > -1) Sync[F].delay(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
      else Sync[F].pure(acc)
  } yield count

// --- defered array allocation
def transfer[F[_]: Sync](origin: FileInputStream, destination: FileOutputStream): F[Long] = for {
  buffer <- Sync[F].delay(new Array[Byte](1024 * 10))
  result <- transmit[F](origin, destination, buffer, 0)
} yield result

// --- use Resource
def copy[F[_]: Async](origin: File, destination: File)(implicit concurrent: Concurrent[F]): F[Long] =
  for {
    semaphore <- Semaphore[F](1)
    result    <- ioStreams(origin, destination, semaphore).use((i, o) => semaphore.withPermit(transfer(i, o)))
  } yield result

// --- use test
val root         = "D:/vovap/Documents/Sound recordings"
val originF      = s"$root/Recording.m4a"
val destinationF = s"$root/copy.m4a"

{
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val copyTask = for {
    origin      <- IO(new File(originF))
    destination <- IO(new File(destinationF))
    result      <- copy[IO](origin, destination)
  } yield result

  // copyTask.unsafeRunSync()
}
