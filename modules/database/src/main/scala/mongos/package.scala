import cats.effect.Async
import cats.implicits._
import io.circe.{Decoder, Encoder}
import mongo4cats.circe._
import mongo4cats.database.MongoDatabaseF
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY

import scala.reflect.ClassTag

package object mongos {
  case class Repos[F[_]: Async](
      account: repositories.Account[F],
      permission: repositories.Permission[F],
      otDocument: repositories.OTDocument[F]
  )

  def init[F[_]: Async](db: MongoDatabaseF[F]): F[Repos[F]] = for {
    accountCol    <- db.getCollectionWithCirceCodecs[models.Account]("accounts")
    permissionCol <- db.getCollectionWithCirceCodecs[models.Permission]("permissions")
    otDocumentCol <- db.getCollectionWithCodecRegistry[models.Document](
      "otDocuments",
      fromRegistries(
        fromProviders(
          circeBasedCodecProviderFull[models.Document],
          circeBasedCodecProviderFull[shared.ot.Operation]
        ),
        DEFAULT_CODEC_REGISTRY
      )
    )
  } yield Repos(
    new Account[F](accountCol),
    new Permission[F](permissionCol),
    new OTDocument[F](otDocumentCol)
  )

  private def circeBasedCodecProviderFull[T: ClassTag](implicit enc: Encoder[T], dec: Decoder[T]): CodecProvider = {
    import io.circe.parser.{decode => circeDecode}
    import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
    import org.bson.{BsonReader, BsonWriter}
    import org.mongodb.scala.bson.codecs.ImmutableDocumentCodec
    def circeBasedCodecProvider(implicit enc: Encoder[T], dec: Decoder[T], classT: Class[T]): CodecProvider =
      new CodecProvider {
        import org.mongodb.scala.bson.collection.immutable.Document
        def get[Y](classY: Class[Y], registry: CodecRegistry): Codec[Y] =
          if (classY == classT) {
            new Codec[Y] {
              private val documentCodec: Codec[Document] = ImmutableDocumentCodec(registry).asInstanceOf[Codec[Document]]
              def encode(writer: BsonWriter, t: Y, encoderContext: EncoderContext): Unit = {
                val document = Document(enc(t.asInstanceOf[T]).noSpaces)
                documentCodec.encode(writer, document, encoderContext)
              }
              def getEncoderClass: Class[Y] = classY
              def decode(reader: BsonReader, decoderContext: DecoderContext): Y = {
                val documentJson = documentCodec.decode(reader, decoderContext).toJson()
                circeDecode[T](documentJson).fold(e => throw MongoJsonParsingException(documentJson, e.getMessage), _.asInstanceOf[Y])
              }
            }
          } else null
      }
    implicit val classT: Class[T] = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    circeBasedCodecProvider
  }

}
