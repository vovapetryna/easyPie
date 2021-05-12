import cats._
import cats.implicits._
import cats.effect._
import fs2._
import fs2.io.file
import java.io.File

Stream.empty
Stream.emit(1)
Stream(1, 2, 3)
Stream.emit(List(1, 2, 3))

val sI = Stream.emits(List.range(1, 20))
sI.map(_ + 2).toList

// Efectfull streams
val effS = Stream.eval(IO { print("calc sum 1 + 1"); 1 + 1 })
effS.compile.toList.unsafeRunSync()

//Steam with Chunk
val cS = Stream.chunk(Chunk.array(Array(1, 2, 3, 4)))

//Stram based file reading
val testInputFile                 = new File("D:/vovap/Documents/Kafka/docker-compose.yaml")
implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
val program = Blocker[IO]
  .use { b =>
    file
      .readAll[IO](testInputFile.toPath, b, 2048)
      .through(text.utf8Decode)
      .through(text.lines)
      .evalMap(l => IO(println(l)))
      .compile
      .drain >> IO(println("done"))
  }

program.unsafeRunSync()
