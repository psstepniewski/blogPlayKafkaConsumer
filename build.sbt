name := "blogPlayKafkaConsumer"
 
version := "1.0" 
      
lazy val `blogplaykafkaconsumer` = (project in file(".")).enablePlugins(PlayScala)

      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.5"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.0.0"