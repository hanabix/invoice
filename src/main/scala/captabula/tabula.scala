package captabula

import java.io.InputStream
import java.util.Collections

import scala.jdk.CollectionConverters._

import cats.data.Kleisli

import org.apache.pdfbox.pdmodel.PDDocument
import technology.tabula._
import technology.tabula.extractors._

object tabula {

  implicit def documentIndexer = new Indexer[PDDocument] {
    def apply(indices: List[Int]): Transform[PDDocument, Capture] = Kleisli { doc =>
      try {
        val ii    = indices.map(Integer.valueOf).asJava
        val pages = new ObjectExtractor(doc).extract(ii).asScala.toList
        pages.map(asCapture)
      } finally doc.close()
    }
  }

  implicit def inputStreamIndexer(implicit i: Indexer[PDDocument]) = new Indexer[InputStream] {
    def apply(indices: List[Int]) = i.apply(indices).compose(in => List(PDDocument.load(in)))
  }

  private def asCapture(p: Page) = new Capture {
    def rect(top: Number, left: Number, width: Number, height: Number): String = {
      val r      = new Rectangle(top.floatValue(), left.floatValue(), width.floatValue(), height.floatValue())
      val tables = new BasicExtractionAlgorithm(Collections.emptyList()).extract(p.getArea(r))
      tables.asScala.headOption.map(_.getRows().asScala.map(_.get(0).getText()).mkString).getOrElse("")
    }

    def sheet(row: Int, column: Int): String = {
      val tables = new SpreadsheetExtractionAlgorithm().extract(p)
      // TODO fix no table found.
      // import technology.tabula.detectors.SpreadsheetDetectionAlgorithm
      // val rects  = new SpreadsheetDetectionAlgorithm().detect(p)
      // println(s"------ ${rects.asScala}")
      tables.asScala.headOption.map(_.getCell(row, column).getText()).getOrElse("")
    }

  }

}
