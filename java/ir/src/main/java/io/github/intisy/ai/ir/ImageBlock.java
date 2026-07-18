package io.github.intisy.ai.ir;

/** Image content block: either inline base64 {@code data} or a {@code url}, not both. */
public final class ImageBlock extends Block {
    public String mediaType;
    public String data;
    public String url;

    public ImageBlock() {
        super(BlockKind.IMAGE);
    }
}
