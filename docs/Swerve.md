# Swerve Setup

Our swerve uses YAGSL, which sets up and configures the modules for us. 

- For the most part, you will follow this guide: https://docs.yagsl.com/bringing-up-swerve/creating-your-first-configuration

However, there is one difference. At the end, when zeroing the modules, face them
so that the bevels of each wheel are facing to the left to the robot and are oriented to be
parallel with the frame (straight).
Additionally, ensure that rotating the wheels (CCW?) increases the encoder value for the wheel. Then, zero the encoders in the 
Spark MAX. **Do not set the zeros in swerve itself.**
