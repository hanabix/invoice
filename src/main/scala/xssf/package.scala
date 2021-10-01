import shapeless._
import shapeless.ops.record._

import org.apache.poi.xssf.usermodel._

package object xssf {
  type AppendRow[A] = XSSFRow => A => Unit

  def appendCell[A](f: XSSFCell => A => Unit): AppendRow[A] = r => a => f(r.createCell(math.max(0, r.getLastCellNum)))(a)

  implicit val appendHNil: AppendRow[HNil]       = _ => _ => ()
  implicit val appendString: AppendRow[String]   = appendCell { c => v => c.setCellValue(v) }
  implicit val appendBoolean: AppendRow[Boolean] = appendCell { c => v => c.setCellValue(v) }
  implicit val appendDouble: AppendRow[Double]   = appendCell { c => v => c.setCellValue(v) }
  implicit val appendSymbol: AppendRow[Symbol]   = appendCell { c => v => c.setCellValue(v.name) }

  implicit def appendHList[H, T <: HList](implicit
      appendHCell: Lazy[AppendRow[H]],
      appendTCell: AppendRow[T]
  ): AppendRow[H :: T] = r => { case h :: t =>
    appendHCell.value(r)(h)
    appendTCell(r)(t)
  }

  implicit def appendRow[A, R](implicit
      gen: Generic.Aux[A, R],
      appendR: AppendRow[R]
  ): AppendRow[A] = r => a => appendR(r)(gen.to(a))

  trait Header[A]

  implicit def appendHeader[A, R <: HList, K <: HList](implicit
      gen: LabelledGeneric.Aux[A, R],
      keys: Keys.Aux[R, K],
      appendK: AppendRow[K]
  ): AppendRow[Header[A]] = r => _ => appendK(r)(keys())

  implicit class SheetOps(val sheet: XSSFSheet) extends AnyVal {
    def appendHeader[A](implicit ap: AppendRow[Header[A]]): Unit = {
      ap(row)(new Header[A] {})
    }

    def appendRow[A](a: A)(implicit ap: AppendRow[A]): Unit = ap(row)(a)

    private def row = sheet.createRow(sheet.getLastRowNum + 1)
  }
}
