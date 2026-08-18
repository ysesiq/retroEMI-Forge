# retroEMI Forge
EMI is a featureful and accessible item and recipe viewer for Minecraft.

This [fork](https://github.com/dilepton/emi_1.12.2) heavily uses code from this project, thereby violating this project's MIT license (by replacing the project developer Xy_Luce (Xy_Lose) with dilepton) and falsely claims that retroEMI is a compatibility layer. Please do not use it.

Fork form [Rewind/retroEMI](https://git.sleeping.town/Rewind/retroEMI)

exaptations accidentally ported it to 1.4.7 because NEI is a trash heap! Oopsie daisy!

Even though NEI-Unofficial is very powerful, it still inherits NEI's unaesthetic shortcomings, so I (Xy_Luce) ported it again to Forge 1.12.2/1.7.10! Woo!

## Port notes

[Jabel](https://github.com/bsideup/jabel) is used to permit usage of modern Java features while
compiling to Java 8, as 1.7 Forge won't run on anything newer.

Because I'm not good at Gradle, I changed the project structure to the traditional form and used GTNHGradle at the cost of manually syncing changes from the original branch.

Mixin is provided by [UniMixins](https://github.com/LegacyModdingMC/UniMixins).

Part of the code refers to [Bommels05's EMI 1.7.10 port](https://github.com/Bommels05/emi), such as tag, data, etc

## Note about NEI-Unofficial/HEI(JEI) compatibility

Nemi and Jemi are compatibility layers used for synchronizing recipes, obtaining screen exclusion area, and proxying drag-and-drop operations, among other things.

However, the NEI/JEI plugins made by developers are not necessarily standard-compliant. Nemi/Jemi are difficult to predict and may encounter conversion failures.

When facing conversion issues, the best solution is to write a plugin that implements `EmiPlugin`, rather than denouncing retroEMI Forge as absolute garbage.

### Nemi

Does not support NEI; only supports NEI-Unofficial (GTNH-NEI).

Note: This does not mean GTNH is fully supported yet. There are still many issues with GTNH at present—please use with caution.

### Jemi

Supports both JEI and HEI.

Does not support NEI when it runs as a JEI addon, due to screen conflicts.
