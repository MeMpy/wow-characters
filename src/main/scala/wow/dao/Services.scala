package wow.dao

import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Ross on 8/7/2016.
  */
object Collections {
  val wowCharacterCollection = Config.database map
    (_.collection[BSONCollection]("characters"))

  val wowGuildCollection = Config.database map
    (_.collection[BSONCollection]("guilds"))
}

object Services {
  val wowCharacterService = new WowCharacterServiceImpl(Collections.wowCharacterCollection)
  val wowGuildService = new WowGuildServiceImpl(Collections.wowGuildCollection)
}
