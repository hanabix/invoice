import java.io._
import scala.util.matching.UnanchoredRegex

package object invoice {
  type ErrorOr[A] = Either[String, A]

  implicit class UnanchoredRegexString(private val sc: StringContext) extends AnyVal {
    def ur: UnanchoredRegex = sc.parts.mkString.r.unanchored
  }

  def using[A <: Closeable, B](a: A)(func: A => B): B = {
    try func(a)
    finally a.close()
  }
}
