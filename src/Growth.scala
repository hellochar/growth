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
    oscP5
  }

  // current state of dials; number from 0 - 1
  val dials = collection.mutable.Buffer.fill(16)(0f)
  val buttons = collection.mutable.Buffer.fill(8)(false)
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
  }

  def buttonReleased(num: Int) {
    buttons(num) = false
  }

  override def draw() {
    background(255)
    println(dials.toString()+", "+buttons.toString() + ", " + directions.toString())
  }

  def oscEvent(m: OscMessage) {
    synchronized {
      val Button = """/button/(\d)""".r
      val Dial = """/dial/(\d+)""".r
      val DirectionRegex = """/(up|down|left|right)""".r
      m.addrPattern() match {
        case Button(num) => {
          m.get(0).intValue() match {
            case 0 => buttonReleased(num.toInt - 1)
            case 127 => buttonPressed(num.toInt - 1)
            case e => System.err.println("Bad value " + e)
          }
        }
        case Dial(num) => {
          val value = m.get(0).intValue() / 127f
          setDial(num.toInt - 1, value)
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
  }
}