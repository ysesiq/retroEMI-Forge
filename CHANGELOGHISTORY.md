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

# 1.1.22-beta1

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
