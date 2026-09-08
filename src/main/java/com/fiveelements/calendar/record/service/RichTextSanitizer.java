package com.fiveelements.calendar.record.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

final class RichTextSanitizer {
  private static final Set<String> TAGS = Set.of(
      "p", "div", "br", "strong", "b", "em", "i", "u", "s", "strike",
      "h1", "h2", "h3", "blockquote", "ol", "ul", "li", "span", "hr", "img");
  private static final Set<String> ALIGNMENTS = Set.of("left", "center", "right", "justify");
  private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{3,8}");
  private static final Pattern FONT_SIZE = Pattern.compile("(?:10|12|14|16|18|20|24|28|32|36|40)px");
  private static final Pattern PRIVATE_IMAGE = Pattern.compile("^/app-api/files/(\\d+)$");
  private static final Safelist ALLOWED = Safelist.none()
      .addTags(TAGS.toArray(String[]::new))
      .addAttributes(":all", "style")
      .addAttributes("img", "src", "alt", "width", "height");

  private RichTextSanitizer() {}

  static RichTextContent normalize(String source) {
    Document.OutputSettings output = new Document.OutputSettings().prettyPrint(false);
    // Quill stores paragraph alignment in classes; retain only known values as safe inline styles.
    Document input = Jsoup.parseBodyFragment(source);
    input.outputSettings(output);
    for (Element element : input.select("[class]")) {
      for (String alignment : ALIGNMENTS) {
        if (element.hasClass("ql-align-" + alignment)) {
          element.attr("style", element.attr("style") + ";text-align:" + alignment);
        }
      }
    }
    String safe = Jsoup.clean(input.body().html(), "", ALLOWED, output);
    Document document = Jsoup.parseBodyFragment(safe);
    document.outputSettings(output);
    for (Element element : document.select("[style]")) {
      String style = safeStyle(element.attr("style"));
      if (style.isEmpty()) element.removeAttr("style");
      else element.attr("style", style);
    }
    List<Long> fileIds = new ArrayList<>();
    for (Element image : document.select("img")) {
      var matcher = PRIVATE_IMAGE.matcher(image.attr("src"));
      if (!matcher.matches()) {
        image.remove();
        continue;
      }
      fileIds.add(Long.parseLong(matcher.group(1)));
      image.attr("width", "100%");
      image.removeAttr("height");
      if (image.attr("alt").length() > 100) image.attr("alt", image.attr("alt").substring(0, 100));
    }
    String html = document.body().html().trim();
    String text = document.body().text().trim();
    if (text.isEmpty() && fileIds.isEmpty()) throw new IllegalArgumentException("请输入记录内容");
    if (text.length() > 10000) throw new IllegalArgumentException("记录内容最多 10000 个字");
    return new RichTextContent(html, text, fileIds.stream().distinct().toList());
  }

  private static String safeStyle(String source) {
    List<String> styles = new ArrayList<>();
    for (String declaration : source.split(";")) {
      String[] parts = declaration.split(":", 2);
      if (parts.length != 2) continue;
      String name = parts[0].trim().toLowerCase(Locale.ROOT);
      String value = parts[1].trim().toLowerCase(Locale.ROOT);
      if (name.equals("color") || name.equals("background-color")) {
        value = safeColor(value);
        if (value.isEmpty()) continue;
        styles.add(name + ": " + value);
      } else if (name.equals("text-align") && ALIGNMENTS.contains(value)) {
        styles.add(name + ": " + value);
      } else if (name.equals("font-weight") && Set.of("normal", "bold", "400", "700").contains(value)) {
        styles.add(name + ": " + value);
      } else if (name.equals("font-style") && Set.of("normal", "italic").contains(value)) {
        styles.add(name + ": " + value);
      } else if (name.equals("text-decoration") && Set.of("none", "underline", "line-through", "underline line-through").contains(value)) {
        styles.add(name + ": " + value);
      } else if (name.equals("font-size") && FONT_SIZE.matcher(value).matches()) {
        styles.add(name + ": " + value);
      }
    }
    return String.join("; ", styles);
  }

  private static String safeColor(String value) {
    if (COLOR.matcher(value).matches()) return value;
    var matcher = Pattern.compile("rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)").matcher(value);
    if (!matcher.matches()) return "";
    int r = Integer.parseInt(matcher.group(1));
    int g = Integer.parseInt(matcher.group(2));
    int b = Integer.parseInt(matcher.group(3));
    if (r > 255 || g > 255 || b > 255) return "";
    return String.format("#%02x%02x%02x", r, g, b);
  }

  record RichTextContent(String html, String text, List<Long> fileIds) {}
}
