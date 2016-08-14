package wow.crawler

import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._
import wow.dto.WowCharacter
import wow.dto.WowCharacterProtocol._

import scala.concurrent.Future

/**
  * Created by Ross on 8/7/2016.
  */
trait WowCharacterApi extends SendReceiveClient {
  import system.dispatcher

  def getWowCharacterBase(pg: String): Future[WowCharacter] = {
    val pipeline = sendAndReceive ~> unmarshal[WowCharacter]
    pipeline {
      Get(WowUrls.character.format(pg))
    }
  }
}

object WowCharacterApiImpl extends WowCharacterApi


