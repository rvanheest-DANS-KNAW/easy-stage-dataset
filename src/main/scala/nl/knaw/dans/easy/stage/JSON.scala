package nl.knaw.dans.easy.stage

import java.io.File

import nl.knaw.dans.easy.stage.Constants._
import nl.knaw.dans.easy.stage.Util._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.util.Try

object JSON {

  def createDatasetCfg(sdoDir: File)(implicit s: Settings): Try[Unit] = {
    def sdoCfg(audiences: Seq[String]) =
      ("namespace" -> "easy-dataset") ~
      ("datastreams" -> List(
        ("contentFile" -> "AMD") ~
        ("dsID" -> "AMD") ~
        ("controlGroup" -> "R") ~
        ("mimeType" -> "application/xml")
        ,
        ("contentFile" -> "EMD") ~
        ("dsID" -> "EMD") ~
        ("controlGroup" -> "R") ~
        ("mimeType" -> "application/xml")
        ,
        ("contentFile" -> "PRSQL") ~
        ("dsID" -> "PRSQL") ~
        ("controlGroup" -> "R") ~
        ("mimeType" -> "application/xml")
      )) ~
      ("relations" -> (List(
        ("predicate" -> "http://dans.knaw.nl/ontologies/relations#hasDoi") ~ ("object" -> s.DOI),
        ("predicate" -> "http://dans.knaw.nl/ontologies/relations#hasPid") ~ ("object" -> s.URN),
        ("predicate" -> "info:fedora/fedora-system:def/model#hasModel") ~ ("object" -> "info:fedora/dans-model:recursive-item-v1"),
        ("predicate" -> "info:fedora/fedora-system:def/model#hasModel") ~ ("object" -> "info:fedora/easy-model:EDM1DATASET"),
        ("predicate" -> "info:fedora/fedora-system:def/model#hasModel") ~ ("object" -> "info:fedora/easy-model:oai-item1")
        ) ++ audiences.map(audience =>
          ("predicate" -> "http://dans.knaw.nl/ontologies/relations#:isMemberOf") ~ ("object" -> s"info:fedora/${s.disciplines(audience)}"))
      ))

    for {
      audiences <- readAudiences()
      _ <- writeToFile(new File(sdoDir.getPath, JSON_CFG_FILENAME), pretty(render(sdoCfg(audiences))))
    } yield ()
  }

  def createFileCfg(fileLocation: String, mimeType: String, parentSDO: String, sdoDir: File): Try[Unit] = {
    val sdoCfg =
      ("namespace" -> "easy-file") ~
      ("datastreams" -> List(
        ("dsLocation" -> fileLocation) ~
        ("dsID" -> "EASY_FILE") ~
        ("controlGroup" -> "R") ~
        ("mimeType" -> mimeType))) ~
      ("relations" -> List(
        ("predicate" -> "http://dans.knaw.nl/ontologies/relations#:isMemberOf") ~ ("objectSDO" -> parentSDO),
        ("predicate" -> "http://dans.knaw.nl/ontologies/relations#:isSubordinateTo") ~ ("objectSDO" -> DATASET_SDO),
        ("predicate" -> "info:fedora/fedora-system:def/model#") ~ ("object" -> "info:fedora/easy-model:EDM1FILE"),
        ("predicate" -> "info:fedora/fedora-system:def/model#") ~ ("object" -> "info:fedora/dans-container-item-v1")
      ))
    writeToFile(new File(sdoDir.getPath, JSON_CFG_FILENAME), pretty(render(sdoCfg)))
  }

  def createDirCfg(dirName: String, parentSDO: String, sdoDir: File): Try[Unit] = {
    val sdoCfg =
      ("namespace" -> "easy-folder") ~
      ("relations" -> List(
        ("predicate" -> "http://dans.knaw.nl/ontologies/relations#:isMemberOf") ~ ("objectSDO" -> parentSDO),
        ("predicate" -> "http://dans.knaw.nl/ontologies/relations#:isSubordinateTo") ~ ("objectSDO" -> DATASET_SDO),
        ("predicate" -> "info:fedora/fedora-system:def/model#hasModel") ~ ("object" -> "info:fedora/easy-model:EDM1FOLDER"),
        ("predicate" -> "info:fedora/fedora-system:def/model#hasModel") ~ ("object" -> "info:fedora/dans-container-item-v1")
      ))
    writeToFile(new File(sdoDir.getPath, JSON_CFG_FILENAME), pretty(render(sdoCfg)))
  }

}