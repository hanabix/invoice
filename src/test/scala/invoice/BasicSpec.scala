package invoice

import org.scalatest.wordspec.AnyWordSpec
import Basic._

class BasicSpec extends AnyWordSpec {

  "Basic" when {
    "covered by default " should {
      "capture information" in {
        import Basic.rect._
        val focus: Rect => String = {
          case Rect.Cover(0, 400, 200, 85) => "发票代码:111111111111\n发票号码:11111111\n开票日期:2011年11月11日 "
          case _                           => "unknown"
        }
        val bi = Capture[BasicInfo].from(focus)
        assert(bi == Right(BasicInfo(Numero("11111111"), Code("111111111111"), Created("2011-11-11"))))
      }
    }

    "covered by customizing" should {
      "capture information" in {
        implicit val r: Rect.Cover[Basic] = Rect.Cover(1, 2, 3, 4)
        val focus: Rect => String = {
          case Rect.Cover(1, 2, 3, 4) => "发票代码:111111111111\n发票号码:11111111\n开票日期:2011年11月11日 "
          case _                      => "unknown"
        }
        val bi = Capture[BasicInfo].from(focus)
        assert(bi == Right(BasicInfo(Numero("11111111"), Code("111111111111"), Created("2011-11-11"))))
      }

    }
  }

}

case class BasicInfo(no: Numero, code: Code, created: Created)
