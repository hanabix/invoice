package invoice

import org.scalatest.wordspec.AnyWordSpec
import Basic._
import Total._

class E2ESpec extends AnyWordSpec {
  "From a pdf" when {
    "read from a file" should {
      "capture record" in {
        val in = classOf[E2ESpec].getClassLoader.getResourceAsStream("test.pdf")

        implicit val basicRect: Rect.Cover[Basic] = Rect.Cover(70, 70, 140, 80)
        implicit val totalRect: Rect.Cover[Total] = Rect.Cover(180, 70, 250, 50)
        val r                                     = tabula.read[Record](in)
        assert(r == Right(Record(Code("111111111111"), Numero("11111111"), Created("2011-11-11"), PriceWithTax(29659.50))))
      }
    }
  }
}

case class Record(co: Code, no: Numero, ct: Created, pwt: PriceWithTax)
