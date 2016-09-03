package wow.crawler

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

import reactivemongo.api.commands.UpdateWriteResult
import spray.http.HttpResponse
import spray.json._
import wow.dao.{Services, WowCharacterService}
import wow.dto._
import wow.dto.GuildInfoProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


/**
  * Created by Ross on 8/13/2016.
  */
class WowCharacterCrawler(getWowCharacter: WowCharacterApi = WowCharacterApiImpl,
                          getWowGuild: WowGuildApi = WowGuildApiImpl,
                          wowCharacterService: WowCharacterService = Services.wowCharacterService) {

  def crawlWowCharacterFromWowProgressGuildList():Future[List[(WowCharacter, UpdateWriteResult)]] = {
    val futureResponseFromWowProgress:Future[HttpResponse] = getWowGuild.getGuildInfoFromWowProgress()

    val futureWowGuild = futureResponseFromWowProgress map { response =>
      val output: String = readGzipResponseAsString(response)
      output.parseJson.convertTo[List[WowProgressGuildInfo]]
    }
    crawlWowCharacterFrom(futureWowGuild)
  }

  def crawlWowCharacterFromRealmPopGuildList():Future[List[(WowCharacter, UpdateWriteResult)]] = {
    val futureResponseFromRealmPop:Future[HttpResponse] = getWowGuild.getGuildInfoFromRealmPop()

    val futureWowGuild = futureResponseFromRealmPop map { response =>
      val output: String = readResponseAsString(response)
      val guildJson = output.substring(output.indexOf("\"guilds\":"))

      val hordeG= guildJson.substring(guildJson.indexOf("["), guildJson.indexOf("]")+1)
      val indexOfFirstBraket = guildJson.indexOf("\"Alliance\":[")+11
      val allianceG = guildJson.substring(indexOfFirstBraket, guildJson.indexOf("]",indexOfFirstBraket)+1)

      val hordeGuilds:List[RealmPopGuildInfo] = hordeG.parseJson.convertTo[List[RealmPopGuildInfo]]
      val allianceGuilds:List[RealmPopGuildInfo] = allianceG.parseJson.convertTo[List[RealmPopGuildInfo]]

      hordeGuilds ++ allianceGuilds
    }
    crawlWowCharacterFrom(futureWowGuild)
  }

  private def crawlWowCharacterFrom(futureWowGuilds: Future[List[GuildInfo]]): Future[List[(WowCharacter, UpdateWriteResult)]] = {
    futureWowGuilds flatMap { guilds =>

      val listOfFutures: List[Future[List[(WowCharacter, UpdateWriteResult)]]] = guilds.slice(0, 2) map { guild =>
        val futureCharacters: Future[List[WowCharacter]] = extractCharactersFromGuild(guild)

        val futureDBWriting: Future[List[(WowCharacter, UpdateWriteResult)]] = futureCharacters flatMap { characters =>
          //Use of Future.traverse in order to convert
          // List[Future[DBResult]] to a Future[List[DBResult]]
          // so flatMap can be applied
          Future.traverse(characters) {
            character => wowCharacterService.insert(character) //Future(character) //Future( character -> Services.wowCharacterService.insert(character) )
          }
        }
        futureDBWriting
      }

      val futureOfList = Future.sequence(listOfFutures).map(_.flatten)
      futureOfList
    }
  }

  private def extractCharactersFromGuild(guild: GuildInfo): Future[List[WowCharacter]] = {
//    val futureWowGuild = getWowGuild.getGuildMembers(guild.getName())
    val futureWowGuild = getWowGuild.getGuildWithMembers(guild.getName())
    val futureMembers:Future[List[WowCharacterLink]] = futureWowGuild.map(_.members.get)

    val futureCharacter = futureMembers.map(_.map(_.character))
    futureCharacter
  }

  private def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
    f.map(Success(_)).recover({case x => Failure(x)})

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
