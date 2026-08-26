package io.github.intisy.ai.ir;

/** Image content block: either inline base64 {@code data} or a {@code url}, not both. */
public final class ImageBlock extends Block {
    /** The image's MIME type, e.g. {@code image/png}. */
    public String mediaType;
    /** Base64-encoded image bytes, or null when {@link #url} is set instead. */
    public String data;
    /** A URL the image can be fetched from, or null when {@link #data} is set instead. */
    public String url;

    /** Creates an image block with no media set yet; the caller fills {@link #mediaType} and either {@link #data} or {@link #url}. */
    public ImageBlock() {
        super(BlockKind.IMAGE);
    }
}
