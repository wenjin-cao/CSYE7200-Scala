import akka.actor.ActorSystem
import finalProj.analysis.{EyeDetector, FaceDetector, HaarDetectorFlag}
import finalProj.video.{Dimensions, Webcam}
import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util._

/**
  * Created by Wenjin on 12/14/16.
  */
class DetectorSpec extends FlatSpec with Matchers{

  behavior of("FaceDetector.defaultCascadeFile")

  it should "work for Face Detector" in {
    val imageDimensions = Dimensions(width=640,height=480)

    val faceDetector = FaceDetector.defaultCascadeFile(imageDimensions)

    faceDetector should matchPattern{case Success(_)=>}

  }

  behavior of("EyeDetector.defaultCascadeFile")

  it should "work for Webcam" in {
    val imageDimensions = Dimensions(width=640,height=480)

    val eyesDetector = EyeDetector.defaultCascadeFile(imageDimensions)

    eyesDetector should matchPattern{case Success(_)=>}

  }

  behavior of("Webcam.source")

  it should "work for Eye Detector" in {
    val imageDimensions = Dimensions(width=640,height=480)

    implicit val system = ActorSystem()
    val source = Webcam.source(0,imageDimensions)(system)

    source should matchPattern{case Success(_)=>}

  }
}
