/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.backends.stb.font

import org.tobi29.scapes.engine.MemoryBuffer
import org.tobi29.scapes.engine.MemoryBufferExternal

expect val STBTT_vmove: Byte
expect val STBTT_vline: Byte
expect val STBTT_vcurve: Byte
expect val STBTT_vcubic: Byte
expect val STBTT_MACSTYLE_DONTCARE: Int
expect val STBTT_MACSTYLE_BOLD: Int
expect val STBTT_MACSTYLE_ITALIC: Int
expect val STBTT_MACSTYLE_UNDERSCORE: Int
expect val STBTT_MACSTYLE_NONE: Int
expect val STBTT_PLATFORM_ID_UNICODE: Int
expect val STBTT_PLATFORM_ID_MAC: Int
expect val STBTT_PLATFORM_ID_ISO: Int
expect val STBTT_PLATFORM_ID_MICROSOFT: Int
expect val STBTT_UNICODE_EID_UNICODE_1_0: Int
expect val STBTT_UNICODE_EID_UNICODE_1_1: Int
expect val STBTT_UNICODE_EID_ISO_10646: Int
expect val STBTT_UNICODE_EID_UNICODE_2_0_BMP: Int
expect val STBTT_UNICODE_EID_UNICODE_2_0_FULL: Int
expect val STBTT_MS_EID_SYMBOL: Int
expect val STBTT_MS_EID_UNICODE_BMP: Int
expect val STBTT_MS_EID_SHIFTJIS: Int
expect val STBTT_MS_EID_UNICODE_FULL: Int
expect val STBTT_MAC_EID_ROMAN: Int
expect val STBTT_MAC_EID_JAPANESE: Int
expect val STBTT_MAC_EID_CHINESE_TRAD: Int
expect val STBTT_MAC_EID_KOREAN: Int
expect val STBTT_MAC_EID_ARABIC: Int
expect val STBTT_MAC_EID_HEBREW: Int
expect val STBTT_MAC_EID_GREEK: Int
expect val STBTT_MAC_EID_RUSSIAN: Int
expect val STBTT_MS_LANG_ENGLISH: Int
expect val STBTT_MS_LANG_CHINESE: Int
expect val STBTT_MS_LANG_DUTCH: Int
expect val STBTT_MS_LANG_FRENCH: Int
expect val STBTT_MS_LANG_GERMAN: Int
expect val STBTT_MS_LANG_HEBREW: Int
expect val STBTT_MS_LANG_ITALIAN: Int
expect val STBTT_MS_LANG_JAPANESE: Int
expect val STBTT_MS_LANG_KOREAN: Int
expect val STBTT_MS_LANG_RUSSIAN: Int
expect val STBTT_MS_LANG_SPANISH: Int
expect val STBTT_MS_LANG_SWEDISH: Int
expect val STBTT_MAC_LANG_ENGLISH: Int
expect val STBTT_MAC_LANG_ARABIC: Int
expect val STBTT_MAC_LANG_DUTCH: Int
expect val STBTT_MAC_LANG_FRENCH: Int
expect val STBTT_MAC_LANG_GERMAN: Int
expect val STBTT_MAC_LANG_HEBREW: Int
expect val STBTT_MAC_LANG_ITALIAN: Int
expect val STBTT_MAC_LANG_JAPANESE: Int
expect val STBTT_MAC_LANG_KOREAN: Int
expect val STBTT_MAC_LANG_RUSSIAN: Int
expect val STBTT_MAC_LANG_SPANISH: Int
expect val STBTT_MAC_LANG_SWEDISH: Int
expect val STBTT_MAC_LANG_CHINESE_SIMPLIFIED: Int
expect val STBTT_MAC_LANG_CHINESE_TRAD: Int

expect fun stbtt_BakeFontBitmap(
    data: MemoryBuffer,
    offset: Int,
    pixel_height: Float,
    pixels: MemoryBuffer,
    pw: Int,
    ph: Int,
    first_char: Int,
    chardata: STBTTBakedCharBuffer
): Int

expect fun stbtt_GetBakedQuad(
    chardata: STBTTBakedCharBuffer,
    pw: Int,
    ph: Int,
    char_index: Int,
    xpos: FloatArray,
    ypos: FloatArray,
    q: STBTTAlignedQuad,
    opengl_fillrule: Boolean
)

expect fun stbtt_PackBegin(
    spc: STBTTPackContext,
    pixels: MemoryBuffer?,
    width: Int,
    height: Int,
    stride_in_bytes: Int,
    padding: Int,
    alloc_context: Long
): Boolean

expect fun stbtt_PackBegin(
    spc: STBTTPackContext,
    pixels: MemoryBuffer?,
    width: Int,
    height: Int,
    stride_in_bytes: Int,
    padding: Int
): Boolean

expect fun stbtt_PackEnd(
    spc: STBTTPackContext
)

