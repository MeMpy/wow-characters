package test.crawler

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import reactivemongo.api.commands.UpdateWriteResult
import test.UtilTest
import test.crawler.CrawlerMocksUtil.HttpClientMock._
import wow.crawler.{WowCharacterApi, WowCharacterCrawler, WowGuildApi}
import wow.dto.{WowCharacter, WowGuild}
import spray.json._
import wow.dao.WowCharacterService
import wow.dto.WowGuildProtocol._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Ross on 8/15/2016.
  */
class WowCharacterCrawlerSpecs extends Specification with Mockito{

  val wowCharacterApiMock = mock[WowCharacterApi]
  val wowGuildApiMock = mock[WowGuildApi]
  wowGuildApiMock.getGuildInfoFromWowProgress() returns {
    Future(
      mockResponseWithGzip("eu_magtheridon_tier18.json.gz")
    )
  }
  wowGuildApiMock.getGuildWithMembers(org.mockito.Matchers.anyString()) returns {
    val guildJson = UtilTest.readResourceAsString("WowGuildMembers.json")
    Future(guildJson.parseJson.convertTo[WowGuild])
  }

  val wowCharacterServiceMock = mock[WowCharacterService]
  wowCharacterServiceMock.insert(org.mockito.Matchers.any[WowCharacter]()) returns {
    Future( new WowCharacter("Mock", 1, 100) -> mock[UpdateWriteResult] )
  }

  "WowCharacterCrawler when crawls from wowProgress " should {
    "return a Future list of couples (WowCharacter, UpdateWriteResult)" >> {
      val crawler = new WowCharacterCrawler(wowCharacterApiMock, wowGuildApiMock, wowCharacterServiceMock)
      val futureRes = crawler.crawlWowCharacterFromWowProgressGuildList()

      val res = Await.result(futureRes, 10 seconds)

      res must have size(1266)

    }
  }
}
