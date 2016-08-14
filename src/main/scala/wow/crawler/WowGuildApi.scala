package wow.crawler

import spray.client.pipelining._
import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._
import wow.dto.WowGuild
import wow.dto.WowGuildProtocol._

/**
  * Created by Ross on 8/14/2016.
  */
trait WowGuildApi extends SendReceiveClient {
  import system.dispatcher

  def getGuildInfoFromWowProgress() = {
    val pipeline = sendAndReceive
    pipeline {
      Get(WowUrls.wowProgressMaghteridon)
    }
  }

  def getGuildMembers(guildName: String) = {
    val pipeline = sendAndReceive ~> unmarshal[WowGuild]
    val encodedGuildName = WowUrls.encodeParam(guildName)
    pipeline {
      Get(WowUrls.guildMembers.format(encodedGuildName))
    }
  }
}

object WowGuildApiImpl extends WowGuildApi