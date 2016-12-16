package finalProj

import java.awt.Dimension

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import finalProj.analysis.{EyeDetector, FaceDetector}
import finalProj.transform.{Flip, MediaConversion, WithGrey}
import finalProj.modify.{EyeDrawer, FaceDrawer, FaceOnlyDrawer}
import finalProj.video.{Dimensions, Webcam}
import org.bytedeco.javacv.CanvasFrame

import scala.swing._
import scala.util.Success

object WebcamFaceDetector extends SimpleSwingApplication {

  def top: Frame = new OptionsFrame

  /**
   * This is the initial frame, which presents two simple options, to load a custom Haar cascade file for face detection,
   * or to use the default one
   */
  private class OptionsFrame extends Frame { currentFrame =>

    peer.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

    val imageDimensions = Dimensions(width = 640, height = 480)
//    val chooseCascadeBtn = Button("Load custom Haar cascade file") {
//      val filePath = openChooser()
//      filePath.foreach { path =>
//        val detector = new FaceDetector(dimensions = imageDimensions, classifierPath = path)
//        openFaceDetectionWindow(detector)
//      }
//    }
    val defaultCascadeBtn1 = Button("Use default face Haar cascade file") {
      FaceDetector.defaultCascadeFile(imageDimensions) match{
        case Success(fd)=>openFaceDetectionWindow1(fd)
      }


    }

    val defaultCascadeBtnEye = Button("Use default eyes Haar cascade file") {
      EyeDetector.defaultCascadeFile(imageDimensions) match {
        case Success(detector)=>openFaceDetectionWindowEye(detector)
      }

    }
    val defaultCascadeBtn = Button("Use default face and eye Haar cascade file") {
//      val detector = FaceDetector.defaultCascadeFile(imageDimensions)
//      openFaceDetectionWindow(detector)

      FaceDetector.defaultCascadeFile(imageDimensions) match{
        case Success(fd)=>openFaceDetectionWindow1(fd)
      }

    }

    val mainPanel = new GridPanel(rows0 = 0, cols0 = 1) {
      preferredSize = new Dimension(300, 200)
      contents ++= Seq(defaultCascadeBtn1, defaultCascadeBtnEye, defaultCascadeBtn)
    }

    contents = mainPanel

    private def openChooser(): Option[String] = {
      val chooser = new FileChooser(new java.io.File("."))
      chooser.fileSelectionMode = FileChooser.SelectionMode.FilesOnly
      chooser.showOpenDialog(currentFrame) match {
        case FileChooser.Result.Approve => Some(chooser.selectedFile.toPath.toAbsolutePath.toString)
        case _ => None
      }
    }

    private def openFaceDetectionWindow(faceDetector: FaceDetector): Unit = {
      new DetectionFrame(faceDetector)
      peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
      currentFrame.close()
    }


    private def openFaceDetectionWindow1(faceDetector: FaceDetector): Unit = {
      new DetectionFrame1(faceDetector)
      peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
      currentFrame.close()
    }
    private def openFaceDetectionWindowEye(faceDetector: EyeDetector): Unit = {
      new DetectionFrameEye(faceDetector)
      peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
      currentFrame.close()
    }
  }

  /**
   * Our detection window; opened by Initial Frame
   */
  private class DetectionFrame(faceDetector: FaceDetector) {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    Webcam.source(deviceId = 0, dimensions = faceDetector.dimensions) match {
      case Success(webcamSource)=>val flow = webcamSource
        .map(MediaConversion.toMat) // most OpenCV manipulations require a Matrix
        .map(Flip.horizontal)
        .map(WithGrey.build)
        .map(faceDetector.detect)
        .map((faceDrawer.drawFaces _).tupled)
        .map(MediaConversion.toFrame) // convert back to a frame
        .map(canvas.showImage)
        .to(Sink.ignore)//A Sink that will consume the stream and discard the elements.

        flow.run()

    }

    val canvas = new CanvasFrame("Webcam")
    //  //Set Canvas frame to close on exit
    canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

    val faceDrawer = new FaceDrawer()


  }
  /**
    * Our detection window; For eyes only
    */
  private class DetectionFrameEye(faceDetector: EyeDetector) {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    Webcam.source(deviceId = 0, dimensions = faceDetector.dimensions) match {
      case Success(webcamSource)=>val flow = webcamSource
        .map(MediaConversion.toMat) // most OpenCV manipulations require a Matrix
        .map(Flip.horizontal)
        .map(WithGrey.build)
        .map(faceDetector.detect)
        .map((eyeDrawer.drawFaces _).tupled)
        .map(MediaConversion.toFrame) // convert back to a frame
        .map(canvas.showImage)
        .to(Sink.ignore)

        flow.run()
    }

    val canvas = new CanvasFrame("Webcam")
    //  //Set Canvas frame to close on exit
    canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

    val eyeDrawer = new EyeDrawer()



  }
  /**
    * Our detection window; For face only
    */
  private class DetectionFrame1(faceDetector: FaceDetector) {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

//    val webcamSource = Webcam.source(deviceId = 0, dimensions = faceDetector.dimensions)

    Webcam.source(deviceId = 0, dimensions = faceDetector.dimensions) match{
      case Success(webcamSource)=>val flow = webcamSource
        .map(MediaConversion.toMat) // most OpenCV manipulations require a Matrix
        .map(Flip.horizontal)
        .map(WithGrey.build)
        .map(faceDetector.detect)
        .map((faceOnlyDrawer.drawFaces _).tupled)
        .map(MediaConversion.toFrame) // convert back to a frame
        .map(canvas.showImage)
        .to(Sink.ignore)

        flow.run()
    }
    val canvas = new CanvasFrame("Webcam")
    //  //Set Canvas frame to close on exit
    canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)

    val faceOnlyDrawer = new FaceOnlyDrawer()



  }
}