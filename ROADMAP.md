# Roadmap
Chisels & Bits 2 is still actively being developed, here follows the roadmap for features to be added in each alpha/beta. If you have feedback for features that you feel should be higher priority and added sooner, make an issue and let me know!

Firstly, a general note on which minecraft version will be developed against. The answer is simple: the newest version available. C&B2 won't be backported to older versions and will be updated to newer versions whenever possible. Specifically for 1.15 I will be waiting for an alpha or two to see what version Forge decides to support long-term. If Forge decides to stay on 1.14 until 1.16 comes out I'll keep C&B2 on 1.14 until the first betas are done so there is a version of C&B2 available for 1.14 modpacks.

The alphas and betas will be released to curseforge whenever they are stable enough to be used in modpacks. Snapshots might be released through github releases whenever there are no obvious bugs or crashes.

## Alphas
Here's a rough outline of which features need to be added, all features need adding eventually but they have been ordered in terms of importance and grouped together in a release by similarity or code being shared.

#### Alpha 4
Alpha 4 will focus on finishing the behaviour of all current features.
- Adding placement code for the chiseled block to allow merging two chiseled blocks, off-grid placement and more.
- Finishing the code behind the undo/redo buttons that were added in alpha 3.
- Adding rotation to the chiseled block
- Adding the wrench; as it also uses the rotation code
- Item descriptions in JEI
- Rewriting the way rendering tasks are distributed and when blocks are re-rendered
- Full rewrite to the way the chisel puts chiseled bits into bags and determines durability
- Allow other mods to make custom menu actions and item modes for existing items or their own items

#### Alpha 5
Alpha 5 is planned to address all currently standing issues that hold the mod back from being usable by everyone.
- Finalise the way chiseled blocks are stored to disk, currently I use the exact C&B1 method but I want to see if there is room for improvement
- Compatiblity with optifine (at least fixing major issues)
- Multiplayer support for the mod
- Finalising the Worldfixer program which can convert 1.12 worlds to 1.14 C&B2.

### Alpha 6
Alpha 6 will include the last few rendering things that need to get sorted out before the mod can be used in modpacks properly.
- Finishing proper light level rendering and dynamically updating light level of chiseled blocks to the amount of glowing blocks contained in it
- Looking at optimization of the block shape (bounding box) and isFullCube method.
- Revamping the model generation code as that is still largely copied from C&B1 without having been looked at.
- Bugfixes, stability improvements and lots of testing

## CurseForge Releases
At this point releases will be published to curseforge where I'll start releasing them under the versioning scheme <a|b|r>release.update.fix where a (alpha), b (beta) and r (release) will correspond to how the update is marked on curseforge.

#### a0.7.0
Slightly updated version of alpha 6 with better stability, performance and many bugfixes.

#### a0.8.0
Beta 2 will reintroduce the pattern and all features it brings with.
- Add patterns including all modes like the mirror pattern and the negative pattern
- The pattern binder, a large book in which you can store all your patterns easily.
- Pattern Table; a block similar to the cartography table to fulfill a couple features relating to patterns that fit better in a custom block, like mirroring the pattern, merging patterns or building chiseled blocks from a pattern.
- (this is a concept I'm not sure about yet) Chisel Designs that can be used to make preset shapes out of any material. Like a stair design, wall design, fence design, slab design, vertical slab design, etc.


#### b0.9.0
Beta 9 will add the bit saw and scoop. Two tools for turning one chiseled block into multiple.
* The saw allows you to slice a chiseled block in half over the axis at the height you clicked the block. The top/right side of your point of division will be dropped as a chiseled item to place elsewhere.
* The bit scoop has a planar drawn region selector that will remove bits inside the selection and turn them into a chiseled block that is dropped on the ground. The scoop has a custom radial menu to select scooping depth.
* A block in which you can perform many modifications to chiseled blocks like merging them, duplicating them from bits in the bag.
* (not sure about this one) A bit exchanger with which you can click on a bit type and all bits of that type in that chiseled block are swapped with the bit type you have selected. Allows for easy material swapping.

#### b0.10.0
Beta 10 will add the remaining bit storages allowing you to use both liquids and coloured bits in your builds.
- Adding the bit beaker; including adding full functionality to liquid bits
- Adding the palette; including adding transparency to bits
- GUIs for the bit bag, beaker and palette.

#### b0.11.0
Beta 11 will get blueprints added and maybe a prototype of the workshop.
- The blueprint, a new item that allows you to store large areas up to 16x16x16 in contrast to the pattern that can only store single blocks.
- The workshop with which you can upload blueprints or even chiseled armor sets depending on if/when Extra Bit Manipulation is updated.
- A workbench where you can view downloaded blueprints and print them onto a blueprint.

#### b0.12.0
Beta 12 consists of features that will be added to betas eventually but have such a low priority that they are going to get added last.
- Allow placing levers, pressure plates, tripwire hooks, etc. onto the sides of chiseled blocks where appropriate.
- More statistics for tracking various statistics like special operations (rotation, undo/redo) performed, patterns made or blueprints downloaded
- More configuration options
- Support for more languages

### Release (r1.0.0)
After all features listed above the first full release of C&B2 will happen at which point a proper vesrioning scheme will start. I have some ideas for possible updates to happen post-release and functional blocks are their own debate, but the release can be considered feature complete. And regardless of how the time I am able to spend on mod development changes I will keep at it until at least this release is done. At which point content updates might slow down considerably.

#### Functional Blocks
After everything mentioned above has been added I will start to look into what will be added for the rest of time, functional blocks. This includes custom seats, ladders, pistons, bit-sized redstone, custom/directional light emitters and everything else that would make chiseled blocks more than just decoration. Though I might never add functional blocks at all. It all depends on on the state of the mod by the time the rest of this roadmap has been completed.