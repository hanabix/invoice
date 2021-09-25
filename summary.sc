// import $ivy.`com.github.zhongl::invoice:0.0.4`
import $ivy.`com.github.zhongl::invoice:0.0.3+3-444549eb+20210925-2126-SNAPSHOT`
import invoice._
import Basic._
import Basic.rect._
import Total._
import Total.rect._

import $ivy.`org.apache.poi:poi-ooxml:5.0.0`, org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.io._

@main
def main(path: os.Path = os.pwd) = {
  def filter = new FilenameFilter { def accept(dir: File, name: String) = name.endsWith(".pdf") }
  val f = path.toIO
  val files = if(f.isFile()) List(f) else f.listFiles(filter).toList

  val (total, records) = files.foldLeft((0.0, List.empty[Record])) { 
    case ((sum, records), f) =>    
      using(new FileInputStream(f)) { io => tabula.read[Record](io) } match {
        case Left(msg) => 
          scala.Console.err.println(s"$f >>> $msg")
          (sum, records)
        case Right(r)  =>
          f.renameTo((path / s"${r.c.value}_${r.n.value}_${r.p.value}.pdf").toIO)
          (r.p.value + sum, r :: records)
      }
  } 

  using(new XSSFWorkbook()) { b =>
    val sheet = b.createSheet()
    val header = sheet.createRow(0)

    for ( (s, i) <- List("发票代码", "发票号码", "开票日期","价税合计").zipWithIndex ) {
      header.createCell(i).setCellValue(s)
    }

    records.zipWithIndex.foreach {
      case (r, i) =>
        val row = sheet.createRow(i + 1)
        row.createCell(0).setCellValue(r.c.value)
        row.createCell(1).setCellValue(r.n.value)
        row.createCell(2).setCellValue(r.t.value)
        row.createCell(3).setCellValue(r.p.value)
    }

    using(new FileOutputStream((path / f"summary_$total%.2f.xlsx").toIO))(b.write)
  }
}

case class Record(c: Code, n: Numero, t: Created, p: PriceWithTax)
