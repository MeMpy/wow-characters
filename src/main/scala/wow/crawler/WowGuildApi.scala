package wow.crawler

import akka.actor._
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import wow.dto.WowGuild
import wow.dto.WowGuildProtocol._

import scala.concurrent.duration._
import scala.concurrent.Future
import spray.json._
import akka.pattern._
import com.typesafe.scalalogging.LazyLogging


/**
  * Created by Ross on 8/14/2016.
  */
trait WowGuildApi extends SendReceiveClient with LazyLogging{
  import system.dispatcher

  private def getGuildInfoFrom(url:String):Future[HttpResponse] = {
    implicit val myTimeout: Timeout = 5 minutes
    val pipeline = sendAndReceive
    pipeline {
      Get(url)
    }
  }

  def getGuildInfoFromWowProgress():Future[HttpResponse] = {
    getGuildInfoFrom(WowUrls.wowProgressMaghteridon)
  }

  def getGuildInfoFromRealmPop():Future[HttpResponse] = {
    getGuildInfoFrom(WowUrls.realmPopMaghteridon)
  }

  //TODO change name to getGuildWithMembers
  def getGuildMembers(guildName: String):Future[WowGuild] = {
    val pipeline = sendAndReceive ~> unmarshal[WowGuild]
    val encodedGuildName = WowUrls.encodeParam(guildName)
    pipeline {
      Get(WowUrls.guildMembers.format(encodedGuildName))
    }
  }

  def getGuildWithMembers(guildName: String):Future[WowGuild] = {
    val encodedGuildName = WowUrls.encodeParam(guildName)
    val stream = system.actorOf(Props(new GuildStreamerActor()))

    implicit val timeout = Timeout(5 minutes)
    (stream ? encodedGuildName) map { response =>
      logger.debug("In the future")
      val json:String = response.asInstanceOf[String]
      json.parseJson.convertTo[WowGuild]
    }
  }
}

class GuildStreamerActor extends Actor with LazyLogging{
  val io = IO(Http)(context.system)
  var outsideSender: ActorRef = this.sender()
  var chunkedEntity:String = ""

  def receive: Receive = {
    case guildName: String =>
      logger.debug("string")
      outsideSender = sender()
    val rq = HttpRequest(HttpMethods.GET, uri = WowUrls.guildMembers.format(guildName))
    sendTo(io).withResponsesReceivedBy(self)(rq)
    case MessageChunk(entity,_) => {
//      logger.debug("In the message chunk")
//      logger.info(entity.asString)
      chunkedEntity = chunkedEntity + entity.asString
    }
    case x:HttpResponse  => {
      logger.debug("Normal response")
      //outsideSender ! x.entity.asString
    }
//    case x:ChunkedResponseStart =>
//      logger.info("start"+ x.response.entity.asString)
    case x:ChunkedMessageEnd => {
      logger.info(chunkedEntity)
//      outsideSender ! chunkedEntity
    }
    case _ => logger.info("any")
  }
}

object WowGuildApiImpl extends WowGuildApi