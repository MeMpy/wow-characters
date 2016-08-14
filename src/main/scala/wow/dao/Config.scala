package wow.dao

import com.typesafe.config.ConfigFactory
import reactivemongo.api.MongoDriver

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Ross on 8/7/2016.
  */
object Config {
//  private def customConfig: com.typesafe.config.Config = ???
  private val config = ConfigFactory.load()
  //  val driver = new MongoDriver(Some(customConfig))
  private val driver = new MongoDriver

  val connection = driver.connection(List(config.getString("mongo.uri")))
  val database = connection.database(config.getString("mongo.db"))
}
