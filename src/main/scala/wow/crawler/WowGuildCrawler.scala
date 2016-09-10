package wow.crawler

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

import reactivemongo.api.commands.UpdateWriteResult
import spray.http.HttpResponse
import wow.dao.{Services, WowGuildService}
import wow.dto._
import spray.json._
import wow.dto.GuildInfoProtocol._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by Ross on 9/3/2016.
  */
//TODO decide which strategy is the best between this one and the one for the Api (impl as object)
class WowGuildCrawler(getWowGuild: WowGuildApi = WowGuildApiImpl,
                      wowGuildService:WowGuildService = Services.wowGuildService) {

  def crawlWowGuildFromWowProgressGuildList(): Future[List[(WowGuild, UpdateWriteResult)]] = {
    val futureResponseFromWowProgress: Future[HttpResponse] = getWowGuild.getGuildInfoFromWowProgress()

    val futureWowGuild:Future[List[GuildInfo]] = futureResponseFromWowProgress map { response =>
      val output: String = readGzipResponseAsString(response)
      output.parseJson.convertTo[List[WowProgressGuildInfo]]
    }
    futureWowGuild.flatMap(crawlWowGuildFrom(_))
  }

  def crawlWowGuildFromRealmPopGuildList(): Future[List[(WowGuild, UpdateWriteResult)]] = {
    val futureResponseFromRealmPop: Future[HttpResponse] = getWowGuild.getGuildInfoFromRealmPop()

    val futureWowGuild:Future[List[GuildInfo]] = futureResponseFromRealmPop map { response =>
      val output: String = readResponseAsString(response)
      val guildJson = output.substring(output.indexOf("\"guilds\":"))

      val hordeG = guildJson.substring(guildJson.indexOf("["), guildJson.indexOf("]") + 1)
      val indexOfFirstBraket = guildJson.indexOf("\"Alliance\":[") + 11
      val allianceG = guildJson.substring(indexOfFirstBraket, guildJson.indexOf("]", indexOfFirstBraket) + 1)

      val hordeGuilds: List[RealmPopGuildInfo] = hordeG.parseJson.convertTo[List[RealmPopGuildInfo]]
      val allianceGuilds: List[RealmPopGuildInfo] = allianceG.parseJson.convertTo[List[RealmPopGuildInfo]]

      hordeGuilds ++ allianceGuilds
    }
    futureWowGuild.flatMap(crawlWowGuildFrom(_))
  }

  //It makes sense that methods work with futures ? See WowCharacterCrawler
  private def crawlWowGuildFrom(guilds:List[GuildInfo]): Future[List[(WowGuild, UpdateWriteResult)]] ={
    Future.traverse(guilds) { guild =>
      val futureWowGuild:Future[WowGuild] = getWowGuild.getGuild(guild.getName())

      val futureDBWriting: Future[(WowGuild, UpdateWriteResult)] = futureWowGuild flatMap { wowGuild =>
          wowGuildService.insert(wowGuild)
      }
      futureDBWriting
    }
  }

  private def readResponseAsString(response: HttpResponse): String = {
    val guildFile = new BufferedReader(
      new InputStreamReader(
        new ByteArrayInputStream(response.entity.data.toByteArray)
      )
    )
    val output = guildFile.readLine()
    guildFile.close()
    output
  }

  private def readGzipResponseAsString(response: HttpResponse): String = {
    val guildFile = new BufferedReader(
      new InputStreamReader(
        new GZIPInputStream(
          new ByteArrayInputStream(response.entity.data.toByteArray)
        )
      )
    )
    val output = guildFile.readLine()
    guildFile.close()
    output
  }

}
