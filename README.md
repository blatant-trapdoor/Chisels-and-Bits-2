# Chisels & Bits 2

This mod is a successor to Chisels & Bits by AlgorithmX2. After AlgorithmX2 decided to retire from minecraft modding in july of 2019 work on this successor for future versions of minecraft was started to fill the blocky bit-shaped gap it left behind.

This mod is currently in very early alpha phases. This means that here is currently no support for any other mods, including optifine. You can always report issues that you're having but compatbility with other mods is currently not the focus.

The current textures for the paintbrush and palette are placeholders, if anyone'd like to make some textures I'd love to use them!

<br/>
<br/>

#### Differences from the original mod (these are the planned differences, they are not all finished yet)
The design goal for C&B2 is to create a worthy successor to the original C&B by offering at least the exact same featureset with only new or optional features added, excluding a few features that won't have any effect on the way the mod is played. (e.g. crafting recipes, whether or not the saw is diamond or iron, etc.) There'll also be a look at adding even more compatibility with other mods and performance improvements using the new tools provided in vanilla and forge in recent updates.

* No more tiered chisels, there's only one type of chisel now which can break any block that is supported to be chiseled. The saw is also no longer diamond.
* Patterns will be merged into a single item and they'll get some new functionality/modes probably.
* Bit Bags are much larger, each slot can store 16 blocks worth of bits. (65536 bits)
* New paint palette and paint buckets, this adds the functionality of the old Flat Colored Blocks mod, they allow you to freely color any bit to be any color. This will also mean that bits can support being an argb color or a blockstate, or a fluidstate.
* Paintbrush item which allows you to easily replace the top layer of a wall block into a colored bit giving you a painted wall.
* Internally the chiseled block is different, instead of there being different blocks for each material (rock, cloth, etc.) the chiseled block will mimic most of the functionality of the dominant block. (the original mod already did this to some extent, but I'd like to add to it by also mimicing tile entities that do not store any data, e.g. crafting table, enchantment table, anvil)
* Compatibility with the new multipart API in forge which will allow even more mods than ever before to be compatible with chisels & bits, for example being able to put power cables in the same space as C&B bits.
* Finishing C&B's original 'Project Blueprint': (https://github.com/AlgorithmX2/Chisels-and-Bits/issues/337)

<br/>

* Bits as an item will probably no longer exist, you can select the block you want to use by right clicking with a chisel or in the bit bag. The way coloured and fluid bits will likely be implemented makes it hard to add bit items for those. Aditionally, adding a new item for each block is also not really in line with the flattening that happened in 1.13 where item damage values were removed. I'm still looking into the best way to create these bit items and I'll update this list when I've got the plans finalised.

<br/>
<br/>

##### Temporary Differences
The mod has been partially rewritten, 99% of the rendering code is still the same though, (because AlgorithmX2s rendering code is both already amazing and better than what I could make) so there may be minor changes to various parts of the mod. More importantly, because of the rewrite not all features have been ported yet, but they will be ported eventually. These features include:
* Liquid Bits (will be re-added after the forge liquid API is done)
* Chiseled Blocks giving off light equal to the percentage of glowing blocks used.
* Most of the configuration options
* The entire old API/addon and IMC (this is probably all going to be completely different)
* The various languages that the original mod supported
* The commands and debug features
* Cross-world model exporting
* Compatibility with worlds with C&B1 (this might not happen at all)