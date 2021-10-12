package captabula

import captabula.dsl._
import captabula.tabula._

import org.scalatest.wordspec.AnyWordSpec
import technology.tabula.Page

class E2ESpec extends AnyWordSpec {
  "Capture invoice" when {
    "read from a pdf" should {
      "by rect" in {

        implicit val capture: Page => Invoice = (for {
          text <- rect(70, 70, 140, 80)
          regex"(\d{12})$no\D+(\d{8})$code\D+(\d{4})$year\D+(\d{2})$month\D+(\d{2})$day" = text
        } yield Invoice(no, code, s"$year-$month-$day")).run

        val in      = classOf[E2ESpec].getClassLoader.getResourceAsStream("test.pdf")
        val invoice = in.as[List, Page].read[Invoice].take(1)

        assert(invoice == List(Invoice("111111111111", "11111111", "2011-11-11")))
      }
    }
  }

  case class Invoice(no: String, code: String, created: String)
}
