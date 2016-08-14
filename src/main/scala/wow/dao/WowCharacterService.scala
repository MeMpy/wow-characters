package wow.dao

import com.typesafe.scalalogging.LazyLogging
import wow.dto.WowCharacter
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Ross on 8/7/2016.
  */

trait WowCharacterService{
  def insert(pg: WowCharacter): Future[UpdateWriteResult]
}

class WowCharacterServiceImpl(wowCharacterCollection:Future[BSONCollection]) extends WowCharacterService with LazyLogging {
  import reactivemongo.bson._

  implicit val personWriter: BSONDocumentWriter[WowCharacter] = Macros.writer[WowCharacter]

  def insert(pg: WowCharacter): Future[UpdateWriteResult] = {
    val pgSelector = BSONDocument("name" -> pg.name)
    val result: Future[UpdateWriteResult] = wowCharacterCollection flatMap (
      _.update(pgSelector, pg, upsert = true)
      )

    result onComplete {
      case Failure(e) => logger.debug(e.getMessage)
      case Success(writeResult) => {
        logger.info(s"successfully inserted document: $writeResult")
      }
    }
    result
  }
}