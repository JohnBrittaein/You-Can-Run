# You Can Run

You can run is a horror health AR app in which a user must keep running and avoiding a glitch monster.

# Technical Implementation

There are three main components I can forsee:

1. AR Enviroment
2. Unleying logic, speed calculations, relative location, pursuit tracking etc.
3. UI and art work/sounds

# AR Enviroment

The AR enviroment will be created via ARCore, which is a google SDK for Android apps and allows the creation of AR enviroments, object tracking etc. using
phone's sensor's and cameras.

# Logic

### Movement and Tracking: 
The players speed will be calculated with either the 

- Location Services (GPS), potentially simpler, but only works with internet connection or outdoors
- Accelrometer local sensors, might be harder to implement howver works anywhere.

A relative 2D, or 3D grid will be created around the plyaer, in which a monster is spawned at some random corner, from there the monster will pursue the player
at an increasing speed. This can be done via a pursuit algorithm, however if a player is moving faster and in the correct direction, the monster will not catch up.
I'm also thinking introducing more life-like physical properties such as momentum etc. to ensure the monster isn't always laser focused on the player. We also came up with 
potentially allowing the monster to navigate 4 Dimensionally, or in other wods have the ability to teleport every once and a while if it gets to far away.

## Speed System

**Accelerometer**

**GPS**

**Hybrid Approach**

## Pursuit Algorithm:

**Momentum Decay**

**Predictive Pursuit Model**

**Periodic Glitches and teleporting**

## UI And Art 

## Fun Gameplay Ideas

- Hide events
- Powerups or stuns
- Multi-dimensional camera views, Normal, Predator Vision etc.,
- Integrate a smart watch to monitor heart beat and adapt the monsters behavior (More agression, more glitches etc.)