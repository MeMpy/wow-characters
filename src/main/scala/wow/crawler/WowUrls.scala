package wow.crawler

import java.net.URLEncoder

import akka.actor.ActorSystem
import spray.client.pipelining._
import com.typesafe.config.ConfigFactory

/**
  * Created by Ross on 8/13/2016.
  */
object WowUrls {
  private val config = ConfigFactory.load()

  def encodeParam(param:String):String = URLEncoder.encode(param, "UTF-8").replaceAll("\\+", "%20")

  val character = config.getString("api.blizzard.wow.characterBase")
  val guildMembers = config.getString("api.blizzard.wow.guildMembers")
  val wowProgressMaghteridon = config.getString("api.wowProgressGuild")
}
