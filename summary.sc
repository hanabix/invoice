// import $ivy.`com.github.zhongl::invoice:0.0.4`

import $ivy.`org.apache.poi:poi-ooxml:5.0.0`, org.apache.poi.xssf.usermodel.XSSFWorkbook



import $ivy.`com.github.zhongl::captabula:0.0.3+3-79e78cb3+20211001-0005-SNAPSHOT`
import java.io.File
import java.io.FilenameFilter

import cats.data.Reader

import captabula._
import tabula._
import dsl._


case class Invoice(no: String, code: String, created: String, pwt: Double)




@main
def main(path: os.Path = os.pwd) = {
  def filter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(".pdf") }
  val f = path.toIO
  val files = if(f.isFile()) List(f) else f.listFiles(filter).toList

  val reader: Reader[File, Invoice] = for {
    basic <- rect[File](top = 0, left = 400, width = 200, height = 85)
    regex"(\d{12})$no\D+(\d{8})$code\D+(\d{4})$year\D+(\d)$m0\D*(\d)$m1\D+(\d)$d0\D*(\d)$d1" = basic
    total <- sheet[File](row = 2, column = 1)
    regex"(\d+\.\d{2})$pwt" = total
  } yield Invoice(no, code, s"$year-$m0$m1-$d0$d1", pwt.toDouble)

  files.map(reader.run).foreach(println)
  // using(new XSSFWorkbook()) { b =>
  //   val sheet = b.createSheet()
  //   val header = sheet.createRow(0)

  //   for ( (s, i) <- List("发票代码", "发票号码", "开票日期","价税合计").zipWithIndex ) {
  //     header.createCell(i).setCellValue(s)
  //   }

  //   records.zipWithIndex.foreach {
  //     case (r, i) =>
  //       val row = sheet.createRow(i + 1)
  //       row.createCell(0).setCellValue(r.c.value)
  //       row.createCell(1).setCellValue(r.n.value)
  //       row.createCell(2).setCellValue(r.t.value)
  //       row.createCell(3).setCellValue(r.p.value)
  //   }

  //   using(new FileOutputStream((path / f"summary_$total%.2f.xlsx").toIO))(b.write)
  // }
}

