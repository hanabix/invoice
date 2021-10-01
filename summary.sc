
import $ivy.`com.github.zhongl::captabula:0.0.4`, captabula._, tabula._, dsl._, xssf._

import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.io.Closeable

case class Invoice(`发票代码`: String, `发票号码`: String, `开票日期`: String, `价税合计`: Double)

def using[A <: Closeable, B](a: A)(f: A => B): B = try f(a)
finally a.close()

@main
def main(path: os.Path = os.pwd) = {
  def filter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(".pdf") }
  val f      = path.toIO
  val isFile = f.isFile()
  val files  = if (isFile) List(f) else f.listFiles(filter).toList

  val transform = for {
    capture <- load[File](1)
    regex"(\d{12})$no\D+(\d{8})$code\D+(\d{4})$year\D*(\d{2})$month\D*(\d{2})$day" = capture.rect(0, 400, 200, 85).replaceAll("\\s", "")
    regex"(\d+\.\d{2})$pwt"                                                        = capture.rect(275, 400, 200, 25)
  } yield Invoice(no, code, s"$year-$month-$day", pwt.toDouble)

  using(new XSSFWorkbook()) { book =>
    val sheet = book.createSheet()
    sheet.appendHeader[Invoice]

    val sum = files.foldLeft(0.0d) { case (sum, f) =>
      val i = transform.run(f).head
      val p = f.toPath();
      sheet.appendRow(i)
      Files.move(p, p.resolveSibling(f"${i.`发票代码`}-${i.`发票号码`}-${i.`价税合计`}%.2f.pdf"))
      sum + i.`价税合计`
    }

    val name = f"summary-$sum%.2f.xlsx"

    using(Files.newOutputStream(if (isFile) path.toNIO.resolveSibling(name) else path.toNIO.resolve(name))) {
      book.write(_)
    }
  }

}
