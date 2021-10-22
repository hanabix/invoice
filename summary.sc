import $ivy.`com.github.zhongl::captabula:0.0.7`, captabula._, tabula._, dsl._, xssf._

import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.Path

import cats.Id

import technology.tabula.Page

@main
def main(dir: os.Path = os.pwd) = {
  sys.props.addOne("org.slf4j.simpleLogger.defaultLogLevel" -> "error")

  val invoices = list(dir, ".pdf").map { f =>
    print(f)
    val i = f.as[Id, Page].read[Invoice]
    println(s" -> $i")
    rename(f.toPath(), f"${i.`发票代码`}-${i.`发票号码`}-${i.`价税合计`}%.2f.pdf")
    i
  }

  if (invoices.nonEmpty) {
    val sum = invoices.map(_.`价税合计`).sum
    (dir / f"summary-$sum%.2f.xlsx").toNIO.as[Excel].write(invoices)
  }
}

case class Invoice(`发票代码`: String, `发票号码`: String, `开票日期`: String, `价税合计`: Double) extends Prehead

implicit val captureInvoice: Page => Invoice = (for {
  basic <- rect(0, 400, 200, 85)
  total <- rect(275, 400, 200, 25)
  regex"(\d{12})$code\D+(\d{8})$no\D+(\d{4})$year\D*(\d{2})$month\D*(\d{2})$day" = basic.replaceAll("\\s", "")
  regex"(\d+\.\d{2})$pwt"                                                        = total
} yield Invoice(code, no, s"$year-$month-$day", pwt.toDouble)).run

def list(dir: os.Path, suffix: String): List[File] = {
  def filter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(suffix) }
  val f      = dir.toIO
  if (f.isFile()) List.empty else f.listFiles(filter).toList
}

def rename(p: Path, name: String): Unit = {
  Files.move(p, p.resolveSibling(name))
}