expect fun stbtt_point_size(
    font_size: Int
): Int

expect fun stbtt_PackFontRange(
    spc: STBTTPackContext,
    fontdata: MemoryBuffer,
    font_index: Int,
    font_size: Float,
    first_unicode_char_in_range: Int,
    chardata_for_range: STBTTPackedcharBuffer
): Boolean

expect fun stbtt_PackFontRanges(
    spc: STBTTPackContext,
    fontdata: MemoryBuffer,
    font_index: Int,
    ranges: STBTTPackRangeBuffer
): Boolean

expect fun stbtt_PackSetOversampling(
    spc: STBTTPackContext,
    h_oversample: Int,
    v_oversample: Int
)

expect fun stbtt_GetPackedQuad(
    chardata: STBTTPackedcharBuffer,
    pw: Int,
    ph: Int,
    char_index: Int,
    xpos: FloatArray,
    ypos: FloatArray,
    q: STBTTAlignedQuad,
    align_to_integer: Boolean
)

expect fun stbtt_PackFontRangesGatherRects(
    spc: STBTTPackContext,
    info: STBTTFontinfo,
    ranges: STBTTPackRangeBuffer,
    rects: STBRPRectBuffer
): Int

expect fun stbtt_PackFontRangesPackRects(
    spc: STBTTPackContext,
    rects: STBRPRectBuffer
)

expect fun stbtt_PackFontRangesRenderIntoRects(
    spc: STBTTPackContext,
    info: STBTTFontinfo,
    ranges: STBTTPackRangeBuffer,
    rects: STBRPRectBuffer
): Boolean

expect fun stbtt_GetNumberOfFonts(
    data: MemoryBuffer
): Int

expect fun stbtt_GetFontOffsetForIndex(
    data: MemoryBuffer,
    index: Int
): Int

expect fun stbtt_InitFont(
    info: STBTTFontinfo,
    data: MemoryBuffer,
    offset: Int
): Boolean

expect fun stbtt_InitFont(
    info: STBTTFontinfo,
    data: MemoryBuffer
): Boolean

expect fun stbtt_FindGlyphIndex(
    info: STBTTFontinfo,
    unicode_codepoint: Int
): Int

expect fun stbtt_ScaleForPixelHeight(
    info: STBTTFontinfo,
    pixels: Float
): Float

expect fun stbtt_ScaleForMappingEmToPixels(
    info: STBTTFontinfo,
    pixels: Float
): Float

expect fun stbtt_GetFontVMetrics(
    info: STBTTFontinfo,
    ascent: IntArray?,
    descent: IntArray?,
    lineGap: IntArray?
)

expect fun stbtt_GetFontVMetricsOS2(
    info: STBTTFontinfo,
    typoAscent: IntArray?,
    typoDescent: IntArray?,
    typoLineGap: IntArray?
): Boolean

expect fun stbtt_GetFontBoundingBox(
    info: STBTTFontinfo,
    x0: IntArray,
    y0: IntArray,
    x1: IntArray,
    y1: IntArray
)

expect fun stbtt_GetCodepointHMetrics(
    info: STBTTFontinfo,
    codepoint: Int,
    advanceWidth: IntArray?,
    leftSideBearing: IntArray?
)

expect fun stbtt_GetCodepointKernAdvance(
    info: STBTTFontinfo,
    ch1: Int,
    ch2: Int
): Int

expect fun stbtt_GetCodepointBox(
    info: STBTTFontinfo,
    codepoint: Int,
    x0: IntArray?,
    y0: IntArray?,
    x1: IntArray?,
    y1: IntArray?
): Boolean

expect fun stbtt_GetGlyphHMetrics(
    info: STBTTFontinfo,
    glyph_index: Int,
    advanceWidth: IntArray?,
    leftSideBearing: IntArray?
)

expect fun stbtt_GetGlyphKernAdvance(
    info: STBTTFontinfo,
    glyph1: Int,
    glyph2: Int
): Int

expect fun stbtt_GetGlyphBox(
    info: STBTTFontinfo,
    glyph_index: Int,
    x0: IntArray?,
    y0: IntArray?,
    x1: IntArray?,
    y1: IntArray?
): Boolean

expect fun stbtt_IsGlyphEmpty(
    info: STBTTFontinfo,
    glyph_index: Int
): Boolean

expect fun stbtt_GetCodepointShape(
    info: STBTTFontinfo,
    unicode_codepoint: Int
): STBTTVertexBuffer?

expect fun stbtt_GetGlyphShape(
    info: STBTTFontinfo,
    glyph_index: Int
): STBTTVertexBuffer?

expect fun stbtt_FreeShape(
    info: STBTTFontinfo,
    vertices: STBTTVertexBuffer
)

expect fun stbtt_FreeBitmap(
    bitmap: MemoryBufferExternal,
    userdata: Long
)

