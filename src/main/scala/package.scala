import java.io.File

import scala.util.matching.Regex

package object invoice {
  type FileTo[A] = File => A
  object FileTo {
    def apply[A](implicit ft: FileTo[A]) = ft
  }

  final case class Capture(pattern: Regex, formatter: String = "%s") {
    def on(row: String): Option[String] = {
      pattern.unapplySeq(row).map(formatter.format(_: _*))
    }
  }

  final case class Area(top: Double, left: Double, bottom: Double, right: Double)
  final case class Selection(area: Area, fields: List[Field[Capture]])
  final case class Field[V](name: String, value: V)

  trait Extractor[F[_]] {
    def stringIn(s: Selection): F[Field[Option[String]]]
  }

  trait Extractors[F[_], A] {
    def of(a: A): Extractor[F]
  }

  object Extractors {
    def apply[F[_], A](implicit e: Extractors[F, A]) = e
  }

}
