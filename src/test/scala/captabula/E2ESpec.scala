package captabula

import java.io.InputStream

import captabula.dsl._
import captabula.tabula._

import org.scalatest.wordspec.AnyWordSpec

class E2ESpec extends AnyWordSpec {
  "Capture invoice" when {
    "read from a pdf" should {
      "by rect" in {
        val in = classOf[E2ESpec].getClassLoader.getResourceAsStream("test.pdf")
        val reader: Transform[InputStream, Invoice] = for {
          capture <- load[InputStream](1)
          regex"(\d{12})$no\D+(\d{8})$code\D+(\d{4})$year\D+(\d{2})$month\D+(\d{2})$day" = capture.rect(70, 70, 140, 80)
        } yield Invoice(no, code, s"$year-$month-$day")
        assert(reader.run(in) == List(Invoice("111111111111", "11111111", "2011-11-11")))
      }
    }
  }

  case class Invoice(no: String, code: String, created: String)
}
