package test.crawler

import org.specs2.mutable.Specification
import spray.http.HttpResponse
import test.crawler.CrawlerMocksUtil.HttpClientMock
import test.crawler.CrawlerMocksUtil.HttpClientMock._
import wow.crawler.WowGuildApi
import wow.dto.WowGuild

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Ross on 8/14/2016.
  */
class WowGuildApiSpecs extends Specification{

  val client = new WowGuildApi {
    override def sendAndReceive = HttpClientMock.sendAndReceive
  }

  "A request to wowProgress data export" should {
    "return an HttpResponse containing a gzip file" >> {
      mockResponseWithGzip("eu_magtheridon_tier18.json.gz")
      val futureResp = client.getGuildInfoFromWowProgress()
      val res = Await.result(futureResp, 10 seconds)

      res must beAnInstanceOf[HttpResponse]
      //TODO imporve assert
    }
  }

  "A request to blizzard guild api " should {
    "return an object of type WowGuild with members" >> {
      mockResponseWithJson("WowGuildMembers.json")
      val futureResp = client.getGuildMembers("Mock")
      val res = Await.result(futureResp, 10 seconds)

      res must beAnInstanceOf[WowGuild]
      res.members must beSome

      val members = res.members.get
      members must have size(633)
    }
  }
}
