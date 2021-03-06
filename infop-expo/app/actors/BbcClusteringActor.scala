package actors

import java.io.{File, FilenameFilter, IOException}
import java.nio.file.Files
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.util.Timeout
import domain.{BbcClusterCommand, ClusterResponse, MessageType, SparkCommand}
import play.api.Configuration
import util.SparkFacade

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by sayantamd on 29/2/16.
  */

object BbcClusteringActor {
  def props(out: ActorRef, config: Configuration): Props = Props(new BbcClusteringActor(out, config))
}

class BbcClusteringActor(out: ActorRef, config: Configuration) extends Actor {

  val log = Logging(context.system, this)

  override def receive: Receive = {
    case command: BbcClusterCommand =>
      val totalDocuments = countDocuments(command.category)
      val processDocuments = command.processCount(totalDocuments)
      val inputPath = setupProcessDir(processDocuments, command.category)

      val outputDirPath = new File(config.getString("app.cluster.outputPath").get)
      val outputFileName: String = "%s.json".format(UUID.randomUUID())
      val outputFilePath = new File(outputDirPath, outputFileName).getAbsolutePath

      out ! ClusterResponse.withData(Map("totalDocuments" -> totalDocuments.toString), MessageType.ClusterStart)

      import play.api.libs.concurrent.Execution.Implicits._
      val future = Future {
        SparkFacade.invokeSpark(config.getConfig("app.cluster.spark").get, SparkCommand(inputPath, outputFilePath))
      }

      future.onSuccess {
        case _ =>
          val dd = new File(inputPath)
          dd.listFiles().foreach((f) => { Files.delete(f.toPath) })
          Files.delete(dd.toPath)
      }

      while (!future.isCompleted) {
        Thread.sleep(1000)
        out ! ClusterResponse(MessageType.HeartBeat)
      }

      try {
        import scala.language.postfixOps
        Await.result(future, Timeout(1 second).duration)
        val data = Map(
          "processDocuments" -> processDocuments.toString,
          "clusterId" -> outputFileName
        )
        out ! ClusterResponse.withData(data, MessageType.ClusterSuccess)
      } catch {
        case ex: Exception => out ! ClusterResponse.withException(ex)
      }
  }

  def countDocuments(category: String): Int = {
    val inputPath = new File(config.getString("app.cluster.bbc.inputPath").get)
    filterInputFiles(category, inputPath).length
  }

  def filterInputFiles(category: String, inputPath: File): Array[File] = {
    new File(inputPath, category).listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        name.endsWith(".txt")
      }
    })
  }

  def setupProcessDir(fileCount: Int, category: String): String = {
    val inputPath = new File(config.getString("app.cluster.bbc.inputPath").get)
    val processPath = config.getString("app.cluster.processPath").get
    val processDir = new File(processPath, UUID.randomUUID().toString)

    // create the process directory
    if (!processDir.mkdir()) throw new IOException(s"Failed to create directory: $processDir")

    // copy files from data to process
    filterInputFiles(category, inputPath)
      .take(fileCount)
      .foreach((f) => java.nio.file.Files.copy(f.toPath, new File(processDir, f.getName).toPath))

    processDir.getAbsolutePath
  }

}
