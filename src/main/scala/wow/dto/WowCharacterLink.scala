package wow.dto

import spray.json.DefaultJsonProtocol

/**
  * Created by Ross on 8/14/2016.
  */
//TODO I don't like it. Try to create a custom protocol
case class WowCharacterLink(character:WowCharacter)
object WowCharacterLinkProtocol extends DefaultJsonProtocol {
  import WowCharacterProtocol._
  implicit val characterLinkFormat = jsonFormat1(WowCharacterLink)
}
