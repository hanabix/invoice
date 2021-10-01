// import $ivy.`com.github.zhongl::invoice:0.0.4`

import java.io.File
import java.io.FilenameFilter

import $ivy.`com.github.zhongl::captabula:0.0.3+8-34829a16+20211001-1221-SNAPSHOT`, cats.data.Reader, captabula._, tabula._, dsl._

import $ivy.`org.apache.poi:poi-ooxml:5.0.0`, org.apache.poi.xssf.usermodel.XSSFWorkbook
import $ivy.`com.chuusai::shapeless:2.3.3`
import java.nio.file.Files

case class Invoice(`发票代码`: String, `发票号码`: String, `开票日期`: String, `价税合计`: Double)

@main
def main(path: os.Path = os.pwd) = {
  def filter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(".pdf") }
  val f      = path.toIO
  val files  = if (f.isFile()) List(f) else f.listFiles(filter).toList

  val transform = for {
    capture <- load[File](1)
    regex"(\d{12})$no\D+(\d{8})$code\D+(\d{4})$year\D+(\d{2})$month\D+(\d{2})$day" = capture.rect(0, 400, 200, 85)
    regex"(\d+\.\d{2})$pwt"                                                        = capture.sheet(2, 1)
  } yield Invoice(no, code, s"$year-$month-$day", pwt.toDouble)

  val (total, invoices) = files.foldLeft((0.0d, List.empty[Invoice])) { case ((sum, invoices), f) =>
    val i = transform.run(f).head
    val p = f.toPath();
    Files.move(p, p.resolveSibling(s"${i.`发票代码`}-${i.`发票号码`}-${i.`价税合计`}%2f"))
    (sum + i.`价税合计`, i :: invoices)
  }
  
}
