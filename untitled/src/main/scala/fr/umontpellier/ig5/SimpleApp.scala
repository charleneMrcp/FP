package fr.umontpellier.ig5

import com.mongodb.client.{MongoClients, MongoCollection, MongoDatabase}
import java.io._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.bson.Document
import scala.jdk.CollectionConverters._

object SimpleApp {
  implicit val formats: Formats = DefaultFormats

  case class CVE(
                  ID: String,
                  Description: String,
                  baseScore: Double,
                  baseSeverity: String,
                  exploitabilityScore: Option[Double],
                  impactScore: Option[Double]
                )


  def main(args: Array[String]): Unit = {
    val filesPath = "src/main/json_files" // Chemin du dossier contenant les fichiers JSON
    val outputFile = "./filtered_cves.json"
    val mongoUri = "mongodb+srv://test:test@cluster0.io45k.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
    val databaseName = "CVE_Database"
    val collectionName = "CVE_Collection"

    val directory = new File(filesPath)
    if (!directory.exists || !directory.isDirectory) {
      println(s"Le chemin $filesPath n'est pas un dossier valide.")
      return
    }

    // Lire et traiter les fichiers JSON
    val jsonFiles = directory.listFiles.filter(_.getName.endsWith(".json"))
    val cveList = jsonFiles.flatMap { file =>
      println(s"Lecture du fichier JSON : ${file.getName}")
      val json = parse(new FileReader(file))
      (json \ "CVE_Items").extract[List[JValue]].flatMap { item =>
        val cve = (item \ "cve")
        val id = (cve \ "CVE_data_meta" \ "ID").extract[String]
        val description = (cve \ "description" \ "description_data")(0) \ "value"
        val impact = item \ "impact" \ "baseMetricV3"
        if (impact != JNothing) {
          val baseScore = (impact \ "cvssV3" \ "baseScore").extract[Double]
          val baseSeverity = (impact \ "cvssV3" \ "baseSeverity").extract[String]
          val exploitabilityScore = (impact \ "exploitabilityScore").extractOpt[Double]
          val impactScore = (impact \ "impactScore").extractOpt[Double]

          Some(CVE(id, description.extract[String], baseScore, baseSeverity, exploitabilityScore, impactScore))
        } else None
      }
    }

    // Enregistrer dans un fichier JSON
    val writer = new PrintWriter(new File(outputFile))
    writer.write(pretty(render(Extraction.decompose(cveList))))
    writer.close()
    println(s"Les CVEs filtrés ont été sauvegardés dans $outputFile")

    // Sauvegarder dans MongoDB
    val client = MongoClients.create(mongoUri)
    val database: MongoDatabase = client.getDatabase(databaseName)
    val collection: MongoCollection[Document] = database.getCollection(collectionName)
    cveList.foreach { cve =>
      val document = new Document()
        .append("ID", cve.ID)
        .append("Description", cve.Description)
        .append("baseScore", cve.baseScore)
        .append("baseSeverity", cve.baseSeverity)
        .append("exploitabilityScore", cve.exploitabilityScore.orNull)
        .append("impactScore", cve.impactScore.orNull)
      collection.insertOne(document)
    }
    println("Les CVEs ont été sauvegardés dans MongoDB.")
  }
}