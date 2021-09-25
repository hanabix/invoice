package invoice

sealed trait Total
object Total {
  final case class PriceWithTax(value: Double) extends Total

  implicit def cTotal[A >: PriceWithTax: Rect.Cover]: Capture[PriceWithTax] =
    Capture.of[PriceWithTax, A] { case ur"(\d+\.\d{2})$v" => PriceWithTax(v.toDouble) }

  object rect {
    implicit val defaultRectTotal: Rect.Cover[Total] = Rect.Cover(275, 400, 200, 25)
  }
}
