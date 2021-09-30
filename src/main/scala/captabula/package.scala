import scala.util.matching.UnanchoredRegex
import cats.data._

package object captabula {
  implicit class UnanchoredRegexString(val sc: StringContext) extends AnyVal {
    def regex: UnanchoredRegex = sc.parts.mkString.r.unanchored
  }

  type ReaderFactory[A, B] = A => Reader[B, String]

  private[captabula] case class Rect(top: Number, left: Number, width: Number, height: Number)
  private[captabula] case class Sheet(row: Int, column: Int)

  object dsl {
    def rect[A](top: Number, left: Number, width: Number, height: Number)(implicit f: ReaderFactory[Rect, A]) = f(Rect(top, left, width, height))

    def sheet[A](row: Int, column: Int)(implicit f: ReaderFactory[Sheet, A]) = f(Sheet(row, column))
  }

}
