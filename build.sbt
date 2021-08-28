lazy val root = (project in file(".")).settings(
  name         := "invoice",
  organization := "com.github.zhongl",
  homepage     := Some(url("https://github.com/hanabix/invoice")),
  licenses := List(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
  ),
  developers := List(
    Developer(
      "zhongl",
      "Lunfu Zhong",
      "zhong.lunfu@gmail.com",
      url("https://github.com/zhongl")
    )
  ),
  scalaVersion      := "2.13.5",
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  scalafmtOnCompile := true,
  scalacOptions += "-deprecation",
  scalacOptions += "-Wunused",
  resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",
  libraryDependencies ++= Seq(
    "technology.tabula" % "tabula"             % "1.0.5",
    "org.apache.poi"    % "poi-ooxml"     % "5.0.0",
    "com.lihaoyi"      %% "os-lib"             % "0.7.8",
    "com.lihaoyi"      %% "mainargs"           % "0.2.1",
    "org.typelevel"    %% "cats-core"          % "2.6.1",
    "org.scalactic"    %% "scalactic"          % "3.2.9",
    "org.scalatest"    %% "scalatest-wordspec" % "3.2.9" % Test,
    "org.scalamock"    %% "scalamock"          % "5.1.0" % Test
  )
)
