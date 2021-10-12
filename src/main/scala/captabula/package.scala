import scala.util.matching.UnanchoredRegex

import cats.data.Reader

package object captabula {

  type Capture[A, B] = A => Reader[B, String]

  trait Unmarshaller[F[_], A] {
    def read[B](implicit f: A => B): F[B]
  }

  trait Marshaller[A] {
    def write[B](b: B)(implicit f: B => A): Unit
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

    def rect[A](top: Number, left: Number, width: Number, height: Number)(implicit c: Capture[Rect, A]): Reader[A, String] =
      c(DefaultRect(top, left, width, height))

    def cell[A](row: Int, column: Int)(implicit c: Capture[Cell, A]): Reader[A, String] =
      c(DefaultCell(row, column))

    implicit class UnanchoredRegexString(val sc: StringContext) extends AnyVal {
      def regex: UnanchoredRegex = sc.parts.mkString.r.unanchored
    }

    implicit class ResourceSyntax[A](val a: A) extends AnyVal {
      def as[F[_], B](implicit f: A => Unmarshaller[F, B]): Unmarshaller[F, B] = f(a)
      def as[B](implicit f: A => Marshaller[B]): Marshaller[B]                 = f(a)
    }

  }

}
