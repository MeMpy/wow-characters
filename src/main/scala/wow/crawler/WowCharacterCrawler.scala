package wow.crawler

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

import spray.json._
import wow.dao.Services
import wow.dto.WowGuild
import wow.dto.WowGuildProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


/**
  * Created by Ross on 8/13/2016.
  */
class WowCharacterCrawler(getWowCharacter: WowCharacterApi = WowCharacterApiImpl,
                          getWowGuild: WowGuildApi = WowGuildApiImpl) {

  def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
    f.map(Success(_)).recover({case x => Failure(x)})


  def crawlWowCharacterFromWowProgressGuildList() = {
    getWowGuild.getGuildInfoFromWowProgress() flatMap { response =>
      val guildFile = new BufferedReader(
        new InputStreamReader(
          new GZIPInputStream(
            new ByteArrayInputStream(response.entity.data.toByteArray)
          )
        )
      )
      val output = guildFile.readLine()
      guildFile.close()

      val json = output.parseJson

      val guilds = json.convertTo[List[WowGuild]]

      val listOfFutures = guilds.slice(0,2) map { guild =>
        val futureWowGuild = getWowGuild.getGuildMembers(guild.name)
        val futureMembers = futureWowGuild.map(_.members.get)

        val futureCharacter = futureMembers.map(_.map(_.character))
        val futureDBWriting = futureCharacter.flatMap(characters => Future.traverse(characters) {
          character => Services.wowCharacterService.insert(character)
        })
        futureDBWriting
      }

      val listOfFutureTrys = listOfFutures.map(futureToFutureTry(_))
      val futureListOfTrys = Future.sequence(listOfFutureTrys)
      futureListOfTrys.map(_.filter(_.isSuccess))

    }
  }
}
