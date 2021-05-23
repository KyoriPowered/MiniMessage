/*
 * This file is part of adventure-text-minimessage, licensed under the MIT License.
 *
 * Copyright (c) 2018-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text.minimessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.parser.ElementNode;
import net.kyori.adventure.text.minimessage.parser.ElementParser;
import net.kyori.adventure.text.minimessage.transformation.Transformation;
import net.kyori.adventure.text.minimessage.transformation.TransformationRegistry;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.minimessage.Tokens.TAG_END;
import static net.kyori.adventure.text.minimessage.Tokens.TAG_START;

class MiniMessageParser {

  // regex group names
  private static final String START = "start";
  private static final String TOKEN = "token";
  private static final String INNER = "inner";
  private static final String END = "end";
  // https://regex101.com/r/8VZ7uA/10
  private static final Pattern pattern = Pattern.compile("((?<start><)(?<token>[^<>]+(:(?<inner>['\"]?([^'\"](\\\\['\"])?)+['\"]?))*)(?<end>>))+?");

  private final TransformationRegistry registry;
  private final Function<String, ComponentLike> placeholderResolver;

  MiniMessageParser() {
    this.registry = new TransformationRegistry();
    this.placeholderResolver = MiniMessageImpl.DEFAULT_PLACEHOLDER_RESOLVER;
  }

  MiniMessageParser(final TransformationRegistry registry, final Function<String, ComponentLike> placeholderResolver) {
    this.registry = registry;
    this.placeholderResolver = placeholderResolver;
  }

  @NonNull String escapeTokens(final @NonNull String richMessage) {
    final StringBuilder sb = new StringBuilder();
    final Matcher matcher = pattern.matcher(richMessage);
    int lastEnd = 0;
    while(matcher.find()) {
      final int startIndex = matcher.start();
      final int endIndex = matcher.end();

      if(startIndex > lastEnd) {
        sb.append(richMessage, lastEnd, startIndex);
      }
      lastEnd = endIndex;

      final String start = matcher.group(START);
      String token = matcher.group(TOKEN);
      final String inner = matcher.group(INNER);
      final String end = matcher.group(END);

      // also escape inner
      if(inner != null) {
        token = token.replace(inner, this.escapeTokens(inner));
      }

      sb.append("\\").append(start).append(token).append(end);
    }

    if(richMessage.length() > lastEnd) {
      sb.append(richMessage.substring(lastEnd));
    }

    return sb.toString();
  }

  @NonNull String stripTokens(final @NonNull String richMessage) {
    final StringBuilder sb = new StringBuilder();
    final Matcher matcher = pattern.matcher(richMessage);
    int lastEnd = 0;
    while(matcher.find()) {
      final int startIndex = matcher.start();
      final int endIndex = matcher.end();

      if(startIndex > lastEnd) {
        sb.append(richMessage, lastEnd, startIndex);
      }
      lastEnd = endIndex;
    }

    if(richMessage.length() > lastEnd) {
      sb.append(richMessage.substring(lastEnd));
    }

    return sb.toString();
  }

  @NonNull String sanitizePlaceholder(final String input) {
    return input.replace("</pre>", "\\</pre>");
  }

  @NonNull String handlePlaceholders(@NonNull String richMessage, final @NonNull Context context, final @NonNull String... placeholders) {
    if(placeholders.length % 2 != 0) {
      throw new ParseException(
        "Invalid number placeholders defined, usage: parseFormat(format, key, value, key, value...)");
    }
    for(int i = 0; i < placeholders.length; i += 2) {
      richMessage = richMessage.replace(TAG_START + placeholders[i] + TAG_END, this.sanitizePlaceholder(placeholders[i + 1]));
    }
    context.replacedMessage(richMessage);
    return richMessage;
  }

  @NonNull String handlePlaceholders(@NonNull String richMessage, final @NonNull Context context, final @NonNull Map<String, String> placeholders) {
    for(final Map.Entry<String, String> entry : placeholders.entrySet()) {
      richMessage = richMessage.replace(TAG_START + entry.getKey() + TAG_END, entry.getValue());
    }
    context.replacedMessage(richMessage);
    return richMessage;
  }

  @NonNull Component parseFormat(final @NonNull String richMessage, final @NonNull Context context, final @NonNull String... placeholders) {
    return this.parseFormat(this.handlePlaceholders(richMessage, context, placeholders), context);
  }

  @NonNull Component parseFormat(final @NonNull String richMessage, final @NonNull Map<String, String> placeholders, final Context context) {
    return this.parseFormat(this.handlePlaceholders(richMessage, context, placeholders), context);
  }

  @NonNull Component parseFormat(@NonNull String input, final Context context, final @NonNull Template... placeholders) {
    final Map<String, Template.ComponentTemplate> map = new HashMap<>();
    for(final Template placeholder : placeholders) {
      if(placeholder instanceof Template.StringTemplate) {
        final Template.StringTemplate stringTemplate = (Template.StringTemplate) placeholder;
        input = input.replace(TAG_START + stringTemplate.key() + TAG_END, this.sanitizePlaceholder(stringTemplate.value()));
      } else if(placeholder instanceof Template.ComponentTemplate) {
        final Template.ComponentTemplate componentTemplate = (Template.ComponentTemplate) placeholder;
        map.put(componentTemplate.key(), componentTemplate);
      }
    }
    return this.parseFormat0(input, map, context);
  }

  @NonNull Component parseFormat(@NonNull String input, final @NonNull List<Template> placeholders, final @NonNull Context context) {
    final Map<String, Template.ComponentTemplate> map = new HashMap<>();
    for(final Template placeholder : placeholders) {
      if(placeholder instanceof Template.StringTemplate) {
        final Template.StringTemplate stringTemplate = (Template.StringTemplate) placeholder;
        input = input.replace(TAG_START + stringTemplate.key() + TAG_END, stringTemplate.value());
      } else if(placeholder instanceof Template.ComponentTemplate) {
        final Template.ComponentTemplate componentTemplate = (Template.ComponentTemplate) placeholder;
        map.put(componentTemplate.key(), componentTemplate);
      }
    }
    return this.parseFormat0(input, map, context);
  }

  @NonNull Component parseFormat(final @NonNull String richMessage, final @NonNull Context context) {
    return this.parseFormat0(richMessage, Collections.emptyMap(), context);
  }

  @NonNull Component parseFormat0(final @NonNull String richMessage, final @NonNull Map<String, Template.ComponentTemplate> templates, final @NonNull Context context) {
    return this.parseFormat0(richMessage, templates, this.registry, this.placeholderResolver, context);
  }

  @NonNull Component parseFormat0(final @NonNull String richMessage, final @NonNull Map<String, Template.ComponentTemplate> templates, final @NonNull TransformationRegistry registry, final @NonNull Function<String, ComponentLike> placeholderResolver, final Context context) {
    try {
      ElementParser elementParser = new ElementParser(richMessage);
      ElementNode root = elementParser.parse();
      context.root(root);
      return this.parse(root, registry, templates, placeholderResolver, context);
    } catch (net.kyori.adventure.text.minimessage.parser.gen.ParseException e) {
      throw new ParseException(e.getMessage()); // TODO better error handling
    }
  }

  @NonNull Component parse(final @NonNull ElementNode node, final @NonNull TransformationRegistry registry, final @NonNull Map<String, Template.ComponentTemplate> templates, final @NonNull Function<String, ComponentLike> placeholderResolver, final @NonNull Context context) {
    Component comp;
    if (node instanceof ElementNode.RawTextNode) {
      comp = Component.text(((ElementNode.RawTextNode) node).getValue());
    } else if (node instanceof ElementNode.TagNode) {
      ElementNode.TagNode tag = (ElementNode.TagNode) node;

      Transformation transformation = registry.get(tag.name(), tag.getParts(), templates, placeholderResolver, context);
      if (transformation == null) {
        // unknown, treat as text
        return Component.text("<error>"); // TODO turn node into text
      }
      comp = transformation.apply();
    } else {
      comp = Component.empty();
    }

    for (ElementNode child : node.getChildren()) {
      comp = comp.append(parse(child, registry, templates, placeholderResolver, context));
    }

    // if root is empty, lift its only child it up
    if (comp instanceof TextComponent) {
      TextComponent root = (TextComponent) comp;
      if (root.content().isEmpty() && root.children().size() == 1 && !root.hasStyling() && root.hoverEvent() == null && root.clickEvent() == null) {
        return root.children().get(0);
      }
    }

    return comp;
  }
}
