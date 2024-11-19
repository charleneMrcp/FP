ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

// Nom du projet et package pour IntelliJ IDEA
lazy val root = (project in file("."))
  .settings(
    name := "projet scala",
    idePackagePrefix := Some("fr.umontpellier.ig5")
  )

// Dépendances
libraryDependencies ++= Seq(
  // Apache Spark
  "org.apache.spark" %% "spark-core" % "3.5.3",
  "org.apache.spark" %% "spark-sql" % "3.5.3",
  "org.apache.spark" %% "spark-hive" % "3.5.3",

  // MongoDB Scala Driver
  "org.mongodb.scala" %% "mongo-scala-driver" % "5.2.0",

  // Neo4j Connector for Apache Spark
  "org.neo4j" %% "neo4j-connector-apache-spark" % "5.3.1_for_spark_3"
)

// Options pour Spark et compatibilité avec Hadoop (si nécessaire)
fork := true
