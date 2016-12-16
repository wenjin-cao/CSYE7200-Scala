package finalProj.analysis

import finalProj.transform.{MediaConversion, WithGrey}
import finalProj.video.Dimensions
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier

import scala.util.Try

object FaceDetector {

  /**
   * Builds a FaceDetector with the default Haar Cascade classifier in the resource directory
   */
  def defaultCascadeFile(
    dimensions: Dimensions,
    scaleFactor: Double = 1.3,
    minNeighbours: Int = 3,
    detectorFlag: HaarDetectorFlag = HaarDetectorFlag.DoCannyPruning,
    minSize: Dimensions = Dimensions(width = 30, height = 30),
    maxSize: Option[Dimensions] = None
    ): Try[FaceDetector] = {
    val classLoader = this.getClass.getClassLoader
    val faceXml = classLoader.getResource("haarcascade_frontalface_alt.xml").getPath
    val leyeXml = classLoader.getResource("haarcascade_lefteye_2splits.xml").getPath
    val reyeXml = classLoader.getResource("haarcascade_righteye_2splits.xml").getPath
    Try(new FaceDetector(
      dimensions = dimensions,
      classifierPath1 = faceXml,
      classifierPath2 = leyeXml,
      classifierPath3 = reyeXml,
      scaleFactor = scaleFactor,
      minNeighbours = minNeighbours,
      detectorFlag = detectorFlag,
      minSize = minSize,
      maxSize = maxSize
    ))
  }
}

class FaceDetector(
    val dimensions: Dimensions,
    classifierPath1: String,
    classifierPath2: String,
    classifierPath3: String,
    scaleFactor: Double = 1.3,
    minNeighbours: Int = 3,
    detectorFlag: HaarDetectorFlag = HaarDetectorFlag.ScaleImage,
    minSize: Dimensions = Dimensions(width = 30, height = 30),
    maxSize: Option[Dimensions] = None
) {

  private val faceCascade = new CascadeClassifier(classifierPath1)
  private val leftEyeCascade = new CascadeClassifier(classifierPath2)
  private val rightEyeCascade = new CascadeClassifier(classifierPath3)
  private val minSizeOpenCV = new Size(minSize.width, minSize.height)
  private val maxSizeOpenCV = maxSize.map(d => new Size(d.width, d.height)).getOrElse(new Size())

  /**
   * Given a frame matrix, a series of detected faces
   */
  def detect(frameMatWithGrey: WithGrey): (WithGrey, Seq[Face]) = {
    val currentGreyMat = frameMatWithGrey.grey
    val faceRects = findFaces(currentGreyMat)
    val faces = for {
      i <- 0L until faceRects.size()
      faceRect = faceRects.get(i)
    } yield {
      // the left eye should be in the top-left quarter of the face area
      val leftFaceMat = new Mat(currentGreyMat, new Rect(faceRect.x, faceRect.y, faceRect.width() / 2, faceRect.height() / 2))
      val leftEyeRect = new RectVector()
      leftEyeCascade.detectMultiScale(leftFaceMat, leftEyeRect, scaleFactor, minNeighbours, detectorFlag.flag, minSizeOpenCV, maxSizeOpenCV)

      // the right eye should be in the top-right quarter of the face area
      val rightFaceMat = new Mat(currentGreyMat, new Rect(faceRect.x + faceRect.width() / 2, faceRect.y, faceRect.width() / 2, faceRect.height() / 2))
      val rightEyeRect = new RectVector()
      rightEyeCascade.detectMultiScale(rightFaceMat, rightEyeRect, scaleFactor, minNeighbours, detectorFlag.flag, minSizeOpenCV, maxSizeOpenCV)
      Face(i, cloneRect(faceRect), cloneRect(leftEyeRect.get(0)), cloneRect(rightEyeRect.get(0)))
    }
    (frameMatWithGrey, faces)
  }
  // we need to clone the rect because openCV is recycling rectangles created by the detectMultiScale method
  private def cloneRect(rect: Rect): Rect = {
    new Rect(rect.x, rect.y, rect.width, rect.height)
  }

  private def findFaces(greyMat: Mat): RectVector = {
    val faceRects = new RectVector()
    faceCascade.detectMultiScale(greyMat, faceRects, scaleFactor, minNeighbours, detectorFlag.flag, minSizeOpenCV, maxSizeOpenCV)

    faceRects

  }

}