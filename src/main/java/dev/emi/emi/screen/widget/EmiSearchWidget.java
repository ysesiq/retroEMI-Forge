package dev.emi.emi.screen.widget;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.QueryType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import com.rewindmc.retroemi.Pair;
import com.rewindmc.retroemi.RetroEMI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.glRotatef;

public class EmiSearchWidget extends TextFieldWidget {
	private static final Pattern ESCAPE = Pattern.compile("\\\\.");
	private List<Pair<Integer, Style>> styles;
	private long lastClick = 0;
	private String last = "";
	private long lastRender = System.currentTimeMillis();
	private long accumulatedSpin = 0;
	public boolean highlight = false;
	// Reimplement focus because other mods keep breaking it
	public boolean isFocused;
	Tessellator tessellator = Tessellator.instance;

	public EmiSearchWidget(FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(fontRenderer, x, y, width, height, EmiPort.literal(""));
		this.setFocusUnlocked(true);
		this.setEditableColor(-1);
		this.setUneditableColor(-1);
		this.setMaxLength(256);
		this.setRenderTextProvider((string, stringStart) -> {
			MutableText text = null;
			int s = 0;
			int last = 0;
			for (; s < styles.size(); s++) {
				Pair<Integer, Style> style = styles.get(s);
				int end = style.getLeft();
				if (end > stringStart) {
					if (end - stringStart >= string.length()) {
						text = EmiPort.literal(string, style.getRight());
						// Skip second loop
						s = styles.size();
						break;
					}
					text = EmiPort.literal(string.substring(0, end - stringStart), style.getRight());
					last = end - stringStart;
					s++;
					break;
				}
			}
			for (; s < styles.size(); s++) {
				Pair<Integer, Style> style = styles.get(s);
				int end = style.getLeft();
				if (end - stringStart >= string.length()) {
					EmiPort.append(text, EmiPort.literal(string.substring(last), style.getRight()));
					break;
				}
				EmiPort.append(text, EmiPort.literal(string.substring(last, end - stringStart), style.getRight()));
				last = end - stringStart;
			}
			return EmiPort.ordered(text);
		});
		this.setChangedListener(string -> {
			if (string.isEmpty()) {
				this.setSuggestion(RetroEMI.translate("emi.search"));
				EmiScreenManager.focusSearchSidebarType(EmiConfig.emptySearchSidebarFocus);
			} else {
				this.setSuggestion("");
				EmiScreenManager.focusSearchSidebarType(EmiConfig.searchSidebarFocus);
			}
			Matcher matcher = EmiSearch.TOKENS.matcher(string);
			List<Pair<Integer, Style>> styles = Lists.newArrayList();
			int last = 0;
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				if (last < start) {
					styles.add(new Pair<Integer, Style>(start, Style.EMPTY.withFormatting(Formatting.WHITE)));
				}
				String group = matcher.group();
				if (group.startsWith("-")) {
					styles.add(new Pair<Integer, Style>(start + 1, Style.EMPTY.withFormatting(Formatting.RED)));
					start++;
					group = group.substring(1);
				}
				QueryType type = QueryType.fromString(group);
				int subStart = type.prefix.length();
				if (group.length() > 1 + subStart && group.substring(subStart).startsWith("/") && group.endsWith("/")) {
					int rOff = start + subStart + 1;
					styles.add(new Pair<Integer, Style>(rOff, type.slashColor));
					Matcher rMatcher = ESCAPE.matcher(string.substring(rOff, end - 1));
					int rLast = 0;
					while (rMatcher.find()) {
						int rStart = rMatcher.start();
						int rEnd = rMatcher.end();
						if (rLast < rStart) {
							styles.add(new Pair<Integer, Style>(rStart + rOff, type.regexColor));
						}
						styles.add(new Pair<Integer, Style>(rEnd + rOff, type.escapeColor));
						rLast = rEnd;
					}
					if (rLast < end - 1) {
						styles.add(new Pair<Integer, Style>(end - 1, type.regexColor));
					}
					styles.add(new Pair<Integer, Style>(end, type.slashColor));
				} else {
					styles.add(new Pair<Integer, Style>(end, type.color));
				}

				last = end;
			}
			if (last < string.length()) {
				styles.add(new Pair<Integer, Style>(string.length(), Style.EMPTY.withFormatting(Formatting.WHITE)));
			}
			this.styles = styles;
			EmiSearch.search(string);
		});
	}

	public void update() {
		setText(getText());
	}

	public void swap() {
		String last = this.getText();
		this.setText(this.last);
		this.last = last;
	}

	@Override
	public void setFocused(boolean focused) {
		isFocused = focused;
		super.setFocused(focused);
	}

	@Override
	public boolean isFocused() {
		return isFocused;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!isMouseOver(mouseX, mouseY) || !EmiConfig.enabled) {
			setFocused(false);
			return false;
		} else {
			boolean b = super.mouseClicked(mouseX, mouseY, button == 1 ? 0 : button);
			if (isMouseOver(mouseX, mouseY)) {
				setFocused(true);
			}
			if (this.isFocused()) {
				if (button == 0) {
					if (System.currentTimeMillis() - lastClick < 500) {
						highlight = !highlight;
						lastClick = 0;
					} else {
						lastClick = System.currentTimeMillis();
					}
				} else if (button == 1) {
					this.setText("");
					this.setFocused(true);
				}
			}
			return b;
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.isFocused()) {
			if (EmiConfig.clearSearch.matchesKey(keyCode, scanCode)) {
				setText("");
				return true;
			}
			if ((EmiConfig.focusSearch.matchesKey(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE)) {
				this.setFocused(false);
				this.setFocused(false);
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		this.setEditable(EmiConfig.enabled);
		String lower = getText().toLowerCase();

		boolean dinnerbone = lower.contains("dinnerbone");
		accumulatedSpin += (dinnerbone ? 1 : -1) * Math.abs(System.currentTimeMillis() - lastRender);
		if (accumulatedSpin < 0) {
			accumulatedSpin = 0;
		} else if (accumulatedSpin > 500) {
			accumulatedSpin = 500;
		}
		lastRender = System.currentTimeMillis();
		long deg = accumulatedSpin * -180 / 500;
		MatrixStack view = MatrixStack.INSTANCE;

		view.push();
		if (deg != 0) {
			view.translate(this.x + (double) this.width / 2, this.y + (double) this.height / 2, 0);
			view.multiply(() -> glRotatef(deg, 0, 0, -1));
			view.translate(-(this.x + (double) this.width / 2), -(this.y + (double) this.height / 2), 0);
		}

		if (lower.contains("jeb_")) {
			int amount = 0x3FF;
			lastRender = System.currentTimeMillis();
			float h = ((lastRender & amount) % (float) amount) / (float) amount;
			int rgb = Color.HSBtoRGB(h, 1, 1);
//			context.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, ((rgb >> 0) & 0xFF) / 255f);
			tessellator.setColorRGBA((int) (((rgb >> 16) & 0xFF) / 255f), (int) (((rgb >> 8) & 0xFF) / 255f), (int) (((rgb >> 0) & 0xFF) / 255f), 1);
		}

		if (EmiConfig.enabled) {
			super.render(context.raw(), mouseX, mouseY, delta);
			if (highlight) {
				int border = 0xffeeee00;
				context.fill(this.x - 2, this.y - 2, this.width + 3, 1, border);
				context.fill(this.x - 2, this.y + this.height + 1, this.width + 3, 1, border);
				context.fill(this.x - 2, this.y - 2, 1, this.height + 3, border);
				context.fill(this.x + this.width + 1, this.y - 2, 1, this.height + 4, border);
			}
		}
		GL11.glColor4f(1, 1, 1, 1);
		view.pop();
	}
}
