package finalProj.video

import akka.actor.{ DeadLetterSuppression, Props, ActorSystem, ActorLogging }
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import akka.stream.scaladsl.Source
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacv.{ FrameGrabber, Frame }
import org.bytedeco.javacv.FrameGrabber.ImageMode


object Webcam {

  /**
   * Builds a Frame [[Source]]
   *
   * @param deviceId device ID for the webcam
   * @param dimensions
   * @param bitsPerPixel
   * @param imageMode
   * @param system ActorSystem
   * @return a Source of [[Frame]]s
   */
  def source(
    deviceId: Int,
    dimensions: Dimensions,
    bitsPerPixel: Int = CV_8U,
    imageMode: ImageMode = ImageMode.COLOR
  )(implicit system: ActorSystem): Source[Frame, Unit] = {
    val props = Props(//Props is a configuration class to specify options for the creation of actors, think of it as an immutable and thus freely shareable recipe for creating an actor including associated deployment information (e.g. which dispatcher to use)
      new WebcamFramePublisher(
        deviceId = deviceId,
        imageWidth = dimensions.width,
        imageHeight = dimensions.height,
        bitsPerPixel = bitsPerPixel,
        imageMode = imageMode
      )
    )
    val webcamActorRef = system.actorOf(props)
    val webcamActorPublisher = ActorPublisher[Frame](webcamActorRef)

    Source.fromPublisher(webcamActorPublisher)
  }

  // Building a started grabber seems finicky if not synchronised; there may be some freaky stuff happening somewhere.
  private def buildGrabber(
    deviceId: Int,
    imageWidth: Int,
    imageHeight: Int,
    bitsPerPixel: Int,
    imageMode: ImageMode
  ): FrameGrabber = synchronized {
    val g = FrameGrabber.createDefault(deviceId)
    g.setImageWidth(imageWidth)
    g.setImageHeight(imageHeight)
    g.setBitsPerPixel(bitsPerPixel)
    g.setImageMode(imageMode)
    g.start()
    g
  }

  /**
   * Actor that backs the Akka Stream source
   */
  private class WebcamFramePublisher(
      deviceId: Int,
      imageWidth: Int,
      imageHeight: Int,
      bitsPerPixel: Int,
      imageMode: ImageMode
  ) extends ActorPublisher[Frame] with ActorLogging {
    //ActorPublisher:to make the actor a stream publisher that keeps track of the subscription life cycle and requested elements.
    //ActorLogging
    private implicit val ec = context.dispatcher

    // Lazy so that nothing happens until the flow begins
    private lazy val grabber = buildGrabber(
      deviceId = deviceId,
      imageWidth = imageWidth,
      imageHeight = imageHeight,
      bitsPerPixel = bitsPerPixel,
      imageMode = imageMode
    )

    def receive: Receive = {//Actor type Receive
      case _: Request => emitFrames()
      case Continue => emitFrames()
      case Cancel => onCompleteThenStop()
      case unexpectedMsg => log.warning(s"Unexpected message: $unexpectedMsg")
    }

    private def emitFrames(): Unit = {
      if (isActive && totalDemand > 0) {
        /*
          Grabbing a frame is a blocking I/O operation, so we don't send too many at once.
         */
        grabFrame().foreach(onNext)
        if (totalDemand > 0) {
          self ! Continue
        }
      }
    }

    private def grabFrame(): Option[Frame] = {
      Option(grabber.grab())
    }
  }

  private case object Continue extends DeadLetterSuppression

}
