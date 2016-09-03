import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import wow.crawler._
import wow.dao._
import wow.dto.WowGuild
import spray.json._
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.routing._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute ~ wowAuctionRoute ~ wowProgressGuild ~ wowGuildRoute ~ wowRealmPop)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService with LazyLogging {

  val getWowCharacter:WowCharacterApi = WowCharacterApiImpl
  val getWowGuild:WowGuildApi = WowGuildApiImpl

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to
                  <i>spray-routing</i>
                  on
                  <i>spray-can</i>
                  !</h1>
                <ul>
                  <li>
                    <a href="wowprogress">start collect characters from guilds</a>
                  </li>
                  <li>
                    <a href="pg/Mylkha">get info on a single character in magtheridon (change the name in the url to change character)</a>
                  </li>
                  <li>
                    <a href="guild/Noobs%20tbh">get info on a single guild and all its members in magtheridon (change the name in the url to change character)</a>
                  </li>
                </ul>
              </body>
            </html>
          }
        }
      }
    }

  import ExecutionContext.Implicits.global

  val wowAuctionRoute =
    path("pg" / Segment) { pgName =>
      get {
        onComplete(getWowCharacter.getWowCharacterBase(pgName)) {
          case Success(pg) => {
            val dao = Services.wowCharacterService
            dao.insert(pg)
            complete(pg.toString)
          }
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }

  val wowProgressGuild =
    path("wowprogress") {
      get {
        val wowCharacterCrawler = new WowCharacterCrawler()
        onComplete(wowCharacterCrawler.crawlWowCharacterFromWowProgressGuildList()) {
          case Success(wowGuilds) => complete(wowGuilds mkString "/")
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }

  val wowRealmPop =
    path("realmpop") {
      get {
        val wowCharacterCrawler = new WowCharacterCrawler()
        onComplete(wowCharacterCrawler.crawlWowCharacterFromRealmPopGuildList()) {
          case Success(wowGuilds) => complete(wowGuilds.size.toString())
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }

  val wowGuildRoute =
    path("guild" / Segment) { guildName =>
      get {
        onComplete(getWowGuild.getGuildMembers(guildName)) {
          case Success(guild) => {
            complete(guild.toString)
          }
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
}