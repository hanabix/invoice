package captabula

import cats.instances.list._

import captabula.xssf._

import org.scalatest.wordspec.AnyWordSpec

class XssfSpec extends AnyWordSpec {

  "A book" when {
    "append a list of records" should {
      "implicit a instance for List[Record1]" in {
        implicitly[List[Record1] => Excel]
      }
      "implicit a instance for List[Record2]" in {
        implicitly[List[Record2] => Excel]
      }
    }
  }
}

case class Record1(field: String)
case class Record2(field: String) extends Prehead
