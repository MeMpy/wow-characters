package wow.crawler

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

import reactivemongo.api.commands.UpdateWriteResult
import spray.http.HttpResponse
import spray.json._
import wow.dao.{Services, WowCharacterService, WowGuildService}
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
                          wowCharacterService: WowCharacterService = Services.wowCharacterService,
                          wowGuildService:WowGuildService = Services.wowGuildService) {

  def crawlWowCharacterFromGuilds(): Future[List[(WowCharacter, UpdateWriteResult)]] = {
    wowGuildService.getAll() flatMap { guilds =>

      val listOfFutures: List[Future[List[(WowCharacter, UpdateWriteResult)]]] = guilds map { guild =>
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

  private def extractCharactersFromGuild(guild: WowGuild): Future[List[WowCharacter]] = {
    val futureWowGuild = getWowGuild.getGuildWithMembers(guild.name)
    val futureMembers:Future[List[WowCharacterLink]] = futureWowGuild.map(_.members.get)

    val futureCharacter = futureMembers.map(_.map(_.character))
    futureCharacter
  }

//  private def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
//    f.map(Success(_)).recover({case x => Failure(x)})
}
