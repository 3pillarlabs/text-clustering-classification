package controllers

import java.io.File

import actors.BbcClusteringActor
import akka.actor.ActorRef
import domain.{BbcClusterCommand, ClusterResponse}
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._

class BBCNewsController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.bbcnews.index(request))
  }

  implicit val inEventFormat = Json.format[BbcClusterCommand]

  implicit val clusterResponseReads = ClusterResponse.clusterResponseReads
  implicit val clusterResponseWrites = ClusterResponse.clusterResponseWrites

  implicit val inEventFrameFormatter = FrameFormatter.jsonFrame[BbcClusterCommand]
  implicit val outEventFrameFormatter = FrameFormatter.jsonFrame[ClusterResponse]

  def createCluster = WebSocket.acceptWithActor[BbcClusterCommand, ClusterResponse] { request =>
    val config = play.api.Play.current.configuration
    (out: ActorRef) => BbcClusteringActor.props(out, config)
  }

  def clusterOutput(clusterId: String) = Action {
    val config = play.api.Play.current.configuration
    Ok.sendFile(
      content = new File(config.getString("app.cluster.outputPath").get, clusterId),
      inline = true
    )
  }

  def clusterNode(category: String, file:String) = Action {
    val config = play.api.Play.current.configuration
    Ok.sendFile(
      content = new File(config.getString("app.cluster.bbc.inputPath").get, s"/$category/$file"),
      inline = true
    )
  }

}
