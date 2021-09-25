package invoice

import java.io.InputStream

import scala.jdk.CollectionConverters._

import technology.tabula._
import technology.tabula.extractors.BasicExtractionAlgorithm

import org.apache.pdfbox.pdmodel._

object tabula {

  def read[A: Capture](in: InputStream): ErrorOr[A] = {
    def focus(page: Page): Rect => String = r => {
      val t      = r.top.toFloat
      val l      = r.left.toFloat
      val w      = r.width.toFloat
      val h      = r.height.toFloat
      val sub    = page.getArea(new Rectangle(t, l, w, h))
      val tables = new BasicExtractionAlgorithm(List().asJava).extract(sub)
      val rows   = tables.get(0).getRows().asScala.map(_.get(0).getText())
      rows.mkString
    }

    using(PDDocument.load(in)) { doc =>
      val page = new ObjectExtractor(doc).extract(1)
      Capture[A].from(focus(page))
    }
  }

}
