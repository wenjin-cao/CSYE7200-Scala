package finalProj.modify

import finalProj.analysis.Face
import finalProj.transform.WithGrey
import org.bytedeco.javacpp.helper.opencv_core.AbstractCvScalar
import org.bytedeco.javacpp.opencv_core.{FONT_HERSHEY_SIMPLEX, Mat, Point, Scalar}
import org.bytedeco.javacpp.opencv_imgproc.{CV_AA, putText, rectangle}

class EyeDrawer(fontScale: Float = 0.6f) {

  private val BlueColour = new Scalar(AbstractCvScalar.BLUE)
  private val GreenColour = new Scalar(AbstractCvScalar.GREEN)
  /**
   * Clones the Mat, draws squares around the faces on it using the provided [[Face]] sequence and returns the new Mat
   */
  def drawFaces(withGrey: WithGrey, faces: Seq[Face]): Mat = {
    val clonedMat = withGrey.orig.clone()
    for (f <- faces) drawFace(clonedMat, f)
    clonedMat
  }

  private def drawFace(clonedMat: Mat, f: Face): Unit = {

    rectangle(
      clonedMat,
      new Point(f.faceRect.x, f.faceRect.y),
      new Point(f.faceRect.x + f.faceRect.width, f.faceRect.y + f.faceRect.height),
      BlueColour,
      1,
      CV_AA,
      0
    )

    // draw the eyes number
    val cvPoint = new Point(f.faceRect.x, f.faceRect.y - 20)
    putText(clonedMat, s"Eye ${f.id}", cvPoint, FONT_HERSHEY_SIMPLEX, fontScale, BlueColour)
  }

}
