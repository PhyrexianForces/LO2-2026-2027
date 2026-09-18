# Base
This package contains several packages designed to help facilitate development. This file explains the purpose of each package, and summarizes the package's contents.

## Actions
The purpose of this package is to help create a framework for actions. Actions are sets of highly-abstract instructions which could be useful during autonomous or in order to create a set of repeatable actions during teleop that can be triggered at the press of a button.

## Controllers
The package contains system controllers such as a [Proportional–integral–derivative controller](https://en.wikipedia.org/wiki/Proportional%E2%80%93integral%E2%80%93derivative_controller).

## Gamepad
This package helps interactions with the gamepad by further abstracting the gamepad class.

## Hardware
This package contains basic hardware components such as motors.

## Pathing
This package defines the localization and pathing system. Notably, this package does not interact with the hardware itself.

## Manipulators
This package contains code to help facilitate the usage of certain manipulators such as linear slides or an arm. This code relies heavily on the hardware package.

## Movement
This package contains the basis of movement. This package contains building blocks such as a Mecanum drive. This package does not define high-level movement that should be defined in the pathing package instead.

## Sensors
This package contains various sensors such as vision (e.g. April Tags) or encoder sensors to find the robots position on the field.