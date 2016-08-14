package wow.dto

import spray.json.DefaultJsonProtocol

/**
  * Created by Ross on 8/14/2016.
  */
case class WowGuild(name:String, members:Option[List[WowCharacterLink]])
object WowGuildProtocol extends DefaultJsonProtocol {
  import WowCharacterLinkProtocol._
  implicit val guildFormat = jsonFormat2(WowGuild)
}
