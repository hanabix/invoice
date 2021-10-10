import scala.util.matching.UnanchoredRegex

import cats.data.Reader

package object captabula {

  type Capture[A, B] = A => B

  trait DataFormatReader[F[_], A] {
    type Decoder[T]

    def read[B](implicit f: Decoder[B]): F[B]
  }

  object DataFormatReader {
    type Aux[F[_], G[_], A] = DataFormatReader[F, A] { type Decoder[T] = G[T] }
  }

  trait DataFormatWriter[A] {
    type Encoder[T]

    def write[B](b: B)(implicit e: Encoder[B]): Unit
  }

  trait Rect {
    def top: Number
    def left: Number
    def width: Number
    def height: Number
  }

  trait Cell {
    def row: Int
    def column: Int
  }

  object dsl {
    private case class DefaultRect(top: Number, left: Number, width: Number, height: Number) extends Rect
    private case class DefaultCell(row: Int, column: Int)                                    extends Cell

    def rect[A](top: Number, left: Number, width: Number, height: Number)(implicit c: Capture[Rect, A]): A =
      c(DefaultRect(top, left, width, height))

    def cell[A](row: Int, column: Int)(implicit c: Capture[Cell, A]): A =
      c(DefaultCell(row, column))

    implicit class UnanchoredRegexString(val sc: StringContext) extends AnyVal {
      def regex: UnanchoredRegex = sc.parts.mkString.r.unanchored
    }

    implicit class ResourceSyntax[A](val a: A) extends AnyVal {
      def asReader[F[_], G[_], B](implicit f: A => DataFormatReader.Aux[F, G, B]): DataFormatReader[F, B] = f(a)
      def asWriter[B](implicit f: A => DataFormatWriter[B]): DataFormatWriter[B]             = f(a)
    }

  }

}
