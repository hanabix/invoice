package invoice

import scala.reflect.runtime.universe._
import shapeless._

trait Capture[A] {
  def from(focus: Rect => String): ErrorOr[A]
}
object Capture {
  implicit val hnilCapture: Capture[HNil] = _ => Right(HNil)

  implicit def hlistCapture[H, T <: HList](implicit
      hCapture: Capture[H],
      tCapture: Capture[T]
  ): Capture[H :: T] = f =>
    for {
      h <- hCapture.from(f)
      t <- tCapture.from(f)
    } yield h :: t

  implicit def genericCapture[A, R](implicit
      gen: Generic.Aux[A, R],
      capture: Capture[R]
  ): Capture[A] = f => for (r <- capture.from(f)) yield gen.from(r)

  def of[A: TypeTag, B: Rect.Cover](pf: PartialFunction[String, A]): Capture[A] =
    focus => pf.unapply(focus(Rect[B])).toRight(s"missing ${typeOf[A]}")

  def apply[A](implicit c: Capture[A]): Capture[A] = c
}
