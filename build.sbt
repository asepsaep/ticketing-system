import sbt._
import Keys._
import sbt.Project.projectToRef
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

lazy val appVersion = "0.0.1"
lazy val scala = "2.11.8"

lazy val versions = new {
  val akka = "2.4.16"
  val activeMQ = "5.14.0"
  val camelJetty = "2.13.4"
  val playSlick = "2.0.2"
  val slick = "3.1.1"
  val slickPostgre = "0.14.3"
  val psqlJdbc = "9.4-1206-jdbc41"
  val silhouette = "4.0.0"
  val guice = "4.1.0"
  val ficus = "1.2.6"
  val playBootstrap = "1.1-P25-B4"
  val slf4j = "1.7.16"
  val logback = "1.1.3"
  val scalatest = "1.5.1"
}

lazy val commonSettings = Seq(
  version := appVersion,
  scalaVersion := scala,
  scalacOptions ++= scalaCompilerOptions,
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(FormatXml, false)
    .setPreference(DoubleIndentClassDeclaration, false)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(AlignParameters, false)
    .setPreference(CompactStringConcatenation, false)
    .setPreference(IndentPackageBlocks, true)
    .setPreference(PreserveSpaceBeforeArguments, false)
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
    .setPreference(SpaceBeforeColon, false)
    .setPreference(SpaceInsideBrackets, false)
    .setPreference(SpaceInsideParentheses, false)
    .setPreference(IndentSpaces, 2)
    .setPreference(IndentLocalDefs, false)
    .setPreference(SpacesWithinPatternBinders, true)
    .setPreference(SpacesAroundMultiImports, true),
  excludeFilter in scalariformFormat := (excludeFilter in scalariformFormat).value ||
    "Routes.scala" ||
    "ReverseRoutes.scala" ||
    "JavaScriptReverseRoutes.scala" ||
    "RoutesPrefix.scala"
)

lazy val TicketingSystem = (project in file("."))
  .settings(SbtScalariform.defaultScalariformSettings: _*)
  .settings(commonSettings: _*)
  .settings(
    name := "ticketing-system",
    fork in run := true,
    connectInput in run := true,
    javaOptions ++= jvmOptions,
    libraryDependencies ++= Seq(
      cache,
      ws,
      filters,
      "com.typesafe.akka" %% "akka-actor" % versions.akka,
      "com.typesafe.akka" %% "akka-camel" % versions.akka,
      "org.apache.camel" % "camel-jetty" % versions.camelJetty,
      "org.apache.activemq" % "activemq-camel" % versions.activeMQ,
      "com.typesafe.play" %% "play-slick" % versions.playSlick,
      "com.typesafe.play" %% "play-slick-evolutions" % versions.playSlick,
      "com.typesafe.slick" % "slick-hikaricp_2.11" % versions.slick,
      "com.github.tminglei" %% "slick-pg" % versions.slickPostgre,
      "com.github.tminglei" %% "slick-pg_date2" % versions.slickPostgre,
      "org.postgresql" % "postgresql" % versions.psqlJdbc,
      "com.mohiva" %% "play-silhouette" % versions.silhouette,
      "com.mohiva" %% "play-silhouette-password-bcrypt" % versions.silhouette,
      "com.mohiva" %% "play-silhouette-crypto-jca" % versions.silhouette,
      "com.mohiva" %% "play-silhouette-persistence" % versions.silhouette,
      "net.codingwell" %% "scala-guice" % versions.guice,
      "com.iheart" %% "ficus" % versions.ficus,
      "org.slf4j" % "slf4j-api" % versions.slf4j,
      "com.adrianhurt" % "play-bootstrap_2.11" % versions.playBootstrap,
      "ch.qos.logback" % "logback-classic" % versions.logback,
      "org.scalatestplus.play" %% "scalatestplus-play" % versions.scalatest % Test
    )
  ).enablePlugins(PlayScala).enablePlugins(SbtScalariform)

lazy val jvmOptions = Seq(
  "-Xms256M",
  "-Xmx2G",
  "-XX:MaxPermSize=2048M",
  "-XX:+UseConcMarkSweepGC",
  "-Dorg.apache.activemq.SERIALIZABLE_PACKAGES=*"
)

lazy val scalaCompilerOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)