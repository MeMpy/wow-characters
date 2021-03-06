package test.crawler

import org.specs2.mock.Mockito
import spray.http.{HttpEntity, _}
import test.UtilTest

import scala.concurrent.Future

/**
  * Created by Ross on 8/13/2016.
  */
object CrawlerMocksUtil {
  //TODO this code sucks improve
  object HttpClientMock extends Mockito {
    val mockResponse = mock[HttpResponse]
    val mockStatus = mock[StatusCode]
    mockResponse.status returns mockStatus
    mockStatus.isSuccess returns true

    def mockResponseWithJson(jsonFileName: String) = {
      val mockResponseTemp = mock[HttpResponse]
      val jsonBytes = UtilTest.readResource(jsonFileName)
      val body = HttpEntity(ContentTypes.`application/json`, jsonBytes)
      mockResponse.entity returns body
      mockResponseTemp.entity returns body
      mockResponseTemp
    }

    def mockResponseWithGzip(gzipFileName: String):HttpResponse = {
      val mockResponseTemp = mock[HttpResponse]
      val gzipBytes = UtilTest.readResource(gzipFileName)
      val body = HttpEntity(ContentTypes.`application/octet-stream`, gzipBytes)
      mockResponse.entity returns body
      mockResponseTemp.entity returns body
      mockResponseTemp
    }

    val sendAndReceive = {
      (req: HttpRequest) => Future.successful(mockResponse)
    }
  }

}
