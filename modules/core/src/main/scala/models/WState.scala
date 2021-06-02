package models

import cats.effect.Async
import cats.effect.std.Queue
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic

case class WState[F[_]: Async](topics: Map[Long, Topic[F, OutputMessages]], queues: Map[Long, Queue[F, InputMessages]]) {
  def subscribe(id: Long): Option[Stream[F, OutputMessages]] = topics.get(id).map(_.subscribe(100))

  def addTopic(id: Long, topic: Topic[F, OutputMessages]): (WState[F], Stream[F, OutputMessages]) =
    copy(topics = topics + (id -> topic)) -> topic.subscribe(100)

  def getQueue(id: Long): Option[Queue[F, InputMessages]] = queues.get(id)

  def addQueue(id: Long, queue: Queue[F, InputMessages]): (WState[F], Queue[F, InputMessages]) =
    copy(queues = queues + (id -> queue)) -> queue
}

object WState { def init[F[_]: Async]: WState[F] = WState(Map.empty, Map.empty) }
