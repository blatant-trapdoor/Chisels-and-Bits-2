package nl.dgoossens.chiselsandbits2.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Variant of {@link ItemRenderer} with support for transparent items and rendering separate
 * transparent durability bars with custom percentages.
 *
 * Items can't actually be rendered transparent (as far as I know) unless we're gonna redo a bunch of forge logic too.
 */
@OnlyIn(Dist.CLIENT)
public class CustomItemRenderer {
    /**
     * The base item renderer.
     */
    private ItemRenderer parent;

    //Stolen from ItemRenderer.
    private ItemColors itemColors;
    private TextureManager textureManager;

    public CustomItemRenderer(ItemRenderer parent) {
        this.parent = parent;

        try {
            //Private's just a suggestion anyways...
            Field a = ItemRenderer.class.getDeclaredField("itemColors");
            a.setAccessible(true);
            itemColors = (ItemColors) a.get(parent);

            Field b = ItemRenderer.class.getDeclaredField("textureManager");
            b.setAccessible(true);
            textureManager = (TextureManager) b.get(parent);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    private float alpha = 1.0f;

    public void setAlpha(float a) {
        alpha = a;
    }

    public ItemColors getItemColors() {
        return itemColors;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    private void renderModel(IBakedModel model, ItemStack stack) {
        this.renderModel(model, -1, stack);
    }

    private void renderModel(IBakedModel model, int color) {
        this.renderModel(model, color, ItemStack.EMPTY);
    }

    private void renderModel(IBakedModel model, int color, ItemStack stack) {
        if (net.minecraftforge.common.ForgeConfig.CLIENT.allowEmissiveItems.get()) {
            net.minecraftforge.client.ForgeHooksClient.renderLitItem(parent, model, color, stack);
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
        Random random = new Random();
        long i = 42L;

        for(Direction direction : Direction.values()) {
            random.setSeed(42L);
            this.renderQuads(bufferbuilder, model.getQuads((BlockState)null, direction, random), color, stack);
        }

        random.setSeed(42L);
        this.renderQuads(bufferbuilder, model.getQuads((BlockState)null, (Direction)null, random), color, stack);
        tessellator.draw();
    }

    public void renderItem(ItemStack stack, IBakedModel model) {
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
            if (model.isBuiltInRenderer()) {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
                GlStateManager.enableRescaleNormal();
                stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
            } else {
                this.renderModel(model, stack);
                if (stack.hasEffect()) {
                    ItemRenderer.renderEffect(getTextureManager(), () -> {
                        this.renderModel(model, -8372020);
                    }, 8);
                }
            }

            GlStateManager.popMatrix();
        }
    }

    public void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack) {
        boolean flag = color == -1 && !stack.isEmpty();
        int i = 0;

        for(int j = quads.size(); i < j; ++i) {
            BakedQuad bakedquad = quads.get(i);
            int k = color;
            if (flag && bakedquad.hasTintIndex()) {
                k = getItemColors().getColor(stack, bakedquad.getTintIndex());
                k = k | -16777216;
            }

            net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(renderer, bakedquad, k);
        }

    }

    public IBakedModel getItemModelWithOverrides(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entitylivingbaseIn) {
        IBakedModel ibakedmodel = parent.getItemModelMesher().getItemModel(stack);
        Item item = stack.getItem();
        return !item.hasCustomProperties() ? ibakedmodel : this.getModelWithOverrides(ibakedmodel, stack, worldIn, entitylivingbaseIn);
    }

    public IBakedModel getModelWithOverrides(ItemStack stack) {
        return this.getItemModelWithOverrides(stack, (World)null, (LivingEntity)null);
    }

    private IBakedModel getModelWithOverrides(IBakedModel model, ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
        IBakedModel ibakedmodel = model.getOverrides().getModelWithOverrides(model, stack, worldIn, entityIn);
        return ibakedmodel == null ? parent.getItemModelMesher().getModelManager().getMissingModel() : ibakedmodel;
    }

    public void renderItemIntoGUI(ItemStack stack, int x, int y) {
        this.renderItemModelIntoGUI(stack, x, y, this.getModelWithOverrides(stack));
    }

    protected void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel) {
        GlStateManager.pushMatrix();
        getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
        this.setupGuiTransform(x, y, bakedmodel.isGui3d());
        bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
        this.renderItem(stack, bakedmodel);
        GlStateManager.popMatrix();
        getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d) {
        GlStateManager.translatef((float)xPosition, (float)yPosition, 100.0F + parent.zLevel);
        GlStateManager.translatef(8.0F, 8.0F, 0.0F);
        GlStateManager.scalef(1.0F, -1.0F, 1.0F);
        GlStateManager.scalef(16.0F, 16.0F, 16.0F);
        if (isGui3d) {
            GlStateManager.enableLighting();
        } else {
            GlStateManager.disableLighting();
        }

    }

    public void renderDurabilityBar(double damage, double maxDamage, int xPosition, int yPosition) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double health = damage / maxDamage;
        int i = Math.round(13.0F - (float)health * 13.0F);
        int j = MathHelper.hsvToRGB(Math.max(0.0F, (float) (1.0F - health)) / 3.0F, 1.0F, 1.0F);
        this.draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0);
        this.draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255);

        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
    }

    //Draw with alpha support.
    private void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        int a = Math.round(alpha*255);
        renderer.pos((double)(x + 0), (double)(y + 0), 0.0D).color(red, green, blue, a).endVertex();
        renderer.pos((double)(x + 0), (double)(y + height), 0.0D).color(red, green, blue, a).endVertex();
        renderer.pos((double)(x + width), (double)(y + height), 0.0D).color(red, green, blue, a).endVertex();
        renderer.pos((double)(x + width), (double)(y + 0), 0.0D).color(red, green, blue, a).endVertex();
        Tessellator.getInstance().draw();
    }
}