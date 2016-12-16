import finalProj.analysis.{EyeDetector, FaceDetector, HaarDetectorFlag}
import finalProj.video.Dimensions
import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util._

/**
  * Created by Wenjin on 12/14/16.
  */
class DetectorSpec extends FlatSpec with Matchers{

  behavior of("FaceDetector.defaultCascadeFile")

  it should "work for the sample file" in {
    val imageDimensions = Dimensions(width=640,height=480)

    val faceDetector = FaceDetector.defaultCascadeFile(imageDimensions)

    faceDetector should matchPattern{case Success(_)=>}

  }

  behavior of("EyeDetector.defaultCascadeFile")

  it should "work for the sample file" in {
    val imageDimensions = Dimensions(width=640,height=480)

    val eyesDetector = EyeDetector.defaultCascadeFile(imageDimensions)

    eyesDetector should matchPattern{case Success(_)=>}

  }

}
