package invoice

import org.scalatest.wordspec.AnyWordSpec
import Total._
import rect._

class TotalSpec extends AnyWordSpec {
  "Total" when {
    "covered by default" should {
      "capture price with tax" in {
        val focus: Rect => String = { case _ => "价税合计(大写)\n 贰万玖仟陆佰伍拾玖圆伍角整 (小写)¥29659.50 " }
        val pwt                   = Capture[PriceWithTax].from(focus)
        assert(pwt == Right(PriceWithTax(29659.50)))
      }
    }
  }
}
