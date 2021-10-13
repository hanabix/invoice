package captabula

import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

import scala.annotation.nowarn

import cats.Functor

import shapeless._
import shapeless.ops.record._

import org.apache.poi.xssf.usermodel._

object xssf {

  type Excel          = XSSFWorkbook => Unit
  type Append[A, B]   = A => B => Unit
  type AppendRow[A]   = Append[A, XSSFRow]
  type AppendSheet[A] = Append[A, XSSFSheet]

  trait Prehead

  def appendCell[A](f: XSSFCell => A => Unit): AppendRow[A] = a => r => f(r.createCell(math.max(0, r.getLastCellNum)))(a)

  implicit val appendHNil: AppendRow[HNil]       = _ => _ => ()
  implicit val appendString: AppendRow[String]   = appendCell { c => v => c.setCellValue(v) }
  implicit val appendBoolean: AppendRow[Boolean] = appendCell { c => v => c.setCellValue(v) }
  implicit val appendDouble: AppendRow[Double]   = appendCell { c => v => c.setCellValue(v) }
  implicit val appendSymbol: AppendRow[Symbol]   = appendCell { c => v => c.setCellValue(v.name) }

  implicit def appendHListRow[H, T <: HList](implicit
      appendHCell: Lazy[AppendRow[H]],
      appendTCell: AppendRow[T]
  ): AppendRow[H :: T] = {
    case h :: t => { r =>
      appendHCell.value(h)(r)
      appendTCell(t)(r)
    }
  }

  implicit def appendARow[A, R](implicit
      gen: Generic.Aux[A, R],
      f: AppendRow[R]
  ): AppendRow[A] = a => r => f(gen.to(a))(r)

  implicit def appendABook[F[_], A](implicit f: AppendSheet[F[A]]): F[A] => Excel = { fa => b => f(fa)(b.createSheet()) }

  implicit def appendAPreheadBook[F[_], A <: Prehead, R <: HList, K <: HList](implicit
      @nowarn gen: LabelledGeneric.Aux[A, R],
      keys: Keys.Aux[R, K],
      f: AppendSheet[F[A]],
      g: AppendRow[K]
  ): F[A] => Excel = { fa => b =>
    val s = b.createSheet()
    val r = s.createRow(0)
    g(keys())(r)
    f(fa)(s)
  }

  implicit def appendASheet[F[_]: Functor, A](implicit f: AppendRow[A]): AppendSheet[F[A]] = { fa => s =>
    Functor[F].map(fa)(a => f(a)(s.createRow(s.getLastRowNum + 1)))
  }

  implicit val pathMarshallerOfExcel: Path => Marshaller[Excel] = p =>
    new Marshaller[Excel] {
      def write[B](b: B)(implicit f: B => Excel): Unit = {
        using(new XSSFWorkbook()) { wb =>
          f(b)(wb)
          using(Files.newOutputStream(p))(wb.write)
        }
      }

      def using[A <: Closeable, B](a: A)(f: A => B): B =
        try f(a)
        finally a.close()

    }

}
