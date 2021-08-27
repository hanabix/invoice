package invoice

import java.io.File

import scala.jdk.CollectionConverters._

import technology.tabula.ObjectExtractor
import technology.tabula.extractors.BasicExtractionAlgorithm

import org.apache.pdfbox.pdmodel._

object tabula {

  implicit val fileToPDDocument: FileTo[(File, PDDocument)] = f => (f, PDDocument.load(f))

  implicit val docEitherExtractors: Extractors[List, PDDocument] = new Extractors[List, PDDocument] {
    def of(doc: PDDocument) = new Extractor[List] {

      val page = new ObjectExtractor(doc).extract(1)

      def stringIn(s: Selection): List[Field[Option[String]]] = {
        val t      = s.area.top.toFloat
        val l      = s.area.left.toFloat
        val b      = s.area.bottom.toFloat
        val r      = s.area.right.toFloat
        val sub    = page.getArea(t, l, b, r)
        val tables = new BasicExtractionAlgorithm(List().asJava).extract(sub)
        val rows   = tables.get(0).getRows().asScala.map(_.get(0).getText())

        if (rows.isEmpty) return s.fields.map(_.copy(value = Option.empty[String]))

        val fields = for {
          row <- rows
          fixed = row.replace(" ", "")
          field <- s.fields
        } yield field.copy(value = field.value.on(fixed))

        fields
          .groupBy(_.name)
          .values
          .toList
          .map(_.reduce[Field[Option[String]]] {
            case (f @ Field(_, Some(_)), _) => f
            case (_, f)                     => f
          })
      }
    }
  }

}
