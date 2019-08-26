package nl.dgoossens.chiselsandbits2.client.render.models.helpers;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.util.Arrays;

public class ModelReader extends BaseModelReader {
    float pos[];
    float uv[];

    @Override
    public void put(int element, float... data) {
        final VertexFormat format = getVertexFormat();
        final VertexFormatElement ele = format.getElement(element);

        if(ele.getUsage() == VertexFormatElement.Usage.UV && ele.getIndex() != 1)
            uv = Arrays.copyOf(data, data.length);
        else if(ele.getUsage() == VertexFormatElement.Usage.POSITION)
            pos = Arrays.copyOf(data, data.length);
    }
}
