package models

import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic
import shared.messages.Output

case class Topics[F[_]](topics: Map[String, Topic[F, Output]]) {
  def subscribe(id: String): Option[Stream[F, Output]] = topics.get(id).map(_.subscribe(100))

  def addTopic(id: String, topic: Topic[F, Output]): (Topics[F], Stream[F, Output]) =
    copy(topics = topics + (id -> topic)) -> topic.subscribe(100)
}

object Topics { def init[F[_]]: Topics[F] = Topics(Map.empty) }
