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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MiniMessageParserTest {

  @Test
  public void test() {
    final String input1 = "<yellow>TEST<green> nested</green>Test";
    final String input2 = "<yellow>TEST<green> nested<yellow>Test";
    final String out1 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input1));
    final String out2 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input2));

    assertEquals(out1, out2);
  }

  @Test
  public void testNewColor() {
    final String input1 = "<color:yellow>TEST<color:green> nested</color:green>Test";
    final String input2 = "<color:yellow>TEST<color:green> nested<color:yellow>Test";
    final String out1 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input1));
    final String out2 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input2));

    assertEquals(out1, out2);
  }

  @Test
  public void testHexColor() {
    final String input1 = "<color:#ff00ff>TEST<color:#00ff00> nested</color:#00ff00>Test";
    final String input2 = "<color:#ff00ff>TEST<color:#00ff00> nested<color:#ff00ff>Test";
    final String out1 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input1));
    final String out2 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input2));

    assertEquals(out1, out2);
  }

  @Test
  public void testHexColorShort() {
      final String input1 = "<#ff00ff>TEST<#00ff00> nested</#00ff00>Test";
      final String input2 = "<#ff00ff>TEST<#00ff00> nested<#ff00ff>Test";
      final String out1 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input1));
      final String out2 = GsonComponentSerializer.gson().serialize(MiniMessageParser.parseFormat(input2));

      assertEquals(out1, out2);
  }

  @Test
  public void testStripSimple() {
    final String input = "<yellow>TEST<green> nested</green>Test";
    final String expected = "TEST nestedTest";
    assertEquals(expected, MiniMessageParser.stripTokens(input));
  }

  @Test
  public void testStripComplex() {
    final String input = "<yellow><test> random <bold>stranger</bold><click:run_command:test command><underlined><red>click here</click><blue> to <bold>FEEL</underlined> it";
    final String expected = " random strangerclick here to FEEL it";
    assertEquals(expected, MiniMessageParser.stripTokens(input));
  }

  @Test
  public void testStripInner() {
    final String input = "<hover:show_text:\"<red>test:TEST\">TEST";
    final String expected = "TEST";
    assertEquals(expected, MiniMessageParser.stripTokens(input));
  }

  @Test
  public void testEscapeSimple() {
    final String input = "<yellow>TEST<green> nested</green>Test";
    final String expected = "\\<yellow\\>TEST\\<green\\> nested\\</green\\>Test";
    assertEquals(expected, MiniMessageParser.escapeTokens(input));
  }

  @Test
  public void testEscapeComplex() {
    final String input = "<yellow><test> random <bold>stranger</bold><click:run_command:test command><underlined><red>click here</click><blue> to <bold>FEEL</underlined> it";
    final String expected = "\\<yellow\\>\\<test\\> random \\<bold\\>stranger\\</bold\\>\\<click:run_command:test command\\>\\<underlined\\>\\<red\\>click here\\</click\\>\\<blue\\> to \\<bold\\>FEEL\\</underlined\\> it";
    assertEquals(expected, MiniMessageParser.escapeTokens(input));
  }

  @Test
  public void testEscapeInner() {
    final String input = "<hover:show_text:\"<red>test:TEST\">TEST";
    final String expected = "\\<hover:show_text:\"\\<red\\>test:TEST\"\\>TEST";
    assertEquals(expected, MiniMessageParser.escapeTokens(input));
  }

  @Test
  public void testUnescape() {
    final String input ="<yellow>TEST\\<green\\> nested\\</green\\>Test";
    final String expected = "TEST<green> nested</green>Test";
    TextComponent comp = (TextComponent) MiniMessageParser.parseFormat(input);

    assertEquals(expected, comp.content());
  }

  @Test
  public void testNoUnescape() {
    final String input ="<yellow>TEST\\<green\\>\\> \\< nested\\</green\\>Test";
    final String expected = "TEST<green>\\> \\< nested</green>Test";
    TextComponent comp = (TextComponent) MiniMessageParser.parseFormat(input);

    assertEquals(expected, comp.content());
  }

  @Test
  public void checkPlaceholder() {
    final String input = "<test>";
    final String expected = "{\"text\":\"Hello!\"}";
    Component comp = MiniMessageParser.parseFormat(input, "test", "Hello!");

    test(comp, expected);
  }

  @Test
  public void testNiceMix() {
    final String input = "<yellow><test> random <bold>stranger</bold><click:run_command:test command><underlined><red>click here</click><blue> to <bold>FEEL</underlined> it";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Hello! random \",\"color\":\"yellow\"},{\"text\":\"stranger\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"click here\",\"color\":\"red\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"test command\"}},{\"text\":\" to \",\"color\":\"blue\",\"underlined\":true},{\"text\":\"FEEL\",\"color\":\"blue\",\"bold\":true,\"underlined\":true},{\"text\":\" it\",\"color\":\"blue\",\"bold\":true}]}";
    Component comp = MiniMessageParser.parseFormat(input, "test", "Hello!");

    test(comp, expected);
  }

  @Test
  public void testColorSimple() {
    final String input = "<yellow>TEST";
    final String expected = "{\"text\":\"TEST\",\"color\":\"yellow\"}";

    test(input, expected);
  }

  @Test
  public void testColorNested() {
    final String input = "<yellow>TEST<green>nested</green>Test";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"TEST\",\"color\":\"yellow\"},{\"text\":\"nested\",\"color\":\"green\"},{\"text\":\"Test\",\"color\":\"yellow\"}]}";

    test(input, expected);
  }

  @Test
  public void testColorNotNested() {
    final String input = "<yellow>TEST</yellow><green>nested</green>Test";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"TEST\",\"color\":\"yellow\"},{\"text\":\"nested\",\"color\":\"green\"},{\"text\":\"Test\"}]}";

    test(input, expected);
  }

  @Test
  public void testHover() {
    final String input = "<hover:show_text:\"<red>test\">TEST";
    final String expected = "{\"text\":\"TEST\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"test\",\"color\":\"red\"}}}";

    test(input, expected);
  }

  @Test
  public void testHover2() {
    final String input = "<hover:show_text:'<red>test'>TEST";
    final String expected = "{\"text\":\"TEST\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"test\",\"color\":\"red\"}}}";

    test(input, expected);
  }

  @Test
  public void testHoverWithColon() {
    final String input = "<hover:show_text:\"<red>test:TEST\">TEST";
    final String expected = "{\"text\":\"TEST\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"test:TEST\",\"color\":\"red\"}}}";

    test(input, expected);
  }

  @Test
  public void testHoverMultiline() {
    final String input = "<hover:show_text:'<red>test\ntest2'>TEST";
    final String expected = "{\"text\":\"TEST\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"test\\ntest2\",\"color\":\"red\"}}}";

    test(input, expected);
  }

  @Test
  public void testClick() {
    final String input = "<click:run_command:test>TEST";
    final String expected = "{\"text\":\"TEST\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"test\"}}";

    test(input, expected);
  }

  @Test
  public void testClickExtendedCommand() {
    final String input = "<click:run_command:/test command>TEST";
    final String expected = "{\"text\":\"TEST\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/test command\"}}";

    test(input, expected);
  }

  @Test
  public void testInvalidTag() {
    final String input = "<test>";
    final String expected = "{\"text\":\"\\u003ctest\\u003e\"}"; // gson makes it html save
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);

    // TODO am not totally happy about this yet, invalid tags arent getting colored for example, but good enough for now
  }

  @Test
  public void testInvalidTagComplex() {
    final String input = "<yellow><test> random <bold>stranger</bold><click:run_command:test command><oof></oof><underlined><red>click here</click><blue> to <bold>FEEL</underlined> it";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"\\u003ctest\\u003e\",\"color\":\"yellow\"},{\"text\":\" random \",\"color\":\"yellow\"},{\"text\":\"stranger\",\"color\":\"yellow\",\"bold\":true},{\"text\":\"\\u003coof\\u003e\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"test command\"}},{\"text\":\"\\u003c/oof\\u003e\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"test command\"}},{\"text\":\"click here\",\"color\":\"red\",\"underlined\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"test command\"}},{\"text\":\" to \",\"color\":\"blue\",\"underlined\":true},{\"text\":\"FEEL\",\"color\":\"blue\",\"bold\":true,\"underlined\":true},{\"text\":\" it\",\"color\":\"blue\",\"bold\":true}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testKeyBind() {
    final String input = "Press <key:key.jump> to jump!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Press \"},{\"keybind\":\"key.jump\"},{\"text\":\" to jump!\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testKeyBindWithColor() {
    final String input = "Press <red><key:key.jump> to jump!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Press \"},{\"keybind\":\"key.jump\",\"color\":\"red\"},{\"text\":\" to jump!\",\"color\":\"red\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testTranslatable() {
    final String input = "You should get a <lang:block.minecraft.diamond_block>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"You should get a \"},{\"translate\":\"block.minecraft.diamond_block\"},{\"text\":\"!\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testTranslatableWith() {
    final String input = "Test: <lang:commands.drop.success.single:'<red>1':'<blue>Stone'>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Test: \"},{\"translate\":\"commands.drop.success.single\",\"with\":[{\"text\":\"1\",\"color\":\"red\"},{\"text\":\"Stone\",\"color\":\"blue\"}]},{\"text\":\"!\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  @Disabled("Need to implement inner with ' or \"")  // TODO
  public void testTranslatableWithHover() {
    final String input = "Test: <lang:commands.drop.success.single:'<red>1<hover:show_text:'<red>dum'>':'<blue>Stone'>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Test: \"},{\"translate\":\"commands.drop.success.single\",\"with\":[{\"text\":\"1\",\"color\":\"red\"},{\"text\":\"Stone\",\"color\":\"blue\"}]},{\"text\":\"!\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testKingAlter() {
    final String input = "Ahoy <lang:offset.-40:'<red>mates!'>";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Ahoy \"},{\"translate\":\"offset.-40\",\"with\":[{\"text\":\"mates!\",\"color\":\"red\"}]}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testInsertion() {
    final String input = "Click <insert:test>this</insert> to insert!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Click \"},{\"text\":\"this\",\"insertion\":\"test\"},{\"text\":\" to insert!\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGH5() {
    final String input = "<dark_gray>»<gray> To download it from the internet, <click:open_url:<pack_url>><hover:show_text:\"<green>/!\\ install it from Options/ResourcePacks in your game\"><green><bold>CLICK HERE</bold></hover></click>";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"»\",\"color\":\"dark_gray\"},{\"text\":\" To download it from the internet, \",\"color\":\"gray\"},{\"text\":\"CLICK HERE\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.google.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"/!\\\\ install it from Options/ResourcePacks in your game\",\"color\":\"green\"}}}]}";

    // should work
    final Component comp1 = MiniMessageParser.parseFormat(input, "pack_url", "https://www.google.com");
    test(comp1, expected);

    // shouldnt throw an error
    MiniMessageParser.parseFormat(input, "url", "https://www.google.com");
  }

  @Test
  @Disabled("Need to implement inner with ' or \"")  // TODO
  public void testGH5Modified() {
    final String input = "<dark_gray>»<gray> To download it from the internet, <click:open_url:<pack_url>><hover:show_text:\"<green>/!\\ install it from 'Options/ResourcePacks' in your game\"><green><bold>CLICK HERE</bold></hover></click>";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"»\",\"color\":\"dark_gray\"},{\"text\":\" To download it from the internet, \",\"color\":\"gray\"},{\"text\":\"CLICK HERE\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.google.com\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"/!\\\\ install it from 'Options/ResourcePacks' in your game\",\"color\":\"green\"}}}]}";

    // should work
    final Component comp1 = MiniMessageParser.parseFormat(input, "pack_url", "https://www.google.com");
    test(comp1, expected);

    // shouldnt throw an error
    MiniMessageParser.parseFormat(input, "url", "https://www.google.com");
  }

  @Test
  public void testReset() {
    final String input = "Click <yellow><insert:test>this<rainbow> wooo<reset> to insert!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Click \"},{\"text\":\"this\",\"color\":\"yellow\",\"insertion\":\"test\"},{\"text\":\" \",\"color\":\"#f3801f\",\"insertion\":\"test\"},{\"text\":\"w\",\"color\":\"#71f813\",\"insertion\":\"test\"},{\"text\":\"o\",\"color\":\"#03ca9c\",\"insertion\":\"test\"},{\"text\":\"o\",\"color\":\"#4135fe\",\"insertion\":\"test\"},{\"text\":\"o\",\"color\":\"#d507b1\",\"insertion\":\"test\"},{\"text\":\" to insert!\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testPre() {
    final String input = "Click <yellow><pre><insert:test>this</pre> to <red>insert!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Click \"},{\"text\":\"\\u003cinsert:test\\u003e\",\"color\":\"yellow\"},{\"text\":\"this\",\"color\":\"yellow\"},{\"text\":\" to \",\"color\":\"yellow\"},{\"text\":\"insert!\",\"color\":\"red\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testRainbow() {
    final String input = "<yellow>Woo: <rainbow>||||||||||||||||||||||||</rainbow>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#f3801f\"},{\"text\":\"|\",\"color\":\"#e1a00d\"},{\"text\":\"|\",\"color\":\"#c9bf03\"},{\"text\":\"|\",\"color\":\"#acd901\"},{\"text\":\"|\",\"color\":\"#8bed08\"},{\"text\":\"|\",\"color\":\"#6afa16\"},{\"text\":\"|\",\"color\":\"#4bff2c\"},{\"text\":\"|\",\"color\":\"#2ffa48\"},{\"text\":\"|\",\"color\":\"#18ed68\"},{\"text\":\"|\",\"color\":\"#08d989\"},{\"text\":\"|\",\"color\":\"#01bfa9\"},{\"text\":\"|\",\"color\":\"#02a0c7\"},{\"text\":\"|\",\"color\":\"#0c80e0\"},{\"text\":\"|\",\"color\":\"#1e5ff2\"},{\"text\":\"|\",\"color\":\"#3640fc\"},{\"text\":\"|\",\"color\":\"#5326fe\"},{\"text\":\"|\",\"color\":\"#7412f7\"},{\"text\":\"|\",\"color\":\"#9505e9\"},{\"text\":\"|\",\"color\":\"#b401d3\"},{\"text\":\"|\",\"color\":\"#d005b7\"},{\"text\":\"|\",\"color\":\"#e71297\"},{\"text\":\"|\",\"color\":\"#f72676\"},{\"text\":\"|\",\"color\":\"#fe4056\"},{\"text\":\"|\",\"color\":\"#fd5f38\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testRainbowPhase() {
    final String input = "<yellow>Woo: <rainbow:2>||||||||||||||||||||||||</rainbow>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#1ff35c\"},{\"text\":\"|\",\"color\":\"#0de17d\"},{\"text\":\"|\",\"color\":\"#03c99e\"},{\"text\":\"|\",\"color\":\"#01acbd\"},{\"text\":\"|\",\"color\":\"#088bd7\"},{\"text\":\"|\",\"color\":\"#166aec\"},{\"text\":\"|\",\"color\":\"#2c4bf9\"},{\"text\":\"|\",\"color\":\"#482ffe\"},{\"text\":\"|\",\"color\":\"#6818fb\"},{\"text\":\"|\",\"color\":\"#8908ef\"},{\"text\":\"|\",\"color\":\"#a901db\"},{\"text\":\"|\",\"color\":\"#c702c1\"},{\"text\":\"|\",\"color\":\"#e00ca3\"},{\"text\":\"|\",\"color\":\"#f21e82\"},{\"text\":\"|\",\"color\":\"#fc3661\"},{\"text\":\"|\",\"color\":\"#fe5342\"},{\"text\":\"|\",\"color\":\"#f77428\"},{\"text\":\"|\",\"color\":\"#e99513\"},{\"text\":\"|\",\"color\":\"#d3b406\"},{\"text\":\"|\",\"color\":\"#b7d001\"},{\"text\":\"|\",\"color\":\"#97e704\"},{\"text\":\"|\",\"color\":\"#76f710\"},{\"text\":\"|\",\"color\":\"#56fe24\"},{\"text\":\"|\",\"color\":\"#38fd3e\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradient() {
    final String input = "<yellow>Woo: <gradient>||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"white\"},{\"text\":\"|\",\"color\":\"#f4f4f4\"},{\"text\":\"|\",\"color\":\"#e9e9e9\"},{\"text\":\"|\",\"color\":\"#dedede\"},{\"text\":\"|\",\"color\":\"#d3d3d3\"},{\"text\":\"|\",\"color\":\"#c8c8c8\"},{\"text\":\"|\",\"color\":\"#bcbcbc\"},{\"text\":\"|\",\"color\":\"#b1b1b1\"},{\"text\":\"|\",\"color\":\"#a6a6a6\"},{\"text\":\"|\",\"color\":\"#9b9b9b\"},{\"text\":\"|\",\"color\":\"#909090\"},{\"text\":\"|\",\"color\":\"#858585\"},{\"text\":\"|\",\"color\":\"#7a7a7a\"},{\"text\":\"|\",\"color\":\"#6f6f6f\"},{\"text\":\"|\",\"color\":\"#646464\"},{\"text\":\"|\",\"color\":\"#595959\"},{\"text\":\"|\",\"color\":\"#4e4e4e\"},{\"text\":\"|\",\"color\":\"#434343\"},{\"text\":\"|\",\"color\":\"#373737\"},{\"text\":\"|\",\"color\":\"#2c2c2c\"},{\"text\":\"|\",\"color\":\"#212121\"},{\"text\":\"|\",\"color\":\"#161616\"},{\"text\":\"|\",\"color\":\"#0b0b0b\"},{\"text\":\"|\",\"color\":\"black\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradient2() {
    final String input = "<yellow>Woo: <gradient:#5e4fa2:#f79459>||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#5e4fa2\"},{\"text\":\"|\",\"color\":\"#65529f\"},{\"text\":\"|\",\"color\":\"#6b559c\"},{\"text\":\"|\",\"color\":\"#725898\"},{\"text\":\"|\",\"color\":\"#795b95\"},{\"text\":\"|\",\"color\":\"#7f5e92\"},{\"text\":\"|\",\"color\":\"#86618f\"},{\"text\":\"|\",\"color\":\"#8d648c\"},{\"text\":\"|\",\"color\":\"#936789\"},{\"text\":\"|\",\"color\":\"#9a6a85\"},{\"text\":\"|\",\"color\":\"#a16d82\"},{\"text\":\"|\",\"color\":\"#a7707f\"},{\"text\":\"|\",\"color\":\"#ae737c\"},{\"text\":\"|\",\"color\":\"#b47679\"},{\"text\":\"|\",\"color\":\"#bb7976\"},{\"text\":\"|\",\"color\":\"#c27c72\"},{\"text\":\"|\",\"color\":\"#c87f6f\"},{\"text\":\"|\",\"color\":\"#cf826c\"},{\"text\":\"|\",\"color\":\"#d68569\"},{\"text\":\"|\",\"color\":\"#dc8866\"},{\"text\":\"|\",\"color\":\"#e38b63\"},{\"text\":\"|\",\"color\":\"#ea8e5f\"},{\"text\":\"|\",\"color\":\"#f0915c\"},{\"text\":\"|\",\"color\":\"#f79459\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradient3() {
    final String input = "<yellow>Woo: <gradient:green:blue>||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"green\"},{\"text\":\"|\",\"color\":\"#55f85c\"},{\"text\":\"|\",\"color\":\"#55f064\"},{\"text\":\"|\",\"color\":\"#55e96b\"},{\"text\":\"|\",\"color\":\"#55e173\"},{\"text\":\"|\",\"color\":\"#55da7a\"},{\"text\":\"|\",\"color\":\"#55d381\"},{\"text\":\"|\",\"color\":\"#55cb89\"},{\"text\":\"|\",\"color\":\"#55c490\"},{\"text\":\"|\",\"color\":\"#55bc98\"},{\"text\":\"|\",\"color\":\"#55b59f\"},{\"text\":\"|\",\"color\":\"#55aea6\"},{\"text\":\"|\",\"color\":\"#55a6ae\"},{\"text\":\"|\",\"color\":\"#559fb5\"},{\"text\":\"|\",\"color\":\"#5598bc\"},{\"text\":\"|\",\"color\":\"#5590c4\"},{\"text\":\"|\",\"color\":\"#5589cb\"},{\"text\":\"|\",\"color\":\"#5581d3\"},{\"text\":\"|\",\"color\":\"#557ada\"},{\"text\":\"|\",\"color\":\"#5573e1\"},{\"text\":\"|\",\"color\":\"#556be9\"},{\"text\":\"|\",\"color\":\"#5564f0\"},{\"text\":\"|\",\"color\":\"#555cf8\"},{\"text\":\"|\",\"color\":\"blue\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradientMultiColor() {
    final String input = "<yellow>Woo: <gradient:red:blue:green:yellow:red>||||||||||||||||||||||||||||||||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"red\"},{\"text\":\"|\",\"color\":\"#f15563\"},{\"text\":\"|\",\"color\":\"#e35571\"},{\"text\":\"|\",\"color\":\"#d55580\"},{\"text\":\"|\",\"color\":\"#c6558e\"},{\"text\":\"|\",\"color\":\"#b8559c\"},{\"text\":\"|\",\"color\":\"#aa55aa\"},{\"text\":\"|\",\"color\":\"#9c55b8\"},{\"text\":\"|\",\"color\":\"#8e55c6\"},{\"text\":\"|\",\"color\":\"#8055d5\"},{\"text\":\"|\",\"color\":\"#7155e3\"},{\"text\":\"|\",\"color\":\"#6355f1\"},{\"text\":\"|\",\"color\":\"blue\"},{\"text\":\"|\",\"color\":\"#6355f1\"},{\"text\":\"|\",\"color\":\"blue\"},{\"text\":\"|\",\"color\":\"#5563f1\"},{\"text\":\"|\",\"color\":\"#5571e3\"},{\"text\":\"|\",\"color\":\"#5580d5\"},{\"text\":\"|\",\"color\":\"#558ec6\"},{\"text\":\"|\",\"color\":\"#559cb8\"},{\"text\":\"|\",\"color\":\"#55aaaa\"},{\"text\":\"|\",\"color\":\"#55b89c\"},{\"text\":\"|\",\"color\":\"#55c68e\"},{\"text\":\"|\",\"color\":\"#55d580\"},{\"text\":\"|\",\"color\":\"#55e371\"},{\"text\":\"|\",\"color\":\"#55f163\"},{\"text\":\"|\",\"color\":\"green\"},{\"text\":\"|\",\"color\":\"#55f163\"},{\"text\":\"|\",\"color\":\"green\"},{\"text\":\"|\",\"color\":\"#63ff55\"},{\"text\":\"|\",\"color\":\"#71ff55\"},{\"text\":\"|\",\"color\":\"#80ff55\"},{\"text\":\"|\",\"color\":\"#8eff55\"},{\"text\":\"|\",\"color\":\"#9cff55\"},{\"text\":\"|\",\"color\":\"#aaff55\"},{\"text\":\"|\",\"color\":\"#b8ff55\"},{\"text\":\"|\",\"color\":\"#c6ff55\"},{\"text\":\"|\",\"color\":\"#d5ff55\"},{\"text\":\"|\",\"color\":\"#e3ff55\"},{\"text\":\"|\",\"color\":\"#f1ff55\"},{\"text\":\"|\",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#f1ff55\"},{\"text\":\"|\",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#fff155\"},{\"text\":\"|\",\"color\":\"#ffe355\"},{\"text\":\"|\",\"color\":\"#ffd555\"},{\"text\":\"|\",\"color\":\"#ffc655\"},{\"text\":\"|\",\"color\":\"#ffb855\"},{\"text\":\"|\",\"color\":\"#ffaa55\"},{\"text\":\"|\",\"color\":\"#ff9c55\"},{\"text\":\"|\",\"color\":\"#ff8e55\"},{\"text\":\"|\",\"color\":\"#ff8055\"},{\"text\":\"|\",\"color\":\"#ff7155\"},{\"text\":\"|\",\"color\":\"#ff6355\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradientMultiColor2() {
    final String input = "<yellow>Woo: <gradient:black:white:black>||||||||||||||||||||||||||||||||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"black\"},{\"text\":\"|\",\"color\":\"#0a0a0a\"},{\"text\":\"|\",\"color\":\"#141414\"},{\"text\":\"|\",\"color\":\"#1d1d1d\"},{\"text\":\"|\",\"color\":\"#272727\"},{\"text\":\"|\",\"color\":\"#313131\"},{\"text\":\"|\",\"color\":\"#3b3b3b\"},{\"text\":\"|\",\"color\":\"#454545\"},{\"text\":\"|\",\"color\":\"#4e4e4e\"},{\"text\":\"|\",\"color\":\"#585858\"},{\"text\":\"|\",\"color\":\"#626262\"},{\"text\":\"|\",\"color\":\"#6c6c6c\"},{\"text\":\"|\",\"color\":\"#767676\"},{\"text\":\"|\",\"color\":\"#808080\"},{\"text\":\"|\",\"color\":\"#898989\"},{\"text\":\"|\",\"color\":\"#939393\"},{\"text\":\"|\",\"color\":\"#9d9d9d\"},{\"text\":\"|\",\"color\":\"#a7a7a7\"},{\"text\":\"|\",\"color\":\"#b1b1b1\"},{\"text\":\"|\",\"color\":\"#bababa\"},{\"text\":\"|\",\"color\":\"#c4c4c4\"},{\"text\":\"|\",\"color\":\"#cecece\"},{\"text\":\"|\",\"color\":\"#d8d8d8\"},{\"text\":\"|\",\"color\":\"#e2e2e2\"},{\"text\":\"|\",\"color\":\"#ebebeb\"},{\"text\":\"|\",\"color\":\"#f5f5f5\"},{\"text\":\"|\",\"color\":\"white\"},{\"text\":\"|\",\"color\":\"#f5f5f5\"},{\"text\":\"|\",\"color\":\"#ebebeb\"},{\"text\":\"|\",\"color\":\"white\"},{\"text\":\"|\",\"color\":\"#f5f5f5\"},{\"text\":\"|\",\"color\":\"#ebebeb\"},{\"text\":\"|\",\"color\":\"#e2e2e2\"},{\"text\":\"|\",\"color\":\"#d8d8d8\"},{\"text\":\"|\",\"color\":\"#cecece\"},{\"text\":\"|\",\"color\":\"#c4c4c4\"},{\"text\":\"|\",\"color\":\"#bababa\"},{\"text\":\"|\",\"color\":\"#b1b1b1\"},{\"text\":\"|\",\"color\":\"#a7a7a7\"},{\"text\":\"|\",\"color\":\"#9d9d9d\"},{\"text\":\"|\",\"color\":\"#939393\"},{\"text\":\"|\",\"color\":\"#898989\"},{\"text\":\"|\",\"color\":\"#808080\"},{\"text\":\"|\",\"color\":\"#767676\"},{\"text\":\"|\",\"color\":\"#6c6c6c\"},{\"text\":\"|\",\"color\":\"#626262\"},{\"text\":\"|\",\"color\":\"#585858\"},{\"text\":\"|\",\"color\":\"#4e4e4e\"},{\"text\":\"|\",\"color\":\"#454545\"},{\"text\":\"|\",\"color\":\"#3b3b3b\"},{\"text\":\"|\",\"color\":\"#313131\"},{\"text\":\"|\",\"color\":\"#272727\"},{\"text\":\"|\",\"color\":\"#1d1d1d\"},{\"text\":\"|\",\"color\":\"#141414\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradientMultiColor2Phase() {
    final String input = "<yellow>Woo: <gradient:black:white:black:10>||||||||||||||||||||||||||||||||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#626262\"},{\"text\":\"|\",\"color\":\"#6c6c6c\"},{\"text\":\"|\",\"color\":\"#767676\"},{\"text\":\"|\",\"color\":\"#808080\"},{\"text\":\"|\",\"color\":\"#898989\"},{\"text\":\"|\",\"color\":\"#939393\"},{\"text\":\"|\",\"color\":\"#9d9d9d\"},{\"text\":\"|\",\"color\":\"#a7a7a7\"},{\"text\":\"|\",\"color\":\"#b1b1b1\"},{\"text\":\"|\",\"color\":\"#bababa\"},{\"text\":\"|\",\"color\":\"#c4c4c4\"},{\"text\":\"|\",\"color\":\"#cecece\"},{\"text\":\"|\",\"color\":\"#d8d8d8\"},{\"text\":\"|\",\"color\":\"#e2e2e2\"},{\"text\":\"|\",\"color\":\"#ebebeb\"},{\"text\":\"|\",\"color\":\"#f5f5f5\"},{\"text\":\"|\",\"color\":\"white\"},{\"text\":\"|\",\"color\":\"#f5f5f5\"},{\"text\":\"|\",\"color\":\"#ebebeb\"},{\"text\":\"|\",\"color\":\"#e2e2e2\"},{\"text\":\"|\",\"color\":\"#d8d8d8\"},{\"text\":\"|\",\"color\":\"#cecece\"},{\"text\":\"|\",\"color\":\"#c4c4c4\"},{\"text\":\"|\",\"color\":\"#bababa\"},{\"text\":\"|\",\"color\":\"#b1b1b1\"},{\"text\":\"|\",\"color\":\"#a7a7a7\"},{\"text\":\"|\",\"color\":\"#9d9d9d\"},{\"text\":\"|\",\"color\":\"#939393\"},{\"text\":\"|\",\"color\":\"#898989\"},{\"text\":\"|\",\"color\":\"#9d9d9d\"},{\"text\":\"|\",\"color\":\"#939393\"},{\"text\":\"|\",\"color\":\"#898989\"},{\"text\":\"|\",\"color\":\"#808080\"},{\"text\":\"|\",\"color\":\"#767676\"},{\"text\":\"|\",\"color\":\"#6c6c6c\"},{\"text\":\"|\",\"color\":\"#626262\"},{\"text\":\"|\",\"color\":\"#585858\"},{\"text\":\"|\",\"color\":\"#4e4e4e\"},{\"text\":\"|\",\"color\":\"#454545\"},{\"text\":\"|\",\"color\":\"#3b3b3b\"},{\"text\":\"|\",\"color\":\"#313131\"},{\"text\":\"|\",\"color\":\"#272727\"},{\"text\":\"|\",\"color\":\"#1d1d1d\"},{\"text\":\"|\",\"color\":\"#141414\"},{\"text\":\"|\",\"color\":\"#0a0a0a\"},{\"text\":\"|\",\"color\":\"black\"},{\"text\":\"|\",\"color\":\"#0a0a0a\"},{\"text\":\"|\",\"color\":\"#141414\"},{\"text\":\"|\",\"color\":\"#1d1d1d\"},{\"text\":\"|\",\"color\":\"#272727\"},{\"text\":\"|\",\"color\":\"#313131\"},{\"text\":\"|\",\"color\":\"#3b3b3b\"},{\"text\":\"|\",\"color\":\"#454545\"},{\"text\":\"|\",\"color\":\"#4e4e4e\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testGradientPhase() {
    final String input = "<yellow>Woo: <gradient:green:blue:10>||||||||||||||||||||||||</gradient>!";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Woo: \",\"color\":\"yellow\"},{\"text\":\"|\",\"color\":\"#55b59f\"},{\"text\":\"|\",\"color\":\"#55aea6\"},{\"text\":\"|\",\"color\":\"#55a6ae\"},{\"text\":\"|\",\"color\":\"#559fb5\"},{\"text\":\"|\",\"color\":\"#5598bc\"},{\"text\":\"|\",\"color\":\"#5590c4\"},{\"text\":\"|\",\"color\":\"#5589cb\"},{\"text\":\"|\",\"color\":\"#5581d3\"},{\"text\":\"|\",\"color\":\"#557ada\"},{\"text\":\"|\",\"color\":\"#5573e1\"},{\"text\":\"|\",\"color\":\"#556be9\"},{\"text\":\"|\",\"color\":\"#5564f0\"},{\"text\":\"|\",\"color\":\"#555cf8\"},{\"text\":\"|\",\"color\":\"blue\"},{\"text\":\"|\",\"color\":\"#555cf8\"},{\"text\":\"|\",\"color\":\"#5564f0\"},{\"text\":\"|\",\"color\":\"#556be9\"},{\"text\":\"|\",\"color\":\"#5573e1\"},{\"text\":\"|\",\"color\":\"#557ada\"},{\"text\":\"|\",\"color\":\"#5581d3\"},{\"text\":\"|\",\"color\":\"#5589cb\"},{\"text\":\"|\",\"color\":\"#5590c4\"},{\"text\":\"|\",\"color\":\"#5598bc\"},{\"text\":\"|\",\"color\":\"#559fb5\"},{\"text\":\"!\",\"color\":\"yellow\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testFont() {
    final String input = "Nothing <font:minecraft:uniform>Uniform <font:minecraft:alt>Alt  </font> Uniform";
    final String expected = "{\"text\":\"\",\"extra\":[{\"text\":\"Nothing \"},{\"text\":\"Uniform \",\"font\":\"minecraft:uniform\"},{\"text\":\"Alt  \",\"font\":\"minecraft:alt\"},{\"text\":\" Uniform\",\"font\":\"minecraft:uniform\"}]}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test // GH-37
  public void testPhil() {
    final String input = "<red><hover:show_text:'Message 1\nMessage 2'>My Message";
    final String expected = "{\"text\":\"My Message\",\"color\":\"red\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"Message 1\\nMessage 2\"}}}";
    final Component comp = MiniMessageParser.parseFormat(input);

    test(comp, expected);
  }

  @Test
  public void testNonBmpCharactersInGradient() {
    assertFalse(Character.isBmpCodePoint("𐌰".codePointAt(0)));

    final String input = "Something <gradient:green:blue:3>𐌰𐌱𐌲</gradient>";
    final Component expected = Component.text()
      .append(Component.text("Something "))
      .append(Component.text("𐌰", TextColor.color(0x5599bb)))
      .append(Component.text("𐌱", TextColor.color(0x5577dd)))
      .append(Component.text("𐌲", NamedTextColor.BLUE))
      .build();

    assertEquals(MiniMessageParser.parseFormat(input), expected);
  }

  private void test(final @NonNull String input, final @NonNull String expected) {
    test(MiniMessageParser.parseFormat(input), expected);
  }

  private void test(final @NonNull Component comp, final @NonNull String expected) {
    assertEquals(expected, GsonComponentSerializer.gson().serialize(comp));
  }
}
