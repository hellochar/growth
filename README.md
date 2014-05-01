Growth
======
Control a bunch of colored balls with a novation launchcontrol. For performed visuals.

![example gif 1](https://raw.githubusercontent.com/hellochar/growth/master/example1.gif "Example gif 1")
![example gif 2](https://raw.githubusercontent.com/hellochar/growth/master/example2.gif "Example gif 2")

What do I do?
===========

1. clone this repo
2. plug in your Novation Launchpad
3. Run `launchcontrol-lean/launchcontrol-lean.exe` if you're on Windows, or `launchcontrol-lean.maxpat` if you have Max MSP installed
4. Run growth.jar
5. Press buttons and turn dials to create balls and change how they draw
6. Optionally, connect an EEG (like the Emotiv EPOC) and [send OSC messages to port 57110](https://github.com/derekrazo/emotiv-osc)

How to build/change?
=========
1. Depends on scala 2.10.x, Processing jar libs, oscP5, and my [scala-libs library](https://github.com/hellochar/scala-libs)
2. Change src/Growth.scala, recompile, build
3. You can also change the max patch `launchcontrol-lean.maxpat`

What do the buttons and dials do?
=========

Buttons
------

1. create balls
2. explode and brighten screen outwards
3. explode screen outwards
4. implode screen inwards
5. copy a random part of the screen to another location
6. pull balls towards the screen center
7. remove all balls
8. Clear the screen

Dials
----

1. (top-left dial) control hue of balls
2. opacity of black background (0 = no background, 1 = full black)
3. size of balls
4. speed at which balls move to the right
5. speed at which balls jitter around the screen
6. number of balls created when you press button 1
7. . (none)
8. .
9. size of pulsing
10. frequency of pulsing
11. transparency of drawn balls
12. .
13. .
14. .
15. .
16. influence of repel and twist forces (which are also controlled by EEG readings)


Help!
======

email me at hellocharlien@hotmail.com!



Thanks,

Xiaohan Zhang

hellocharlien@hotmail.com

[www.zhangxiaohan.com](http://www.zhangxiaohan.com)
