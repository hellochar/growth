import java.io.PrintWriter
import org.zhang.geom.{Vec3, Vec2}
import processing.core._
import org.zhang.lib._
import oscP5._

object Direction extends Enumeration {
  type Direction = Value
  val Left, Right, Up, Down = Value
  def offsetFor(d: Direction) = d match {
    case Left => Vec2(-1, 0)
    case Right => Vec2(1, 0)
    case Up => Vec2(0, -1)
    case Down => Vec2(0, 1)
  }
}
import Direction._

object Controller extends Serializable {
  // current state of dials; number from 0 - 1
  val dials = collection.mutable.Buffer.fill(17)(0f)
  val buttons = collection.mutable.Buffer.fill(9)(false)
  val directions = collection.mutable.Map(
    Left -> false,
    Right -> false,
    Up -> false,
    Down  -> false
  )
//
//  def saveControllerState() {
//    Some(new PrintWriter("controller_state.csv")).foreach{ p =>
//      p.println(dials.mkString("\t"))
//      p.println(buttons.mkString("\t"))
//      p.close()
//    }
//  }
//
//  def loadControllerState() {
////      Some(new )
//  }

}
class Growth extends MyPApplet {

  import PApplet._
  import PConstants._

  lazy val oscP5 = new OscP5(this, 12000)
  lazy val oscP5_eeg = new OscP5(this, 57110)

  override def setup() {
//    size(displayWidth / 2, displayHeight / 2, P2D)
    size(displayWidth, displayHeight, P2D)
    colorMode(HSB)
    oscP5
    oscP5_eeg
    background(0)
//    registerMethod("dispose", new { def dispose() {
//      onDispose()
//    }})
  }

//  def onDispose() {
//  }

  def mutateScreen(src: Vec2, dim: Vec2, dest: Vec2, destDim: Vec2, mode: Int = SCREEN) {
    implicit def f2i(f: Float) = f.toInt
//    copy(src.x, src.y, dim.x, dim.y, dest.x, dest.y, dim.x, dim.y)
    blend(src.x, src.y, dim.x, dim.y, dest.x, dest.y, destDim.x, destDim.y, mode)
  }

  import Controller._

  def setDial(num: Int, value: Float) {
    dials(num) = value
  }

  def directionPressed(direction: Direction) {
    directions(direction) = true
    drawables.foreach{ _.pos += offsetFor(direction) * 50 }
  }
  def directionReleased(direction: Direction) {
    directions(direction) = false
  }

  def buttonPressed(num: Int) {
    buttons(num) = true
    val posBase = Vec2(0, random(height))
    for(iter <- 0 until map(dials(6), 0, 1, 1, 20).toInt) {
      val col = color(dials(1) * 255 + iter * 1, 255, 255)
      val pos = posBase + Vec2(0, iter*2)
      num match {
        case 1 => {
          drawables +:= new Drawer(pos, col) with WithNoiseSlider with CircleDrawer
        }
        case e => {

        }
      }
    }
  }

  object EEG {
    var frustration = 0.5f; // 0 == extremely calm, 1 = extremely frustrated
    var meditation = 0.5f
    var excitement = 0.5f
    var action = 0
  }

  def noise(v: Vec3):Float = noise(v.x, v.y, v.z)

  abstract class Drawer(var pos: Vec2, var colBase: Int) {
    private[this] val hueBase = hue(colBase)
    private[this] val saturationBase = saturation(colBase)
    private[this] val brightnessBase = brightness(colBase)
    def update(): Unit
    def draw(): Unit

    def radius = {
      val radiusBase = pow(map(dials(3), 0, 1, 1, 20), 2)
      val freq = map(dials(10), 0, 1, 0, 10)
      val pulseScalar = map(pow(cos(millis() / 1000f * freq), 4), -1, 1, 1, map(dials(9), 0, 1, 1, 4))
      radiusBase * pulseScalar
    }

    def col = color(hueBase, saturationBase, brightnessBase, map(dials(11), 0, 1, 0, 255))

    def isAlive = {
//      pos.x < 2*width  && pos.x > -width &&
//      pos.y < 2*height && pos.y > -height
      pos.x < 1.5f*width && pos.x >= -width/2 &&
      pos.y < 1.5f*height && pos.y >= -height/2
    }
  }

  trait WithNoiseSlider extends Drawer {
    val offset = random(1000)

    def update() {
      pos += Vec2(map(dials(4), 0, 1, 0, 20), 0)
      val noiseScale = pow(map(dials(5), 0, 1, 0, 5), 3)
//      val noiseScale = pow(map(EEG.frustration, 0, 1, 0, 5), 3)
      pos += Vec2(
        noise(pos.withZ(millis() + offset) / 100f) - .5f,
        noise(pos.withZ(millis() + offset) / 100f + Vec3(12.2f, 49, 900)) - .5f
      ) * noiseScale
    }

  }

  trait CircleDrawer extends Drawer {
    def draw() {
      noStroke(); fill(col)
//      val radius = EEG.frustration * 300
      ellipse(pos, radius, radius)
    }
  }

  trait MopDrawer extends Drawer {
    def draw() {
      stroke(col); fill(col)
      val x = pos.x
      val y = pos.y
      bezier(x, y, x, y + radius, x + radius * sin(millis() / 500f), y + radius, x + radius * sin(millis() / 500f), y + radius)
    }
  }

  var drawables = List[Drawer]()

  def buttonReleased(num: Int) {
    buttons(num) = false
  }

