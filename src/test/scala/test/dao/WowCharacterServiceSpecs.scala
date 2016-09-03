package test.dao

import com.typesafe.scalalogging.LazyLogging
import wow.dto.WowCharacter
import org.specs2.mutable.{BeforeAfter, Specification}
import org.specs2.specification.Scope
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.mutable.ExecutionEnvironment
import wow.dao.{Collections, Services}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Ross on 8/7/2016.
  */

class WowCharacterServiceSpecs extends Specification with ExecutionEnvironment with LazyLogging {

  def is(implicit ee: ExecutionEnv) = {
    //Since unit tests are executed in parallel we cannot rely on before and after
    //the will be executed before and after the unit test but no before and after each other
    //this means that they cannot clean the database since it is shared among the unit tests
    sequential

    "Insert if not exists should succeed" in new WowCharacterServiceContext {

      val pg = WowCharacter("Test", 1, 1)
      service.insert(pg)

      val size = Collections.wowCharacterCollection flatMap { collection =>
        collection.find(BSONDocument())
          .cursor()
          .collect[List]() map (_.size)
      }

      size must be_==(1).awaitFor(timeOut)
    }

    "Insert if exsist should update" in new WowCharacterServiceContext {

      implicit val personReader: BSONDocumentReader[WowCharacter] = Macros.reader[WowCharacter]
      val pg = WowCharacter("Test2", 1, 1)
      val pg2 = WowCharacter("Test2", 0, 0)

      Await.result(service.insert(pg), timeOut)
      Await.result(service.insert(pg2), timeOut)

      val result = Collections.wowCharacterCollection flatMap { collection =>
        collection.find(BSONDocument())
          .cursor[WowCharacter]()
          .collect[List]()
      }

      result onSuccess{
        case r => logger.debug(r.head.toString)
      }
      result map(_.size) must be_==(1).awaitFor(timeOut)
      result map(_.head) must be_==(pg2).awaitFor(timeOut)
    }
  }
}

//TODO use context here can be cumbersome
trait WowCharacterServiceContext extends Scope with BeforeAfter with LazyLogging{
  val service = Services.wowCharacterService
  val timeOut = 10 seconds
  def before = Collections.wowCharacterCollection map(_.drop(true))
  def after = Collections.wowCharacterCollection map(_.drop(true))

}

