package invoice

trait Rect {
  def top: Int
  def left: Int
  def width: Int
  def height: Int
}
object Rect {
  final case class Cover[-A](top: Int, left: Int, width: Int, height: Int) extends Rect

  def apply[A](implicit rect: Rect.Cover[A]): Rect = rect

}
