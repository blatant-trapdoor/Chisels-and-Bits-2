# Chisels & Bits 2

This mod is a successor to Chisels & Bits by AlgorithmX2. After AlgorithmX2 decided to retire from minecraft modding in july of 2019 work on this successor for future versions of minecraft was started to fill the blocky bit-shaped gap it left behind.

This mod is currently in very early alpha phases. This means that here is currently no support for any other mods, including optifine. You can always report issues that you're having but compatbility with other mods is currently not the focus.

Note: The current textures for the paintbrush and palette aren't that pretty and I'd like to replace them. If anyone'd like to make some pretty textures that fit the original mod I'd love to use them.

<br/>
<br/>

#### Differences from the original mod (these are the planned differences, they are not all finished yet)
* No more tiered chisels, there's only one type of chisel now which can break any block that is supported to be chiseled. The saw is also no longer diamond.
* Patterns will be merged into a single item and they'll get some new functionality/modes probably.
* Bit Bags are much larger, each slot can store 16 blocks worth of bits. (65536 bits)
* New paint palette and paint buckets, this adds the functionality of the old Flat Colored Blocks mod, they allow you to freely color any bit to be any color. This will also mean that bits can support being an argb color or a blockstate, or a fluidstate.
* Paintbrush item which allows you to easily replace the top layer of a wall block into a colored bit giving you a painted wall.
* Bits as an item no longer exist, you can select the block you want to use by right clicking with a chisel or in the bit bag.
* Internally the chiseled block is different, instead of there being different blocks for each material (rock, cloth, etc.) the chiseled block will mimic most of the functionality of the dominant block. (the original mod already did this to some extent, but I'd like to add to it by also mimicing tile entities that do not store any data, e.g. crafting table, enchantment table, anvil)
* Compatibility with the new multipart API in forge which will allow even more mods than ever before to be compatible with chisels & bits, for example being able to put power cables in the same space as C&B bits.

<br/>
<br/>

##### Temporary Differences
The mod has been partially rewritten, 99% of the rendering code is still the same though, (because AlgorithmX2s rendering code is both already amazing and better than what I could make) so there may be minor changes to various parts of the mod. More importantly, because of the rewrite not all features have been ported yet, but they will be ported eventually. These features include:
* Bit Saw
* Patterns
* Colored Bit Bags
* Wrench
* Liquid Bits (will be re-added after the forge liquid API is done)
* Chiseled Blocks giving off light equal to the percentage of glowing blocks used.
* Most of the configuration options
* The entire old API/addon and IMC (this is probably all going to be completely different)
* The various languages that the original mod supported
* The commands and debug features
* Cross-world model exporting
* Compatibility with worlds with C&B1 (this might not happen at all)