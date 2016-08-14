package test.crawler

import org.specs2.mutable.Specification
import test.crawler.CrawlerMocksUtil.HttpClientMock
import test.crawler.CrawlerMocksUtil.HttpClientMock._
import wow.crawler.WowCharacterApi
import wow.dto.WowCharacter

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Ross on 8/13/2016.
  */
class WowCharacterApiSpecs  extends Specification{

  val client = new WowCharacterApi {
    override def sendAndReceive = HttpClientMock.sendAndReceive
  }

  "A request to blizzard character api" should {
    "return an WowCharacter object" >> {
      mockResponseWithJson("WowCharacterBase.json")
      val futureResp = client.getWowCharacterBase("Mock")
      val res = Await.result(futureResp, 10 seconds)

      res must beAnInstanceOf[WowCharacter]
      //TODO improve assert
    }
  }
}
