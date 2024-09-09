lazy val root = (project in file(".")).settings(
  name         := "captabula",
  organization := "com.github.zhongl",
  homepage     := Some(url("https://github.com/hanabix/captabula")),
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
  scalaVersion      := "2.13.12",
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  scalafmtOnCompile := true,
  scalacOptions += "-deprecation",
  scalacOptions += "-Wunused",
  resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",
  libraryDependencies ++= Seq(
    "technology.tabula" % "tabula"             % "1.0.5",
    "org.typelevel"    %% "cats-core"          % "2.10.0",
    "com.chuusai"      %% "shapeless"          % "2.3.10",
    "org.apache.poi"    % "poi-ooxml"          % "5.2.5",
    "org.scalactic"    %% "scalactic"          % "3.2.17",
    "org.scalatest"    %% "scalatest-wordspec" % "3.2.17" % Test,
    "org.scalamock"    %% "scalamock"          % "6.0.0"  % Test
  )
)
