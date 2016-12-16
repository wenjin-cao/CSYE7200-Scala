import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, WordSpecLike}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestActors, TestFSMRef, TestKit, TestProbe}
import akka.actor.{Actor, ActorKilledException, ActorRef, ActorRefFactory, ActorSystem, FSM, Kill, Props}

import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.{Keep, Sink, Source}

import com.typesafe.config.ConfigFactory

class WebcamFaceDetectorSpec extends TestKit(ActorSystem("system")) with Matchers with ImplicitSender
with WordSpecLike with BeforeAndAfterAll{

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  implicit val actorsystem = ActorSystem("system", ConfigFactory.parseString("""akka.loggers = ["akka.testkit.TestEventListener"] """))

  try {
    val actor = actorsystem.actorOf(Props.empty)
    EventFilter[ActorKilledException](occurrences = 1) intercept {
      actor ! Kill
    }
  } finally {
    shutdown(actorsystem)
  }


  //Externalize child making from the parent(Alternatively, you can tell the parent how to create its child. There are two ways to do this: by giving it a Props object or by giving it a function which takes care of creating the child actor:)
  class DependentParent(childProps: Props) extends Actor {
    val child = context.actorOf(childProps, "child")
    var ponged = false

    def receive = {
      case "pingit" => child ! "ping"
      case "pong"   => ponged = true
    }
  }

  class GenericDependentParent(childMaker: ActorRefFactory => ActorRef) extends Actor {
    val child = childMaker(context)
    var ponged = false

    def receive = {
      case "pingit" => child ! "ping"
      case "pong"   => ponged = true
    }
  }

//  val maker = (_: ActorRefFactory) => probe.ref
//  val parent = system.actorOf(Props(classOf[GenericDependentParent], maker))
//
//  val sinkUnderTest = Sink.cancelled
//
//  TestSource.probe[Int]
//    .toMat(sinkUnderTest)(Keep.left)
//    .run()
//    .expectCancellation()


//  val sinkUnderTest = Sink.head[Int]
//
//  val (probe, future) = TestSource.probe[Int]
//    .toMat(sinkUnderTest)(Keep.both)
//    .run()
//  probe.sendError(new Exception("boom"))
//
//  Await.ready(future, 3.seconds)
//  val Failure(exception) = future.value.get
//  assert(exception.getMessage == "boom")



}
