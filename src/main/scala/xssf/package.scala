import cats._
import cats.data.Reader

import shapeless._

import org.apache.poi.xssf.usermodel._

package object xssf {
  type Append[A, B]         = A => Reader[B, Unit]
  type AppendRow[A]         = Append[A, XSSFRow]
  type AppendSheet[F[_], A] = Append[F[A], XSSFSheet]

  def appendRow[A](f: A => XSSFCell => Unit): AppendRow[A] = a => Reader { row => f(a)(row.createCell(math.max(0, row.getLastCellNum))) }

  implicit val appendHNil: AppendRow[HNil]       = _ => Reader { _ => }
  implicit val appendString: AppendRow[String]   = appendRow { v => c => c.setCellValue(v) }
  implicit val appendBoolean: AppendRow[Boolean] = appendRow { v => c => c.setCellValue(v) }
  implicit val appendDouble: AppendRow[Double]   = appendRow { v => c => c.setCellValue(v) }

  implicit def appendHList[H, T <: HList](implicit
      appendHCell: Lazy[AppendRow[H]],
      appendTCell: AppendRow[T]
  ): AppendRow[H :: T] = { case h :: t =>
    for {
      _ <- appendHCell.value.apply(h)
      _ <- appendTCell.apply(t)
    } yield ()
  }

  implicit def appendA[A, R](implicit
      gen: Generic.Aux[A, R],
      appendR: AppendRow[R]
  ): AppendRow[A] = a => appendR.apply(gen.to(a))

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
