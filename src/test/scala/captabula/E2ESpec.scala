package captabula


import captabula.dsl._
import captabula.tabula._

import cats.data.Reader

import org.scalatest.wordspec.AnyWordSpec
import technology.tabula.Page

class E2ESpec extends AnyWordSpec {
  "Capture invoice" when {
    "read from a pdf" should {
      "by rect" in {
        type D[A] = Reader[A, String]

        val in = classOf[E2ESpec].getClassLoader.getResourceAsStream("test.pdf")
        implicit val capture: Reader[Page, Invoice] = for {
          text <- rect[Reader[Page, String]](70, 70, 140, 80)
          regex"(\d{12})$no\D+(\d{8})$code\D+(\d{4})$year\D+(\d{2})$month\D+(\d{2})$day" = text
        } yield Invoice(no, code, s"$year-$month-$day")
        
        val invoice = in.asReader[List, D, Page].read(capture)

        assert(invoice == List(Invoice("111111111111", "11111111", "2011-11-11")))
      }
    }
  }

  case class Invoice(no: String, code: String, created: String)
}
