# Beavertronics Swerve
This is the command-based swerve code for 5970, using the swerve library YAGSL.
This project includes the following:
- Swerve
- Vision
- Swank (Swerve-tank)
- Swar (Swerve-car), and FS-Swar (Foursteer-swerve-car)
- Additional "child mode" drive command
- Example pneumatics subsystem


## Beaverlib
**Updating beaverlib** <br>

In order to update beaverlib, you can either update it in the terminal with this:

- `git submodule update --init --recursive --remote`

Or you can update it in Intellij IDEA by clicking on branches in the top-left,
then selecting beaverlib and updating it.

**Reinstalling beaverlib** <br>
To fix beaverlib not showing up or updating, you can delete the beaverlib folder
then reinstall it with the following function:

- `git submodule add https://github.com/beavertronics/beaverlib.git ./(path from root of project)`

For us specifically, the full command is:
- `git submodule add https://github.com/beavertronics/beaverlib.git ./src/main/kotlin/beaverlib`

JVM: corretto-17.0.14, found in .jdks under user account
JVM: consider WPI jdk for simulation which is under wpilib/(year)/jdk

## Instructions / information
- [Instructions on Swerve setup](/docs/Swerve.md)
- [Information on Swank](/docs/Swank.md)
- [Information on Swar and FS-Swar](/docs/Swar.md)
- [Information on child mode]
