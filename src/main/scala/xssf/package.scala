import cats._
import cats.data.Reader

import shapeless._
import ops.record._

import org.apache.poi.xssf.usermodel._

package object xssf {
  type Append[A, B]         = A => Reader[B, Unit]
  type AppendRow[A]         = Append[A, XSSFRow]
  type AppendSheet[F[_], A] = Append[F[A], XSSFSheet]

  def appendCell[A](f: A => XSSFCell => Unit): AppendRow[A] = a => Reader { row => f(a)(row.createCell(math.max(0, row.getLastCellNum))) }

  implicit val appendHNil: AppendRow[HNil]       = _ => Reader { _ => }
  implicit val appendString: AppendRow[String]   = appendCell { v => c => c.setCellValue(v) }
  implicit val appendBoolean: AppendRow[Boolean] = appendCell { v => c => c.setCellValue(v) }
  implicit val appendDouble: AppendRow[Double]   = appendCell { v => c => c.setCellValue(v) }
  implicit val appendSymbol: AppendRow[Symbol]   = appendCell { v => c => c.setCellValue(v.name) }

  implicit def appendHList[H, T <: HList](implicit
      appendHCell: Lazy[AppendRow[H]],
      appendTCell: AppendRow[T]
  ): AppendRow[H :: T] = { case h :: t =>
    for {
      _ <- appendHCell.value.apply(h)
      _ <- appendTCell.apply(t)
    } yield ()
  }

  implicit def appendRow[A, R](implicit
      gen: Generic.Aux[A, R],
      appendR: AppendRow[R]
  ): AppendRow[A] = a => appendR.apply(gen.to(a))

  trait Header[A]

  implicit def appendHeader[A, R <: HList, K <: HList](implicit
      gen: LabelledGeneric.Aux[A, R],
      keys: Keys.Aux[R, K],
      appendK: AppendRow[K]
  ): AppendRow[Header[A]] = _ => appendK.apply(keys())

  implicit def appendMonad[F[_]: Traverse, A: AppendRow]: AppendSheet[F, A] = fa => {
    import cats.syntax.traverse._

    val readers = Functor[F].map(fa) { a =>
      val reader                                   = implicitly[AppendRow[A]].apply(a)
      val createRow: XSSFSheet => cats.Id[XSSFRow] = s => s.createRow(math.max(0, s.getLastRowNum))
      reader.compose[XSSFSheet, XSSFRow](createRow)
    }
    readers.sequence.map(_ => ())
  }

}
