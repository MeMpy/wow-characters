package wow.crawler

import akka.actor.ActorSystem
import spray.client.pipelining._

/**
  * Created by Ross on 8/14/2016.
  */
trait SendReceiveClient{
  implicit val system = ActorSystem()
  import system.dispatcher
  def sendAndReceive = sendReceive
}
