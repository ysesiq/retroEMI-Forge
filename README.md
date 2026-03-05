# retroEMI Forge
EMI is a featureful and accessible item and recipe viewer for Minecraft.

* Fork form [Rewind/retroEMI](https://git.sleeping.town/Rewind/retroEMI)

exaptations accidentally ported it to 1.4.7 because NEI is a trash heap! Oopsie daisy!

Even though NEI-Unofficial is very powerful, it still inherits NEI's unaesthetic shortcomings, so Xy_Lose ported it again to Forge 1.7.10! Woo!

## Port notes

[Jabel](https://github.com/bsideup/jabel) is used to permit usage of modern Java features while
compiling to Java 8, as 1.7 Forge won't run on anything newer.

Because I'm not good at Gradle, I changed the project structure to the traditional form and used GTNHGradle at the cost of manually syncing changes from the original branch.

Mixin is provided by [UniMixins](https://github.com/LegacyModdingMC/UniMixins).

Part of the code refers to [Bommels05's EMI 1.7.10 port](https://github.com/Bommels05/emi), such as tag, data, etc

### Note about NEI-Unofficial compatibility

Stay tuned, it's not something that will happen for a while, and I've been under a lot of pressure to study lately.
