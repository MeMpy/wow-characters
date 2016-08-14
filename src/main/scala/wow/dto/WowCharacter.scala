package wow.dto

import spray.json.DefaultJsonProtocol

/**
  * Created by Ross on 8/7/2016.
  */
case class WowCharacter(name:String, `class`: Int, level: Int)
object WowCharacterProtocol extends DefaultJsonProtocol {
  implicit val characterFormat = jsonFormat3(WowCharacter)
}
