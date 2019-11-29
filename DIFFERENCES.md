# Differences from the original mod

Chisels & Bits 2 is actively being developed and currently unfinished. The following features have temporarily been removed, they'll be re-added in the future:
* Chiseled Blocks giving off light equal to the percentage of glowing blocks used.
* Configuration options
* API for other mods including IMC
* Support for various languages
* Cross-world model exporting
* Compatibility with worlds with C&B1

The following features were added/changed in Chisels & Bits 2:
* No more tiered chisels, there's only one type of chisel now which can break any block that is supported to be chiseled.
* The saw is now golden instead of diamond and the wrench/mallet are iron now.
* Bit Bags have less slots but each slot can now store 32 blocks. (131072 bits)
* New paint palette, this adds the functionality of the old separate Flat Colored Blocks mod, they allow you to freely color any bit to be any color.
* New bit beaker which will replace the Bit Tank, the beaker can scoop up any fluid and place the fluids in chiseled blocks.
* Bits are now no longer separate items for each block type, instead there's now a single "Morphing Bit" which can take on the name and texture of any chiselable block. (more information later)


* (originally suggested by weeryan17) A pattern book in which you can easily store your patterns and cycle between them.

And the following features are planned but not implemented in any way yet:
* Finishing C&B's original 'Project Blueprint': (https://github.com/AlgorithmX2/Chisels-and-Bits/issues/337)
* Patterns will be merged into a single item and they'll get some new functionality/modes.

But there's still one really big change...
<br/>

### New behaviour of the chisel

_The biggest change to the way you use Chisels & Bits 2 is in the removal/replacement of normal bits as an item. (though the morphing bit still exists) In C&B1 you'd have a stack of bits in your inventory for every block you're building with and the bit bags would be ordinary GUI's that can store the bits._

Let's start with the chisel, left clicking still chisels a bit but right clicking with the chisel now places a bit.

Selecting which bit you want to place is done through the bags. Bags now have a radial menu where all the types of blocks inside of the bag are shown, in this radial menu you can select which type of bit you want to use.

You can also press the middle mouse button whilst holding a chisel to attempt to select the bit type of the block you're looking at. If any bag in your inventory contains the bit type you're looking at that bag will automatically select it.

Coloured bits won't go in the bit bags, instead the paint palette has a radial menu too where you can choose favourited/bookmarked colours.

Fluids are stored in the Bit Beaker which works similar to the Bit Bag. You can put fluids into the Bit Beaker by scooping up fluids from the world. (no more spam clicking bit tanks)

The palette will get a GUI where you can freely mix colours and bookmark/favourite the colours you want to use. The bit bag (and beaker) will have GUIs to allow you to extract the blocks or reorganise them.

<br/>