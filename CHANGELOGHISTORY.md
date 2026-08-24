# 1.1.24-beta.3

* Improvement: Speeds up find resources
* Improvement: Speeds up emi reload
* Improvement: Implemented greater compatibility with the JEI interface (1.12.2)
* Remove: Removed FermiumBooter support (1.12.2)
* Fix: Fixed the issue where the recipe screen could not go back
* Fix: Fixed prefix issues for search tags and tooltips in zh_cn and zh_tw
* Fix: Fixed incorrect item rendering when highlighting search results (1.7.10)
* Fix: Fixed a crash caused by Mixin bootstrapping (1.12.2)
* Fix: Fixed the issue where the game would not launch when loaded together with JEI (1.12.2)
* Fix: Fixed the issue where Ctrl+O could not switch the EMI interface to the JEI interface in some cases (1.12.2)
* Fix: Fixed an error when registering brewing recipes (1.12.2)
* Fix: Fixed an issue where the output slot item was incorrect for bucketable fluid recipes in world interaction types (1.12.2)
* Fix: Fixed some crashes caused by Jemi (1.12.2)
* Fix: Fixed the issue where Jemi prevented searching (1.12.2)
* Fix: Fixed Jemi's inability to render custom ingredients (1.12.2)
* Fix: Fixed incorrect rendering of some Jemi recipes (1.12.2)
* Fix: Fixed GL state leaks in Jemi recipes (1.12.2)
* Fix: Fixed some Jemi elements not displaying correctly (1.12.2)
* Fix: Fixed Jemi spamming a large number of errors (1.12.2)
* Fix: Fixed Jemi's inability to display ingredients with variable loop counts (1.12.2)
* Fix: Fixed Jemi ignoring `ITooltipCallback<ItemStack>` information (1.12.2)
* Fix: Fixed Jemi using tanks instead of slots for some ingredients (1.12.2)
* Fix: Fixed the issue where the mod ownership of Jemi recipe types was incorrect (1.12.2)
* Fix: Fixed Jemi not handling wildcards (1.12.2)
* Fix: Fixed Nemi not retrieving recipe tooltips (1.7.10)
* Fix: Fixed Nemi recipes changing too quickly (1.7.10)

### Known bugs that will not be fixed
* Cannot render Modular Machinery Community Edition's multiblock previews (1.12.2)
* Duplicate Jemi recipe screen elements converted from Tinker I/O (1.12.2)

---

# 1.1.24-beta.2

* Feature: Implemented Jemi (1.12.2)
	+ Support JEI/HEI
* Feature: Refine Nemi (1.7.10, Thanks to Deeplerg for the contribution)
	+ NEIU Only
	+ Support for conversion recipes
* Feature: Add built-in modernity pack
* Improvement: Speeds up scanning for plugins
* Sync:
	+ Introduce some abstractions -- emilyploszaj
	+ search all registries in tag queries instead of just items [#1231](https://github.com/emilyploszaj/emi/pull/1231) -- Abbie5
	+ feat(config, screen): add configurable display all recipes keybind [#1183](https://github.com/emilyploszaj/emi/pull/1183) -- LivP0810
	+ Update ru_ru.json [#1204](https://github.com/emilyploszaj/emi/pull/1204) -- mpustovoi
	+ add api for displaying all recipes made in a given workstation [#1209](https://github.com/emilyploszaj/emi/pull/1209) -- Abbie5
	+ fix: correct typos in ja_jp.json [#1215](https://github.com/emilyploszaj/emi/pull/1215) -- mochi-753
	+ fix [#1200)](https://github.com/emilyploszaj/emi/pull/1200) -- link-fgfgui
* Fix: Resolved crashes caused by mixins
* Fix: Fixed incorrect rendering on some layers

---

# 1.1.24-beta1

* Feature: Support forge 1.12.2
* Feature: Tags model has been reimplemented
* Feature: (FAKE) Batcher render has been reimplemented
* Improvement: Favorite and recipe tree button texture improvement
* Improvement: Optimized code
* Sync:
  + i18n: Traditional Chinese localization. ([emilyploszaj#1134](https://github.com/emilyploszaj/emi/pull/1161)) -- CrazyO9
  + Added German Translations (de_de.json) ([emilyploszaj#1156](https://github.com/emilyploszaj/emi/pull/1133)) -- Taylo160
  + [WILL NOT BE ADDED] allow specifying component changes as json rather than stringified nbt ([emilyploszaj#1132](https://github.com/emilyploszaj/emi/pull/1132)) -- Abbie5
  + Update ja_jp.json ([emilyploszaj#1133](https://github.com/emilyploszaj/emi/pull/1156)) -- HayaKoh-WeldyAlin
  + Refactor workstation handling in RecipeScreen ([emilyploszaj#1161](https://github.com/emilyploszaj/emi/pull/1134)) -- link-fgfgui
  + [WIP] Add global mixin -- emilyploszaj
  + Lower global mixin java version -- emilyploszaj
  + Fix [emilyploszaj#1171](https://github.com/emilyploszaj/emi/pull/1174) ([emilyploszaj#1174](https://github.com/emilyploszaj/emi/pull/1174)) -- exaskye
  + 1.1.23 & 1.1.24  -- emilyploszaj
* Fix: Some tags are not listed
* Fix: Background misalignment
* Fix: Delete items, chess invitations, items given by the server, and fill recipes are abnormal

---

# 1.1.22-beta3

* Feature: Reimplemented EMI Data
* Fix: Sometimes colors of the recipe screen elements were wrong
* Fix: Crashes when loading with DragonAPI
* Fix: Change scale of the recipe tree screen leads to coloring errors
* Fix: The recipe screenshot path is wrong
* Improvement: Optimized code

---

# 1.1.22-beta2

* Feature: Support JSON files (Including language & data files)
* Feature: /emi command
* Improvement: Optimized recipe IDs
* Improvement: Optimized performance and significantly improved loading speed
* Improvement: Optimized code
* Fix: UI confusion caused when loading with NEIU
  + Press Ctrl+O to switch ui between the two mods

---

# 1.1.22-beta1

* Feature: EMI 1.0.24 to 1.1.22 updates
* Feature: Recipe screenshot
* Improvement: Removed NilLoader support
* Improvement: Update MC version to 1.7.10
* Improvement: Optimized code
