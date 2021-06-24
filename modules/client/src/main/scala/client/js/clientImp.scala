package client.js

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import client.{Client, RefQueue, socket}

import scala.scalajs.js.{Promise, annotation => jsA}

@jsA.JSExportTopLevel("client")
object clientImp {
  //created client. Uses RefQueue and calBack for external js state updating
  @jsA.JSExport("create")
  def client(onUpdate: scalajs.js.Function1[shared.ot.State, Unit]): Promise[Client[IO]] =
    Client.create[IO](onUpdate).unsafeToPromise()

  //starts socket connection
  @jsA.JSExport("start")
  def start(host: String, client: Client[IO]): Unit =
    socket
      .registerSocket(host, client.inputPipe, client.outputStream)
      .unsafeRunAsync(log => println("connection_log", log))

  //dispatch new input text value
  @jsA.JSExport("nextText")
  def nextText(newText: String, sid: Int, client: Client[IO]): Unit =
    client.nextText(newText, sid).unsafeRunAsync(log => println("next_text_log", log))

  //closes connection
  @jsA.JSExport("close")
  def close(client: Client[IO]): Unit =
    client.close.unsafeRunAsync(log => println("close_log", log))
}
