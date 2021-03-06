package net.afterlifelochie.fontbox.document;

import net.afterlifelochie.fontbox.api.data.IBookProperties;
import net.afterlifelochie.fontbox.api.formatting.layout.AlignmentMode;
import net.afterlifelochie.fontbox.api.formatting.layout.FloatMode;
import net.afterlifelochie.fontbox.api.layout.IIndexed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Minecraft item stacks as images.
 *
 * @author AfterLifeLochie
 */
public class ImageItemStack extends Image {
    /**
     * The item stack
     */
    public ItemStack stack;

    /**
     * Creates a new inline item-stack image with the properties specified.
     *
     * @param source The item stack, may not be null.
     * @param width  The width of the image.
     * @param height The height of the image.
     */
    public ImageItemStack(ItemStack source, int width, int height) {
        this(source, width, height, AlignmentMode.LEFT, FloatMode.NONE);
    }

    /**
     * Creates a new inline item-stack image with the properties specified.
     *
     * @param source The item stack, may not be null.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @param align  The alignment of the image.
     */
    public ImageItemStack(ItemStack source, int width, int height, AlignmentMode align) {
        this(source, width, height, align, FloatMode.NONE);
    }

    /**
     * Creates a new floating item-stack image with the properties specified.
     *
     * @param source   The item stack, may not be null.
     * @param width    The width of the image.
     * @param height   The height of the image.
     * @param floating The floating mode.
     */
    public ImageItemStack(ItemStack source, int width, int height, FloatMode floating) {
        this(source, width, height, AlignmentMode.LEFT, floating);
    }

    /**
     * Creates a new item-stack image with the properties specified.
     *
     * @param source   The image source location, may not be null.
     * @param width    The width of the image.
     * @param height   The height of the image.
     * @param align    The alignment of the image.
     * @param floating The floating mode.
     */
    public ImageItemStack(ItemStack source, int width, int height, AlignmentMode align, FloatMode floating) {
        super(null, width, height, align, floating);
        stack = source;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void update() {
        /* No action required */
    }

    @Override
    public boolean canCompileRender() {
        /* No, because glint effects */
        return false;
    }

    @Override
    public void render(GuiScreen gui, int mx, int my, float frame) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate(bounds().x * IBookProperties.SCALE, bounds().y * IBookProperties.SCALE, 0);
        GlStateManager.scale(bounds().width * IBookProperties.SCALE / 16.0f, bounds().height * IBookProperties.SCALE / 16.0f, 1.0f);
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void clicked(IIndexed gui, int mx, int my) {
        /* No action required */
    }

    @Override
    public void typed(GuiScreen gui, char val, int code) {
		/* No action required */
    }

    @Override
    public void hover(GuiScreen gui, int mx, int my) {
        List<String> lines = stack.getTooltip(Minecraft.getMinecraft().player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        if (lines != null && !lines.isEmpty()) {
            gui.drawHoveringText(lines, mx, my);
        }
    }
}
