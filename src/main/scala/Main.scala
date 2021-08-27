package invoice

import java.io._

import org.apache.pdfbox.pdmodel.PDDocument

import invoice.tabula._
import mainargs._

object Main {

  implicit object PathRead extends TokensReader[os.Path]("path", strs => Right(os.Path(strs.head, os.pwd)))

  @main
  def run(path: os.Path = os.pwd) = {
    val selections = List(
      Selection(
        Area(0, 400, 85, 600),
        List(
          Field("发票号码", Capture("发票号码\\D+(\\d+)".r)),
          Field("开票日期", Capture("开票日期\\D+(\\d{4})\\D*(\\d{2})\\D*(\\d{2})\\D*".r, "%s-%s-%s"))
        )
      ),
      Selection(Area(270, 443, 295, 600), List(Field("总计金额", Capture("\\D*(\\d+\\.\\d{2})\\D*".r))))
    )

    val rows = pdf[(File, PDDocument)](path).map { case (f, d) =>
      using(d) { _ =>
        val e = Extractors[List, PDDocument].of(d)
        selections.map(e.stringIn).flatten.appended(Field("文件名", Option(f.getName)))
      }
    }

    rows.zipWithIndex.foreach {
      case (fs, 0) => println(fs.map(_.name).mkString("\t"))
      case (fs, _) => println(fs.map(_.value.getOrElse("N/A")).mkString("\t"))
    }
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)

  private def using[A <: Closeable, B](a: A)(func: A => B): B = {
    try func(a)
    finally a.close()
  }

  private def pdf[A](path: os.Path)(implicit ft: FileTo[A]): List[A] = {
    def filter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(".pdf") }
    val f      = path.toIO
    val fs     = if (f.isFile()) List(f) else f.listFiles(filter).toList
    fs.map(ft)
  }

}
