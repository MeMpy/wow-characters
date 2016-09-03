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

trait GuildInfo {
  def getName():String = this match {
    case WowProgressGuildInfo(name) => name
    case RealmPopGuildInfo(name, _) => name
  }

  def getMemberCount: Int = this match {
    case WowProgressGuildInfo(_) => 1 //Any number greater then zero. We suppose that in wowProgress the guild are never empty
    case RealmPopGuildInfo(_, count) => count
  }
}
case class WowProgressGuildInfo(name:String) extends GuildInfo
case class RealmPopGuildInfo(guild:String, membercount:Int) extends GuildInfo
object GuildInfoProtocol extends DefaultJsonProtocol {
  implicit val wowProgressGuildInfoFormat = jsonFormat1(WowProgressGuildInfo)
  implicit val realmPopGuildInfo = jsonFormat2(RealmPopGuildInfo)
}