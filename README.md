# Chisels & Bits 2

This mod is a successor to Chisels & Bits by AlgorithmX2. After AlgorithmX2 decided to retire from minecraft modding in july of 2019 work on this successor was started to fill the blocky bit-shaped gap it left behind for future versions of minecraft.

<br/>
<br/>

#### Differences from the original mod (these are the planned differences, they are not all finished yet)
* No more tiered chisels, there's only one type of chisel now which can break any block that is supported to be chiseled.
* Patterns are now merged into one pattern item that can be toggled between positive or negative. There are also some new modes, shape mode to only copy the shape and not the materials and exchanger pattern where you can swap the material of a chiseled block.
* Bit Bags are much larger, each slot can store 16 blocks worth of bits. (65536 bits)
* New paint palette and paint buckets, this adds the functionality of the old Flat Colored Blocks mod, they allow you to freely color any bit to be any color.
* Paintbrush item which allows you to easily replace the top layer of a wall block into a colored bit giving you a painted wall.
* Bits as an item no longer exist, you can select the block you want to use by right clicking with a chisel or in the bit bag.
* Internally the chiseled block is different, instead of there being different blocks for each material (rock, cloth, etc.) the chiseled block will mimic most of the functionality of the dominant block.
* Compatibility with the new multipart API in forge which will allow even more mods than ever before to be compatible with chisels & bits, for example being able to put power cables in the same space as C&B bits.

<br/>
<br/>

##### Temporary Differences
The mod has also been rewritten from scratch, (although with major parts being copied over from the original mod) so there may be minor changes to various parts of the mod. More importantly, because of the rewrite not all features have been ported yet, but they will be ported eventually. These features include:
* Bit Saw
* Colored Bit Bags
* Wrench
* Mirrored Chisel Designs
* Liquid Bits (will be re-added after the forge liquid API is done)
* Most of the customisability, API and configuration options.
* The various languages that the original mod supported
* The commands and debug features