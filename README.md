# Chisels & Bits 2

This mod is a successor to Chisels & Bits by AlgorithmX2. After AlgorithmX2 decided to retire from minecraft modding in july of 2019 work on this successor for future versions of minecraft was started to fill the blocky bit-shaped gap it left behind.

This mod is currently in very early alpha phases. This means that here is currently no support for any other mods, including optifine. You can always report issues that you're having but compatbility with other mods is currently not the focus. This also means that the mod is currently not fully functional yet!

The current textures for the palette and bit beaker are placeholders, if anyone'd like to make some textures I'd love to use them!

You can find the original mod for Minecraft 1.8-1.12 by AlgorithmX2 at: https://github.com/AlgorithmX2/Chisels-and-Bits !

<br/> <br/>

#### Differences from the original mod
_Note: The development of C&B2 isn't complete yet, it's still in the alpha stages. Not all of the features listed below are fully functional yet._

The design goal for C&B2 is to create a worthy successor to the original C&B by offering at least the exact same featureset with only new or optional features added, excluding a few features that won't have any effect on the way the mod is played. (e.g. crafting recipes, whether or not the saw is diamond or iron, etc.) There'll also be a look at adding even more compatibility with other mods and performance improvements using the new tools provided in vanilla and forge in recent updates.

* No more tiered chisels, there's only one type of chisel now which can break any block that is supported to be chiseled. The saw is also no longer diamond.
* Patterns will be merged into a single item and they'll get some new functionality/modes, this is still TBD.
* Bit Bags are larger, each slot can store 32 blocks worth of bits. (131072 bits)
* New paint palette and paint buckets, this adds the functionality of the old Flat Colored Blocks mod, they allow you to freely color any bit to be any color. This will also mean that bits can support being an argb color or a blockstate, or a fluidstate.
* Internally the chiseled block is different, instead of there being different blocks for each material (rock, cloth, etc.) the chiseled block will mimic most of the functionality of the dominant block. (the original mod already did this to some extent, but I'd like to add to it by also mimicing tile entities that do not store any data, e.g. crafting table, enchantment table, anvil)
* Compatibility with the new multipart API in forge which will allow even more mods than ever before to be compatible with chisels & bits, for example being able to put power cables in the same space as C&B bits.
* Finishing C&B's original 'Project Blueprint': (https://github.com/AlgorithmX2/Chisels-and-Bits/issues/337)

<br/>

##### New behaviour of the Chisel item

_The biggest change to the way you use Chisels & Bits 2 is in the removal/replacement of bits as an item. In C&B1 you'd have a stack of bits in your inventory for every block you're building with and the bit bags would be ordinary GUI's that can store the bits._

Let's start with the chisel, left clicking still chisels a bit but right clicking with the chisel now places a bit.

Selecting which bit you want to place is done through the bags. Bags now have a radial menu where all the types of blocks inside of the bag are shown, in this radial menu you can select which type of bit you want to use.

You can also press the middle mouse button whilst holding a chisel to attempt to select the bit type of the block you're looking at. If any bag in your inventory contains the bit type you're looking at that bag will automatically select it.

Coloured bits won't go in the bit bags, instead the paint palette has a radial menu too where you can choose favourited/bookmarked colours.

Both the palette and bit bag will still have GUI's where you can manage which items are in which bag/where you can freely mix rgba colours and favourite them. (the blocks shown in the bags won't be real items and you can't have them in your inventory, but you can merge them into full blocks and take them out)

Fluids are stored in the Bit Beaker which works similar to the Bit Bag. You can put fluids into the Bit Beaker by scooping up fluids from the world. (no more spam clicking bit tanks)

<br/>
<br/>

##### Temporary Differences
The following features will be (re-)added eventually and are currently absent because this mod is still in the alpha stages.
* Chiseled Blocks giving off light equal to the percentage of glowing blocks used.
* More of the old configuration options
* The entire old API/addon and IMC (this is probably all going to be completely different)
* The various languages that the original mod supported
* The commands and debug features
* Cross-world model exporting
* Compatibility with worlds with C&B1 (this might not happen at all)
