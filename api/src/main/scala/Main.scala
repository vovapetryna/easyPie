import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import java.io.{File, FileInputStream, FileOutputStream}

object Main extends IOApp.Simple {
  def inputStreem(f: File): Resource[IO, FileInputStream] = Resource.make(IO(new FileInputStream(f)))(i => IO(i.close).handleErrorWith(_ => IO.unit))
  def outputStreem(f: File): Resource[IO, FileOutputStream] = Resource.make(IO(new FileOutputStream(f)))(o => IO(o.close).handleErrorWith(_ => IO.unit))
  def ioStream(fIn: File, fOut: File) = for {
    i <- inputStreem(fIn)
    o <- outputStreem(fOut)
  } yield (i, o)

  def transmit(iS: FileInputStream, oS: FileOutputStream, buffer: Array[Byte], acc: Long): IO[Long] = for {
    amount <- IO(iS.read(buffer, 0, buffer.size))
    count <- if (amount > -1) IO(oS.write(buffer, 0, amount)) >> transmit(iS, oS, buffer, acc + amount) else IO.pure(acc)
  } yield count

  def transfer(iS: FileInputStream, oS: FileOutputStream): IO[Long] = for {
    buffer <- IO(new Array[Byte](1024 * 10))
    total <- transmit(iS, oS, buffer, 0)
  } yield total

  def copy(origin: File, destination: File) = ioStream(origin, destination).use ((i, o) => transfer(i, o))
}
