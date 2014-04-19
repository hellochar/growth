import org.zhang.geom.{Vec3, Vec2}
import processing.core._
import org.zhang.lib._
import oscP5._

class Growth extends MyPApplet {

  import PApplet._
  import PConstants._

  object Direction extends Enumeration {
    type Direction = Value
    val left, right, up, down = Value
  }
  import Direction._

  lazy val oscP5 = new OscP5(this, 12000)

  override def setup() {
    size(displayWidth, displayHeight, P2D)
    colorMode(HSB)
    oscP5
  }

  // current state of dials; number from 0 - 1
  val dials = collection.mutable.Buffer.fill(17)(0f)
  val buttons = collection.mutable.Buffer.fill(9)(false)
  val directions = collection.mutable.Map(
    left -> false,
    right -> false,
    up -> false,
    down  -> false
  )

  def setDial(num: Int, value: Float) {
    dials(num) = value
  }

  def directionPressed(direction: Direction) {
    directions(direction) = true
  }
  def directionReleased(direction: Direction) {
    directions(direction) = false
  }

  def buttonPressed(num: Int) {
    buttons(num) = true
    val pos = Vec2(mouseX, mouseY)
    for(iter <- 0 until map(dials(6), 0, 1, 1, 20).toInt) {
      val col = color(dials(1) * 255 + iter * 1, 255, 255, 64)
      num match {
        case 1 => {
          drawables +:= new Drawer(pos, col) with WithNoiseSlider with CircleDrawer
        }
        case 2 => {
          drawables +:= new Drawer(pos, col) with WithNoiseSlider with MopDrawer
        }
      }

    }
  }

  def noise(v: Vec3):Float = noise(v.x, v.y, v.z)

  abstract class Drawer(var pos: Vec2, var col: Int) {
    def update(): Unit
    def draw(): Unit

    def isAlive = {
      pos.x < 2*width  && pos.x > -width &&
      pos.y < 2*height && pos.y > -height
    }
  }

  trait WithNoiseSlider extends Drawer {
    val offset = random(1000)

    def update() {
      pos += Vec2(map(dials(4), 0, 1, -10, 10), 0)
      val noiseScale = pow(map(dials(5), 0, 1, 0, 5), 3)
      pos += Vec2(
        noise(pos.withZ(millis() + offset) / 100f) - .5f,
        noise(pos.withZ(millis() + offset) / 100f + Vec3(12.2f, 49, 900)) - .5f
      ) * noiseScale
    }

  }

  trait CircleDrawer extends Drawer {
    def draw() {
      noStroke(); fill(col)
      val radius = pow(map(dials(3), 0, 1, 1, 20), 2)
      ellipse(pos, radius, radius)
    }
  }

  trait MopDrawer extends Drawer {
    def draw() {
      stroke(col); fill(col)
      val x = pos.x
      val y = pos.y
      val radius = pow(map(dials(3), 0, 1, 1, 20), 2)
      bezier(x, y, x, y + radius, x + radius * sin(millis() / 500f), y + radius, x + radius * sin(millis() / 500f), y + radius)
    }
  }

  var drawables = List[Drawer]()

  def buttonReleased(num: Int) {
    buttons(num) = false
  }

  override def draw() {
    //background(255)
    fill(0, pow(dials(2), 2) * 255); noStroke()
    rect(0, 0, width, height)
    drawables = drawables.filter{ drawer =>
      drawer.update()
      drawer.draw()
      drawer.isAlive
    }
    textAlign(LEFT, TOP)
    stroke(255); fill(255)
    text("1 - hue of circles\n" +
         "2 - alpha of background\n" +
         "3 - size of circles\n" +
         "4 - horizontal speed\n" +
         "5 - perlin noise scale\n" +
         "6 - number of instances", 0, 0)
  }

  def oscEvent(m: OscMessage) {
    try {
      synchronized {
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
              case 0 => directionReleased(Direction.withName(direction))
              case 127 => directionPressed(Direction.withName(direction))
              case e => System.err.println("Bad value " + e)
            }
          }
          case e => {
            System.err.println("Got unknown message " + e + ", arguments " + m.arguments())
          }
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
  }

}