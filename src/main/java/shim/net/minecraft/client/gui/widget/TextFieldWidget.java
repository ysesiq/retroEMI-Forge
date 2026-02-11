package shim.net.minecraft.client.gui.widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.emi.emi.input.EmiInput;
import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import shim.net.minecraft.client.gui.Drawable;
import shim.net.minecraft.text.OrderedText;
import shim.net.minecraft.text.Text;
import shim.org.lwjgl.glfw.GLFW;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL11.*;

@SideOnly(Side.CLIENT)
public class TextFieldWidget extends ClickableWidget implements Drawable {
	private static final String HORIZONTAL_CURSOR = "_";
	public static final int DEFAULT_EDITABLE_COLOR = 0xE0E0E0;
	private final FontRenderer textRenderer;
	private String text = "";
	private int maxLength = 32;
	private int focusedTicks;
	private boolean drawsBackground = true;
	private boolean focusUnlocked = true;
	private boolean editable = true;
	private boolean selecting;
	/**
	 * The index of the leftmost character that is rendered on a screen.
	 */
	private int firstCharacterIndex;
	private int selectionStart;
	private int selectionEnd;
	private int editableColor = 0xE0E0E0;
	private int uneditableColor = 0x707070;
	@Nullable
	private String suggestion;
	@Nullable
	private Consumer<String> changedListener;
	private Predicate<String> textPredicate = Objects::nonNull;
	private BiFunction<String, Integer, OrderedText> renderTextProvider = (string, firstCharacterIndex) -> Text.literal(string).asOrderedText();
	@Nullable
	private Text placeholder;
    private int frameColor = 0;
    protected Minecraft client = Minecraft.getMinecraft();

	public TextFieldWidget(FontRenderer textRenderer, int x, int y, int width, int height, Text text) {
		this(textRenderer, x, y, width, height, null, text);
	}

	public TextFieldWidget(FontRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
		super(x, y, width, height, text);
		this.textRenderer = textRenderer;
		if (copyFrom != null) {
			this.setText(copyFrom.getText());
		}
	}

	public void setChangedListener(Consumer<String> changedListener) {
		this.changedListener = changedListener;
	}

	public void setRenderTextProvider(BiFunction<String, Integer, OrderedText> renderTextProvider) {
		this.renderTextProvider = renderTextProvider;
	}

	public void tick() {
		++this.focusedTicks;
	}

	public void setText(String text) {
		if (!this.textPredicate.test(text)) {
			return;
		}
		this.text = text.length() > this.maxLength ? text.substring(0, this.maxLength) : text;
		this.setCursorToEnd();
		this.setSelectionEnd(this.selectionStart);
		this.onChanged(text);
	}

	public String getText() {
		return this.text;
	}

	public String getSelectedText() {
		int i = Math.min(this.selectionStart, this.selectionEnd);
		int j = Math.max(this.selectionStart, this.selectionEnd);
		return this.text.substring(i, j);
	}

	public void setTextPredicate(Predicate<String> textPredicate) {
		this.textPredicate = textPredicate;
	}

	public void write(String text) {
		String string2;
		String string;
		int l;
		int i = Math.min(this.selectionStart, this.selectionEnd);
		int j = Math.max(this.selectionStart, this.selectionEnd);
		int k = this.maxLength - this.text.length() - (i - j);
		if (k < (l = (string = text.replaceAll("§.", "")).length())) {
			string = string.substring(0, k);
			l = k;
		}
		if (!this.textPredicate.test(string2 = new StringBuilder(this.text).replace(i, j, string).toString())) {
			return;
		}
		this.text = string2;
		this.setSelectionStart(i + l);
		this.setSelectionEnd(this.selectionStart);
		this.onChanged(this.text);
	}

	private void onChanged(String newText) {
		if (this.changedListener != null) {
			this.changedListener.accept(newText);
		}
	}

	private void erase(int offset) {
		if (EmiInput.isControlDown()) {
			this.eraseWords(offset);
		} else {
			this.eraseCharacters(offset);
		}
	}

