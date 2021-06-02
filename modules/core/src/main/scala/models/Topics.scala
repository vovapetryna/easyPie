package models

import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic

case class Topics[F[_]](topics: Map[Long, Topic[F, OutputMessages]]) {
  def subscribe(id: Long): Option[Stream[F, OutputMessages]] = topics.get(id).map(_.subscribe(100))

  def addTopic(id: Long, topic: Topic[F, OutputMessages]): (Topics[F], Stream[F, OutputMessages]) =
    copy(topics = topics + (id -> topic)) -> topic.subscribe(100)
}

object Topics { def init[F[_]]: Topics[F] = Topics(Map.empty) }
