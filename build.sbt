organization in ThisBuild := "com.livelygig"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  Resolver.jcenterRepo,
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("denigma", "denigma-releases")
)

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    //    libraryDependencies ++= Settings.sharedDependencies.value
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js
// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")


// instantiate the JS project for SBT with some additional settings
lazy val clientJSDeps = List("prolog_parser.js", "validator.js")
lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Versions.appVersion,
    scalacOptions ++= Settings.scalacOptions,
    resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases"),
    libraryDependencies ++= Settings.scalajsDependencies.value,
    // by default we do development build, no eliding
    elideOptions := Seq(),
    scalacOptions ++= elideOptions.value,
    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
    jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    jsDependencies ++= clientJSDeps.map(ProvidedJS / _),
    // use Scala.js provided launcher code to start the client app
    persistLauncher := true,
    persistLauncher in Test := false
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJs)


// Client projects (just one in this case)
lazy val clients = Seq(client)

// web gateway with play server
lazy val webGateway = (project in file("web-gateway"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala && LagomPlay)
  .dependsOn(userApi, messageApi, security)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .dependsOn(sharedJvm)
  .settings(
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmDependencies.value,
    libraryDependencies ++= Seq(lagomScaladslServer,filters),
    commands += ReleaseCmd,
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    devCommands in scalaJSPipeline += "runAll",
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    includeFilter in(Assets, LessKeys.less) := "main.less",
    // compress CSS
    LessKeys.compress in Assets := true
  )


lazy val security = (project in file("security"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      "org.julienrf" %% "play-json-derived-codecs" % "3.3",
      "org.scalatest" %%% "scalatest" % Versions.scalaTest % "test"
    )
  )

lazy val userApi = (project in file("user-api"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      "org.julienrf" %% "play-json-derived-codecs" % "3.3",
      "be.objectify" %% "deadbolt-scala" % "2.5.1"
    )
  )

lazy val userImpl = (project in file("user-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomScala)
  .dependsOn(userApi, security)
  .settings(
    libraryDependencies ++= Settings.apiImplDependencies.value,
    libraryDependencies +=lagomScaladslPersistenceCassandra
  )

lazy val messageApi = (project in file("message-api"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val messageImpl = (project in file("message-impl"))
  .settings(commonSettings: _*)
  .enablePlugins(LagomScala)
  .dependsOn(messageApi, security)
  .settings(
    libraryDependencies ++= Settings.apiImplDependencies.value,
    libraryDependencies ++= Seq(lagomScaladslPersistenceCassandra, lagomScaladslPubSub)
  )


def commonSettings: Seq[Setting[_]] = Seq(
  version := Versions.appVersion,
  scalacOptions ++= Settings.scalacOptions
)

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "client/clean" ::
    "client/test" ::
    "webGateway/clean" ::
    "webGateway/test" ::
    "webGateway/dist" ::
    "set elideOptions in client := Seq()" ::
    state
}


lagomCassandraCleanOnStart in ThisBuild := false
onLoad in Global := (Command.process("project webGateway", _: State)) compose (onLoad in Global).value
