package com.fiveelements.calendar.record.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class RichTextSanitizerTest {
  @Test
  void preservesEditorRgbColorsAndQuillAlignment() {
    var result = RichTextSanitizer.normalize("<p class='ql-align-center arbitrary' onclick='bad()'><span style='color: rgb(78, 130, 238);font-weight:bold;font-style:italic;text-decoration:underline'>正文</span></p>");
    assertTrue(result.html().contains("text-align: center"));
    assertTrue(result.html().contains("color: #4e82ee"));
    assertTrue(result.html().contains("font-weight: bold"));
    assertTrue(result.html().contains("font-style: italic"));
    assertTrue(result.html().contains("text-decoration: underline"));
    assertFalse(result.html().contains("class="));
    assertFalse(result.html().contains("onclick"));
  }

  @Test
  void rejectsInvalidRgbColorsAndStylePayloads() {
    var result = RichTextSanitizer.normalize("<p style='color:rgb(999,0,0);font-weight:expression(alert(1));background-color:url(x);text-decoration:url(x)'>正文</p>");
    assertEquals("<p>正文</p>", result.html());
  }

  @Test
  void keepsSupportedFormattingAndExtractsSearchableText() {
    var result = RichTextSanitizer.normalize(
        "<h1>标题</h1><p><strong>重点</strong> 内容</p><ul><li>清单</li></ul>");
    assertTrue(result.html().contains("<h1>标题</h1>"));
    assertTrue(result.html().contains("<strong>重点</strong>"));
    assertEquals("标题 重点 内容 清单", result.text());
  }

  @Test
  void removesScriptsLinksAndUnsafeStyles() {
    var result = RichTextSanitizer.normalize(
        "<script>alert(1)</script><p onclick='x()' style='color:#4e82ee;background:url(x);text-align:center'>安全内容</p><a href='javascript:alert(1)'>链接</a>");
    assertFalse(result.html().contains("script"));
    assertFalse(result.html().contains("onclick"));
    assertFalse(result.html().contains("href"));
    assertFalse(result.html().contains("url("));
    assertTrue(result.html().contains("color: #4e82ee"));
    assertTrue(result.html().contains("text-align: center"));
  }

  @Test
  void keepsPrivateImagesAndSafeFontSizes() {
    var result = RichTextSanitizer.normalize(
        "<p><span style='font-size:24px;color:#3971e8'>大字</span></p><img src='/app-api/files/42' width='200' onerror='x()'><img src='https://bad.example/x.png'>");
    assertTrue(result.html().contains("font-size: 24px"));
    assertTrue(result.html().contains("src=\"/app-api/files/42\""));
    assertTrue(result.html().contains("width=\"100%\""));
    assertFalse(result.html().contains("bad.example"));
    assertEquals(List.of(42L), result.fileIds());
  }
}