  override def draw() {
    fill(0, pow(dials(2), 2) * 255); noStroke()
    rect(0, 0, width, height)
    drawables = drawables.filter{ drawer =>
      drawer.update()
      drawer.draw()
      drawer.isAlive
    }
    var repelForce = map(sq(EEG.frustration), 0, 1, 0, 7f) * dials(16)
    for(d <- drawables;
        d2 <- drawables if d != d2) {
      val offset = d.pos - d2.pos
//      val force = offset.normalize * repelForce / offset.mag2
      val force = offset.ofMag(repelForce)
      d.pos += force
    }
    var twistForce = map(sq(EEG.excitement), 0, 1, 0, 4) * dials(16)
    for(d <- drawables) {
      val offset = d.pos - Vec2(width/2, height/2)
      val force = offset.rotate(PI/2).ofMag(twistForce)
      d.pos += force
    }
    if(buttons(2)) {
      mutateScreen(Vec2(0), Vec2(width, height), Vec2(-10, -10), Vec2(width + 20, height + 20), SCREEN)
    }
    if(buttons(3)) {
      mutateScreen(Vec2(0), Vec2(width, height), Vec2(-10, -10), Vec2(width + 20, height + 20), LIGHTEST)
    }
    if(buttons(4)) {
      mutateScreen(Vec2(0), Vec2(width, height), Vec2(10, 10), Vec2(width - 20, height - 20), LIGHTEST)
    }
    if(buttons(5)) {
      mutateScreen(Vec2(random(width), random(height)), Vec2(400), Vec2(random(width), random(height)), Vec2(400))
    }
    if(buttons(6)) {
      for(d <- drawables) {
        d.pos = d.pos * .8f + Vec2(width/2, height/2) * .2f
      }
    }
    if(buttons(7)) {
      drawables = List()
    }
    if(buttons(8)) {
      background(0)
    }
//    println(frameRate)
//    textAlign(LEFT, TOP)
//    stroke(255); fill(255)
//    text("EEG frustration (force " + repelForce + ": " + EEG.frustration + "\nEEG meditation: " + EEG.meditation, 0, 0)
//    text("1 - hue of circles\n" +
//         "2 - alpha of background\n" +
//         "3 - size of circles\n" +
//         "4 - horizontal speed\n" +
//         "5 - perlin noise scale\n" +
//         "6 - number of instances", 0, 0)
  }

  val buttonMap = (1 to 8).map{ i => ('0'+i) -> i }.toMap
  val dialMap = Map(
        'q' -> 1
      , 'w' -> 2
      , 'e' -> 3
      , 'r' -> 4
      , 't' -> 5
      , 'y' -> 6
      , 'u' -> 7
      , 'i' -> 8
      , 'a' -> 9
      , 's' -> 10
      , 'd' -> 11
      , 'f' -> 12
      , 'g' -> 13
      , 'h' -> 14
      , 'j' -> 15
      , 'k' -> 16
      )
  var dialsHeld = Set[Int]()
  override def keyPressed() {
    dialMap.get(key).map{dialsHeld += _}
    buttonMap.get(key).map(buttonPressed)
  }

  override def keyReleased() {
    dialMap.get(key).map{dialsHeld -= _}
    buttonMap.get(key).map(buttonReleased)
  }

  override def mouseMoved() {
    dialsHeld.foreach{setDial(_, map(mouseY, 0, height, 0, 1))}
  }


  def oscEvent(m: OscMessage) {
    try {
      val Button = """/button/(\d)""".r
      val Dial = """/dial/(\d+)""".r
      val DirectionRegex = """/(up|down|left|right)""".r

      m.addrPattern() match {
        case Button(num) => {
          m.get(0).intValue() match {
            case 0 => buttonReleased(num.toInt)
            case 127 => buttonPressed(num.toInt)
            case e => System.err.println("Bad value " + e)
          }
        }
        case Dial(num) => {
          val value = m.get(0).intValue() / 127f
          setDial(num.toInt, value)
        }
        case DirectionRegex(direction) => {
          m.get(0).intValue() match {
            case 0 => directionReleased(Direction.withName(direction.capitalize))
            case 127 => directionPressed(Direction.withName(direction.capitalize))
            case e => System.err.println("Bad value " + e)
          }
        }
        case "/eeg/frustration" => {
          val args = m.arguments()
          val firstArg = args(0).asInstanceOf[Float]
          if(Math.random() < .01f) println("Got frustration", firstArg)
          EEG.frustration = firstArg
        }
        case "/eeg/meditation" => {
          val args = m.arguments()
          val firstArg = args(0).asInstanceOf[Float]
          if(Math.random() < .01f) println("Got meditation", firstArg)
          EEG.meditation = firstArg
        }
        case "/eeg/excitement/short" => {
          val args = m.arguments()
          val firstArg = args(0).asInstanceOf[Float]
          if(Math.random() < .01f) println("Got excitement", firstArg)
          EEG.excitement = firstArg
        }
        case "/eeg/action" => {
          val args = m.arguments()
          val firstArg = args(0).asInstanceOf[Int]
//          println("Got action", firstArg)
          EEG.action = firstArg
        }
        case e => {
//            System.err.println("Got unknown message " + e + ", arguments " + m.arguments())
        }
      }
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }
}

object Growth {

  def main(args: Array[String]) {
    PApplet.main(Array("--display=1", "--present", "Growth"))
//    PApplet.main(Array("Growth"))
  }

}