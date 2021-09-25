package invoice

sealed trait Basic
object Basic {
  final case class Numero(value: String)  extends Basic
  final case class Code(value: String)    extends Basic
  final case class Created(value: String) extends Basic

  implicit def cNumero[A >: Numero: Rect.Cover]: Capture[Numero] =
    Capture.of[Numero, A] { case ur"\D+(\d{8})\D+$v" => Numero(v) }

  implicit def cCode[A >: Code: Rect.Cover]: Capture[Code] =
    Capture.of[Code, A] { case ur"\D+(\d{12})\D+$v" => Code(v) }

  implicit def cCreated[A >: Created: Rect.Cover]: Capture[Created] =
    Capture.of[Created, A] { case ur"\D+(\d{4})$y\D+(\d)$m0\W*(\d)$m1\D+(\d)$d0\W*(\d)$d1" => Created(s"$y-$m0$m1-$d0$d1") }

  object rect {
    implicit val defaultRectBasic: Rect.Cover[Basic] = Rect.Cover(0, 400, 200, 85)
  }
}
