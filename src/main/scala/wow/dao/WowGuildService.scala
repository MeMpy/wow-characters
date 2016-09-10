package wow.dao

import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}
import wow.dto.WowGuild

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Ross on 9/3/2016.
  */
trait WowGuildService {
  def insert(guild: WowGuild): Future[(WowGuild,UpdateWriteResult)]

  def getAll():Future[List[WowGuild]]
}



class WowGuildServiceImpl(wowGuildCollection:Future[BSONCollection]) extends WowGuildService with LazyLogging {

  implicit object wowGuildWriter extends BSONDocumentWriter[WowGuild] {
    override def write(t: WowGuild): BSONDocument =
      BSONDocument("name" -> t.name)
  }

  implicit object reader extends BSONDocumentReader[WowGuild] {
    def read(bson: BSONDocument): WowGuild =
      new WowGuild(bson.getAs[String]("name").get, None)
  }

  override def getAll(): Future[List[WowGuild]] =
    Collections.wowGuildCollection flatMap { collection =>
      collection.find(BSONDocument())
        .cursor[WowGuild]()
        .collect[List]()
    }

  def insert(guild: WowGuild): Future[(WowGuild, UpdateWriteResult)] = {
    val guildSelector = BSONDocument("name" -> guild.name)
    val result: Future[UpdateWriteResult] = wowGuildCollection flatMap (
      _.update(guildSelector, guild, upsert = true)
      )

    result onComplete {
      case Failure(e) => logger.debug(e.getMessage)
      case Success(writeResult) => {
        logger.info(s"successfully inserted guild: $writeResult")
      }
    }
    result.map(guild -> _)
  }
}

