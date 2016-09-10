package wow.dao

import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson._
import wow.dto.WowCharacter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by Ross on 8/7/2016.
  */

trait WowCharacterService{
  def insert(pg: WowCharacter): Future[(WowCharacter,UpdateWriteResult)]
}

class WowCharacterServiceImpl(wowCharacterCollection:Future[BSONCollection]) extends WowCharacterService with LazyLogging {

  implicit val wowCharacterWriter: BSONDocumentWriter[WowCharacter] = Macros.writer[WowCharacter]

  def insert(pg: WowCharacter): Future[(WowCharacter,UpdateWriteResult)] = {
    val pgSelector = BSONDocument("name" -> pg.name)
    val result: Future[UpdateWriteResult] = wowCharacterCollection flatMap (
      _.update(pgSelector, pg, upsert = true)
      )

    result onComplete {
      case Failure(e) => logger.debug(e.getMessage)
      case Success(writeResult) => {
        logger.info(s"successfully inserted charachter: $writeResult")
      }
    }
    result.map(pg -> _)
  }
}