expect fun stbtt_FreeBitmap(
    bitmap: MemoryBufferExternal
)

expect fun stbtt_GetCodepointBitmap(
    info: STBTTFontinfo,
    scale_x: Float,
    scale_y: Float,
    codepoint: Int,
    width: IntArray,
    height: IntArray,
    xoff: IntArray?,
    yoff: IntArray?
): MemoryBufferExternal?

expect fun stbtt_GetCodepointBitmapSubpixel(
    info: STBTTFontinfo,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    codepoint: Int,
    width: IntArray,
    height: IntArray,
    xoff: IntArray?,
    yoff: IntArray?
): MemoryBufferExternal?

expect fun stbtt_MakeCodepointBitmap(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    codepoint: Int
)

expect fun stbtt_MakeCodepointBitmapSubpixel(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    codepoint: Int
)

expect fun stbtt_MakeCodepointBitmapSubpixelPrefilter(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    oversample_x: Int,
    oversample_y: Int,
    sub_x: FloatArray,
    sub_y: FloatArray,
    codepoint: Int
)

expect fun stbtt_GetCodepointBitmapBox(
    font: STBTTFontinfo,
    codepoint: Int,
    scale_x: Float,
    scale_y: Float,
    ix0: IntArray?,
    iy0: IntArray?,
    ix1: IntArray?,
    iy1: IntArray?
)

expect fun stbtt_GetCodepointBitmapBoxSubpixel(
    font: STBTTFontinfo,
    codepoint: Int,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    ix0: IntArray?,
    iy0: IntArray?,
    ix1: IntArray?,
    iy1: IntArray?
)

expect fun stbtt_GetGlyphBitmap(
    info: STBTTFontinfo,
    scale_x: Float,
    scale_y: Float,
    glyph: Int,
    width: IntArray,
    height: IntArray,
    xoff: IntArray?,
    yoff: IntArray?
): MemoryBufferExternal?

expect fun stbtt_GetGlyphBitmapSubpixel(
    info: STBTTFontinfo,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    glyph: Int,
    width: IntArray,
    height: IntArray,
    xoff: IntArray?,
    yoff: IntArray?
): MemoryBufferExternal?

expect fun stbtt_MakeGlyphBitmap(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    glyph: Int
)

expect fun stbtt_MakeGlyphBitmapSubpixel(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    glyph: Int
)

expect fun stbtt_MakeGlyphBitmapSubpixelPrefilter(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    oversample_x: Int,
    oversample_y: Int,
    sub_x: FloatArray,
    sub_y: FloatArray,
    glyph: Int
)

expect fun stbtt_GetGlyphBitmapBox(
    font: STBTTFontinfo,
    glyph: Int,
    scale_x: Float,
    scale_y: Float,
    ix0: IntArray?,
    iy0: IntArray?,
    ix1: IntArray?,
    iy1: IntArray?
)

expect fun stbtt_GetGlyphBitmapBoxSubpixel(
    font: STBTTFontinfo,
    glyph: Int,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    ix0: IntArray?,
    iy0: IntArray?,
    ix1: IntArray?,
    iy1: IntArray?
)

expect fun stbtt_Rasterize(
    result: STBTTBitmap,
    flatness_in_pixels: Float,
    vertices: STBTTVertexBuffer,
    scale_x: Float,
    scale_y: Float,
    shift_x: Float,
    shift_y: Float,
    x_off: Int,
    y_off: Int,
    invert: Boolean
)

expect fun stbtt_FreeSDF(
    bitmap: MemoryBufferExternal,
    userdata: Long
)

expect fun stbtt_FreeSDF(
    bitmap: MemoryBufferExternal
)

expect fun stbtt_GetGlyphSDF(
    font: STBTTFontinfo,
    scale: Float,
    glyph: Int,
    padding: Int,
    onedge_value: Byte,
    pixel_dist_scale: Float,
    width: IntArray,
    height: IntArray,
    xoff: IntArray,
    yoff: IntArray
): MemoryBufferExternal?

expect fun stbtt_GetCodepointSDF(
    font: STBTTFontinfo,
    scale: Float,
    codepoint: Int,
    padding: Int,
    onedge_value: Byte,
    pixel_dist_scale: Float,
    width: IntArray,
    height: IntArray,
    xoff: IntArray,
    yoff: IntArray
): MemoryBufferExternal?

expect fun stbtt_FindMatchingFont(
    fontdata: MemoryBuffer,
    name: String,
    flags: Int
): Int

expect fun stbtt_CompareUTF8toUTF16_bigendian(
    s1: MemoryBuffer,
    s2: MemoryBuffer
): Boolean

expect fun stbtt_GetFontNameString(
    font: STBTTFontinfo,
    platformID: Int,
    encodingID: Int,
    languageID: Int,
    nameID: Int
): MemoryBufferExternal?
