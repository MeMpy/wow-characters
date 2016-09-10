package test.crawler

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import reactivemongo.api.commands.UpdateWriteResult
import test.UtilTest
import test.crawler.CrawlerMocksUtil.HttpClientMock._
import wow.crawler.{WowCharacterCrawler, WowGuildApi, WowGuildCrawler}
import wow.dto.{WowCharacter, WowGuild}
import spray.json._
import wow.dao.{WowCharacterService, WowGuildService}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import wow.dto.WowGuildProtocol._
import scala.concurrent.duration._

/**
  * Created by Ross on 9/4/2016.
  */
class WowGuildCrawlerSpecs extends Specification with Mockito{

  val wowGuildApiMock = mock[WowGuildApi]
  wowGuildApiMock.getGuildInfoFromWowProgress() returns {
    Future(
      mockResponseWithGzip("eu_magtheridon_tier18.json.gz")
    )
  }
  wowGuildApiMock.getGuildInfoFromRealmPop() returns {
    Future(
      mockResponseWithJson("eu-magtherido-realpop-short.json")
    )
  }

  wowGuildApiMock.getGuild(org.mockito.Matchers.anyString()) returns {
    val guildJson = UtilTest.readResourceAsString("WowGuildBase.json")
    Future(guildJson.parseJson.convertTo[WowGuild])
  }

  val wowGuildServiceMock = mock[WowGuildService]
  wowGuildServiceMock.insert(org.mockito.Matchers.any[WowGuild]()) returns {
    Future( new WowGuild("Mock",None) -> mock[UpdateWriteResult] )
  }

  "WowGuildCrawler when crawls from wowProgress " should {
    "return a Future list of couples (WowGuild, UpdateWriteResult)" >> {
      val crawler = new WowGuildCrawler(wowGuildApiMock, wowGuildServiceMock)
      val futureRes = crawler.crawlWowGuildFromWowProgressGuildList()

      val res = Await.result(futureRes, 10 seconds)

      res must have size(238)

    }
  }

  "WowGuildCrawler when crawls from realmPop " should {
    "return a Future list of couples (WowGuild, UpdateWriteResult)" >> {
      val crawler = new WowGuildCrawler(wowGuildApiMock, wowGuildServiceMock)
      val futureRes = crawler.crawlWowGuildFromRealmPopGuildList()

      val res = Await.result(futureRes, 10 seconds)

      res must have size(5531)

    }
  }

}
