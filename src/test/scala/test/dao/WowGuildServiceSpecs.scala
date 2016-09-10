package test.dao

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.{BeforeAfter, Specification}
import org.specs2.specification.Scope
import org.specs2.specification.mutable.ExecutionEnvironment
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, Macros}
import wow.dao.{Collections, Services}
import wow.dto.{WowCharacter, WowCharacterLink, WowGuild}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Ross on 9/4/2016.
  */
class WowGuildServiceSpecs extends Specification with ExecutionEnvironment with LazyLogging {

  def is(implicit ee: ExecutionEnv) = {
    //Since unit tests are executed in parallel we cannot rely on before and after
    //the will be executed before and after the unit test but no before and after each other
    //this means that they cannot clean the database since it is shared among the unit tests
    sequential

    "Insert if not exists should succeed" in new WowGuildServiceContext {

      val guild = WowGuild("Test", None)
      service.insert(guild)

      val size = Collections.wowGuildCollection flatMap { collection =>
        collection.find(BSONDocument())
          .cursor()
          .collect[List]() map (_.size)
      }

      size must be_==(1).awaitFor(timeOut)
    }

    "Insert if exsist should update" in new WowGuildServiceContext {

//      implicit val reader: BSONDocumentReader[WowGuild] = Macros.reader[WowGuild]
//      implicit val readerLink: BSONDocumentReader[WowCharacterLink] = Macros.reader[WowCharacterLink]
//      implicit val readerChar: BSONDocumentReader[WowCharacter] = Macros.reader[WowCharacter]
      implicit object reader extends BSONDocumentReader[WowGuild] {
        def read(bson: BSONDocument): WowGuild =
          new WowGuild(bson.getAs[String]("name").get, None)
      }
      val guild = WowGuild("Test2", None)
      val guild2 = WowGuild("Test2", None)

      Await.result(service.insert(guild), timeOut)
      Await.result(service.insert(guild2), timeOut)

      val result = Collections.wowGuildCollection flatMap { collection =>
        collection.find(BSONDocument())
          .cursor[WowGuild]()
          .collect[List]()
      }

      result onSuccess{
        case r => logger.debug(r.head.toString)
      }
      result map(_.size) must be_==(1).awaitFor(timeOut)
      result map(_.head) must be_==(guild2).awaitFor(timeOut)
    }
  }
}

//TODO use context here can be cumbersome
trait WowGuildServiceContext extends Scope with BeforeAfter with LazyLogging{
  val service = Services.wowGuildService
  val timeOut = 10 seconds
  def before = Collections.wowGuildCollection map(_.drop(true))
  def after = Collections.wowGuildCollection map(_.drop(true))

}
