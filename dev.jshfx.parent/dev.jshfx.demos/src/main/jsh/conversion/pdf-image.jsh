/resolve org.apache.pdfbox:pdfbox:3.0.0-RC1

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer

PDDocument document = Loader.loadPDF(JSh.getCurDir().resolve("../../resources/conversion/hello-world.pdf").toFile())
PDFRenderer pdfRenderer = new PDFRenderer(document)
BufferedImage bim = pdfRenderer.renderImage(0, 1)
ImageIO.write(bim, "jpg", JSh.getCurDir().resolve("../../resources/conversion/hello-world-1.jpg").toFile())