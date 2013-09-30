import sbt._
import Keys._
import com.typesafe.sbt.SbtAtmos.{Atmos, atmosSettings}
import com.github.retronym.SbtOneJar._


object IotaBuild extends Build {

  val ProjectName = "iota"
  val Version = "0.1-SNAPSHOT"
  val ScalaVersion = "2.10.2"

  lazy val root = Project(
    id = ProjectName,
    base = file("."),
    settings = shared
  ).aggregate(iotaAPI, iotaServer, directoryDriver)
   .settings(pomExtra := mavenExtraXML)

  lazy val iotaAPI = Project(
    id = "iota-api",
    base = file("iota-api"),
    settings = shared
  )

  lazy val iotaServer = Project(
    id = "iota-server",
    base = file("iota-server"),
    settings = shared ++ atmosSettings
  ).dependsOn(iotaAPI)
   .dependsOn(directoryDriver, echoDriver)
   .settings(
      libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % "1.0.13"
      ),
      mainClass in oneJar := Some("org.madnl.iota.IotaServer")
  ).configs(Atmos)

  lazy val directoryDriver = Project(
    id = "iota-directory",
    base = file("iota-directory"),
    settings = shared
  ).dependsOn(iotaAPI)
   .settings(libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.3")

  lazy val echoDriver = Project(
    id = "iota-echo",
    base = file("iota-echo"),
    settings = shared
  ).dependsOn(iotaAPI)

  def shared = Defaults.defaultSettings ++ Seq(
    scalaVersion := ScalaVersion,
    organization := "org.madnl",
    version := Version,
    scalacOptions += "-feature",
    libraryDependencies ++= commonDependencies,
    conflictWarning := ConflictWarning.disable, //fixes a bug in SBT which signals incompatibilities between 2.10.* versions
    exportJars := true
  ) ++ tools

  def tools =
    seq(org.scalastyle.sbt.ScalastylePlugin.Settings: _*) ++
      com.github.retronym.SbtOneJar.oneJarSettings

  def commonDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.google.guava" % "guava" % "14.0.1",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"
  )

  def mavenExtraXML =
    <pluginRepositories>
      <pluginRepository>
        <id>scala-tools.org</id>
        <name>Scala-tools Maven2 Repository</name>
        <url>http://scala-tools.org/repo-releases</url>
      </pluginRepository>
    </pluginRepositories>
      <reporting>
        <plugins>
          <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>cobertura-maven-plugin</artifactId>
            <version>2.6</version>
            <configuration>
              <formats>
                <format>html</format>
                <format>xml</format>
              </formats>
            </configuration>
          </plugin>
        </plugins>
      </reporting>
      <build>
        <sourceDirectory>src/main/scala</sourceDirectory>
        <testSourceDirectory>src/test/scala</testSourceDirectory>
        <plugins>
          <plugin>
            <groupId>org.scala-tools</groupId>
            <artifactId>maven-scala-plugin</artifactId>
            <version>2.15.2</version>
            <executions>
              <execution>
                <goals>
                  <goal>compile</goal>
                  <goal>testCompile</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.7</version>
            <configuration>
              <skipTests>true</skipTests>
            </configuration>
          </plugin>
          <plugin>
            <groupId>org.scalatest</groupId>
            <artifactId>scalatest-maven-plugin</artifactId>
            <version>1.0-M2</version>
            <configuration>
              <reportsDirectory>${{project.build.directory}}/surefire-reports</reportsDirectory>
              <junitxml>.</junitxml>
              <filereports>WDF TestSuite.txt</filereports>
            </configuration>
            <executions>
              <execution>
                <id>test</id>
                <goals>
                  <goal>test</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.scalastyle</groupId>
            <artifactId>scalastyle-maven-plugin</artifactId>
            <version>0.3.2</version>
            <configuration>
              <verbose>false</verbose>
              <failOnViolation>true</failOnViolation>
              <includeTestSourceDirectory>true</includeTestSourceDirectory>
              <failOnWarning>false</failOnWarning>
              <sourceDirectory>${{basedir}}/src/main/scala</sourceDirectory>
              <testSourceDirectory>${{basedir}}/src/test/scala</testSourceDirectory>
              <configLocation>${{basedir}}/lib/scalastyle_config.xml</configLocation>
              <outputFile>${{project.basedir}}/scalastyle-output.xml</outputFile>
              <outputEncoding>UTF-8</outputEncoding>
            </configuration>
            <executions>
              <execution>
                <goals>
                  <goal>check</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
}