	public void eraseWords(int wordOffset) {
		if (this.text.isEmpty()) {
			return;
		}
		if (this.selectionEnd != this.selectionStart) {
			this.write("");
			return;
		}
		this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
	}

	public void eraseCharacters(int characterOffset) {
		int k;
		if (this.text.isEmpty()) {
			return;
		}
		if (this.selectionEnd != this.selectionStart) {
			this.write("");
			return;
		}
		int i = this.getCursorPosWithOffset(characterOffset);
		int j = Math.min(i, this.selectionStart);
		if (j == (k = Math.max(i, this.selectionStart))) {
			return;
		}
		String string = new StringBuilder(this.text).delete(j, k).toString();
		if (!this.textPredicate.test(string)) {
			return;
		}
		this.text = string;
		this.setCursor(j);
	}

	public int getWordSkipPosition(int wordOffset) {
		return this.getWordSkipPosition(wordOffset, this.getCursor());
	}

	private int getWordSkipPosition(int wordOffset, int cursorPosition) {
		return this.getWordSkipPosition(wordOffset, cursorPosition, true);
	}

	private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
		int i = cursorPosition;
		boolean bl = wordOffset < 0;
		int j = Math.abs(wordOffset);
		for (int k = 0; k < j; ++k) {
			if (bl) {
				while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
					--i;
				}
				while (i > 0 && this.text.charAt(i - 1) != ' ') {
					--i;
				}
				continue;
			}
			int l = this.text.length();
			if ((i = this.text.indexOf(32, i)) == -1) {
				i = l;
				continue;
			}
			while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
				++i;
			}
		}
		return i;
	}

	public void moveCursor(int offset) {
		this.setCursor(this.getCursorPosWithOffset(offset));
	}

	private int getCursorPosWithOffset(int offset) {
		return moveCursor(this.text, this.selectionStart, offset);
	}

	/**
	 * Moves the {@code cursor} in the {@code string} by a {@code delta} amount. Skips surrogate
	 * characters.
	 */
	public static int moveCursor(String string, int cursor, int delta) {
		int i = string.length();
		if (delta >= 0) {
			for (int j = 0; cursor < i && j < delta; ++j) {
				if (!Character.isHighSurrogate(string.charAt(cursor++)) || cursor >= i || !Character.isLowSurrogate(string.charAt(cursor)))
					continue;
				++cursor;
			}
		} else {
			for (int j = delta; cursor > 0 && j < 0; ++j) {
				if (!Character.isLowSurrogate(string.charAt(--cursor)) || cursor <= 0 || !Character.isHighSurrogate(string.charAt(cursor - 1)))
					continue;
				--cursor;
			}
		}
		return cursor;
	}

	public void setCursor(int cursor) {
		this.setSelectionStart(cursor);
		if (!this.selecting) {
			this.setSelectionEnd(this.selectionStart);
		}
		this.onChanged(this.text);
	}

	public void setSelectionStart(int cursor) {
		this.selectionStart = MathHelper.clamp_int(cursor, 0, this.text.length());
	}

	public void setCursorToStart() {
		this.setCursor(0);
	}

	public void setCursorToEnd() {
		this.setCursor(this.text.length());
	}

	public static boolean isCut(int code) {
		return code == Keyboard.KEY_X && EmiInput.isControlDown() && !EmiInput.isShiftDown() && !EmiInput.isAltDown();
	}

	public static boolean isPaste(int code) {
		return code == Keyboard.KEY_V && EmiInput.isControlDown() && !EmiInput.isShiftDown() && !EmiInput.isAltDown();
	}

	public static boolean isCopy(int code) {
		return code == Keyboard.KEY_C && EmiInput.isControlDown() && !EmiInput.isShiftDown() && !EmiInput.isAltDown();
	}

	public static boolean isSelectAll(int code) {
		return code == Keyboard.KEY_A && EmiInput.isControlDown() && !EmiInput.isShiftDown() && !EmiInput.isAltDown();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!this.isActive()) {
			return false;
		}
		this.selecting = EmiInput.isShiftDown();
		if (isSelectAll(keyCode)) {
			this.setCursorToEnd();
			this.setSelectionEnd(0);
			return true;
		}
		if (isCopy(keyCode)) {
			GuiScreen.setClipboardString(this.getSelectedText());
			return true;
		}
		if (isPaste(keyCode)) {
			if (this.editable) {
				this.write(GuiScreen.getClipboardString());
			}
			return true;
		}
		if (isCut(keyCode)) {
			GuiScreen.setClipboardString(this.getSelectedText());
			if (this.editable) {
				this.write("");
			}
			return true;
		}
        return switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                if (EmiInput.isControlDown()) {
                    this.setCursor(this.getWordSkipPosition(-1));
                } else {
                    this.moveCursor(-1);
                }
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (EmiInput.isControlDown()) {
                    this.setCursor(this.getWordSkipPosition(1));
                } else {
                    this.moveCursor(1);
                }
                yield true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (this.editable) {
                    this.selecting = false;
                    this.erase(-1);
                    this.selecting = EmiInput.isShiftDown();
                }
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (this.editable) {
                    this.selecting = false;
                    this.erase(1);
                    this.selecting = EmiInput.isShiftDown();
                }
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                this.setCursorToStart();
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                this.setCursorToEnd();
                yield true;
            }
            default -> false;
        };
    }

	public boolean isActive() {
		return this.isVisible() && this.isFocused() && this.isEditable();
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (!this.isActive()) {
			return false;
		}
		if (this.editable) {
			this.write(Character.toString(chr));
		}
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean bl;
		if (!this.isVisible() || button != 0) {
			return false;
		}
		boolean bl2 = bl = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
		if (this.focusUnlocked) {
			this.setFocused(bl);
		}
		if (this.isFocused() && bl && button == 0) {
			int i = MathHelper.floor_double(mouseX) - this.getX();
			if (this.drawsBackground) {
				i -= 4;
			}
			String string = this.textRenderer.trimStringToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
			this.setCursor(this.textRenderer.trimStringToWidth(string, i).length() + this.firstCharacterIndex);
			return true;
		}
		return false;
	}

	@Override
	public void renderWidget(DrawContext raw, int mouseX, int mouseY, float delta) {
		int color;
		if (!this.isVisible()) {
			return;
		}
		if (this.drawsBackground()) {
            color = this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0;
            color = this.frameColor == 0 ? color : this.frameColor;
            drawRect(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);
            drawRect(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF000000);
		}
		color = this.editable ? this.editableColor : this.uneditableColor;
		int j = this.selectionStart - this.firstCharacterIndex;
		int k = this.selectionEnd - this.firstCharacterIndex;
		String string = this.textRenderer.trimStringToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
		boolean bl = j >= 0 && j <= string.length();
		boolean bl2 = this.isFocused() && this.focusedTicks / 6 % 2 == 0 && bl;
		int l = this.drawsBackground ? this.getX() + 4 : this.getX();
		int m = this.drawsBackground ? this.getY() + (this.height - 8) / 2 : this.getY();
		int n = l;
		if (k > string.length()) {
			k = string.length();
		}
		if (!string.isEmpty()) {
			String string2 = bl ? string.substring(0, j) : string;
			n = this.textRenderer.drawStringWithShadow(this.renderTextProvider.apply(string2, this.firstCharacterIndex).asString(), n, m, color);
		}
		boolean bl3 = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
		int o = n;
		if (!bl) {
			o = j > 0 ? l + this.width : l;
		} else if (bl3) {
			--o;
			--n;
		}
		if (!string.isEmpty() && bl && j < string.length()) {
			this.textRenderer.drawStringWithShadow(this.renderTextProvider.apply(string.substring(j), this.selectionStart).asString(), n, m, color);
		}
		if (this.placeholder != null && string.isEmpty() && !this.isFocused()) {
			this.textRenderer.drawStringWithShadow(this.placeholder.asString(), n, m, color);
		}
		if (!bl3 && this.suggestion != null) {
			this.textRenderer.drawStringWithShadow(this.suggestion, (o - 1), m, 0xFF808080);
		}
		if (bl2) {
			if (bl3) {
				drawRect(o, m - 1, o + 1, m + 1 + this.textRenderer.FONT_HEIGHT, 0xFFD0D0D0);
			} else {
				this.textRenderer.drawStringWithShadow(HORIZONTAL_CURSOR, o, m, color);
			}
		}
		if (k != j) {
			int p = l + this.textRenderer.getStringWidth(string.substring(0, k));
			this.drawSelectionHighlight(raw, o, m - 1, p - 1, m + 1 + this.textRenderer.FONT_HEIGHT);
		}
	}

	private void drawSelectionHighlight(DrawContext raw, int x1, int y1, int x2, int y2) {
		int i;
		if (x1 < x2) {
			i = x1;
			x1 = x2;
			x2 = i;
		}
		if (y1 < y2) {
			i = y1;
			y1 = y2;
			y2 = i;
		}
		if (x2 > this.getX() + this.width) {
			x2 = this.getX() + this.width;
		}
		if (x1 > this.getX() + this.width) {
			x1 = this.getX() + this.width;
		}
		glEnable(GL_COLOR_LOGIC_OP);
		glLogicOp(GL_OR_REVERSE);
		drawRect(x1, y1, x2, y2, -16776961);
		glDisable(GL_COLOR_LOGIC_OP);
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		if (this.text.length() > maxLength) {
			this.text = this.text.substring(0, maxLength);
			this.onChanged(this.text);
		}
	}

	private int getMaxLength() {
		return this.maxLength;
	}

	public int getCursor() {
		return this.selectionStart;
	}

	private boolean drawsBackground() {
		return this.drawsBackground;
	}

	public void setDrawsBackground(boolean drawsBackground) {
		this.drawsBackground = drawsBackground;
	}

	public void setEditableColor(int editableColor) {
		this.editableColor = editableColor;
	}

	public void setUneditableColor(int uneditableColor) {
		this.uneditableColor = uneditableColor;
	}

    public void setFrameColor(int frameColor) {
		this.frameColor = frameColor;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.visible && mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
	}

	@Override
	public void setFocused(boolean focused) {
		if (!this.focusUnlocked && !focused) {
			return;
		}
		super.setFocused(focused);
		if (focused) {
			this.focusedTicks = 0;
		}
	}

	private boolean isEditable() {
		return this.editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public int getInnerWidth() {
		return this.drawsBackground() ? this.width - 8 : this.width;
	}

	public void setSelectionEnd(int index) {
		int i = this.text.length();
		this.selectionEnd = MathHelper.clamp_int(index, 0, i);
		if (this.textRenderer != null) {
			if (this.firstCharacterIndex > i) {
				this.firstCharacterIndex = i;
			}
			int j = this.getInnerWidth();
			String string = this.textRenderer.trimStringToWidth(this.text.substring(this.firstCharacterIndex), j);
			int k = string.length() + this.firstCharacterIndex;
			if (this.selectionEnd == this.firstCharacterIndex) {
				this.firstCharacterIndex -= this.textRenderer.trimStringToWidth(this.text, j, true).length();
			}
			if (this.selectionEnd > k) {
				this.firstCharacterIndex += this.selectionEnd - k;
			} else if (this.selectionEnd <= this.firstCharacterIndex) {
				this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
			}
			this.firstCharacterIndex = MathHelper.clamp_int(this.firstCharacterIndex, 0, i);
		}
	}

	public void setFocusUnlocked(boolean focusUnlocked) {
		this.focusUnlocked = focusUnlocked;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setSuggestion(@Nullable String suggestion) {
		this.suggestion = suggestion;
	}

	public int getCharacterX(int index) {
		if (index > this.text.length()) {
			return this.getX();
		}
		return this.getX() + this.textRenderer.getStringWidth(this.text.substring(0, index));
	}

	public void setPlaceholder(Text placeholder) {
		this.placeholder = placeholder;
	}
}
