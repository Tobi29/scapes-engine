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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.backends.stb.font

import org.lwjgl.stb.STBTruetype
import org.tobi29.io.viewBufferE
import org.tobi29.scapes.engine.MemoryBuffer

actual inline val STBTT_vmove get() = STBTruetype.STBTT_vmove
actual inline val STBTT_vline get() = STBTruetype.STBTT_vline
actual inline val STBTT_vcurve get() = STBTruetype.STBTT_vcurve
actual inline val STBTT_vcubic get() = STBTruetype.STBTT_vcubic
actual inline val STBTT_MACSTYLE_DONTCARE get() = STBTruetype.STBTT_MACSTYLE_DONTCARE
actual inline val STBTT_MACSTYLE_BOLD get() = STBTruetype.STBTT_MACSTYLE_BOLD
actual inline val STBTT_MACSTYLE_ITALIC get() = STBTruetype.STBTT_MACSTYLE_ITALIC
actual inline val STBTT_MACSTYLE_UNDERSCORE get() = STBTruetype.STBTT_MACSTYLE_UNDERSCORE
actual inline val STBTT_MACSTYLE_NONE get() = STBTruetype.STBTT_MACSTYLE_NONE
actual inline val STBTT_PLATFORM_ID_UNICODE get() = STBTruetype.STBTT_PLATFORM_ID_UNICODE
actual inline val STBTT_PLATFORM_ID_MAC get() = STBTruetype.STBTT_PLATFORM_ID_MAC
actual inline val STBTT_PLATFORM_ID_ISO get() = STBTruetype.STBTT_PLATFORM_ID_ISO
actual inline val STBTT_PLATFORM_ID_MICROSOFT get() = STBTruetype.STBTT_PLATFORM_ID_MICROSOFT
actual inline val STBTT_UNICODE_EID_UNICODE_1_0 get() = STBTruetype.STBTT_UNICODE_EID_UNICODE_1_0
actual inline val STBTT_UNICODE_EID_UNICODE_1_1 get() = STBTruetype.STBTT_UNICODE_EID_UNICODE_1_1
actual inline val STBTT_UNICODE_EID_ISO_10646 get() = STBTruetype.STBTT_UNICODE_EID_ISO_10646
actual inline val STBTT_UNICODE_EID_UNICODE_2_0_BMP get() = STBTruetype.STBTT_UNICODE_EID_UNICODE_2_0_BMP
actual inline val STBTT_UNICODE_EID_UNICODE_2_0_FULL get() = STBTruetype.STBTT_UNICODE_EID_UNICODE_2_0_FULL
actual inline val STBTT_MS_EID_SYMBOL get() = STBTruetype.STBTT_MS_EID_SYMBOL
actual inline val STBTT_MS_EID_UNICODE_BMP get() = STBTruetype.STBTT_MS_EID_UNICODE_BMP
actual inline val STBTT_MS_EID_SHIFTJIS get() = STBTruetype.STBTT_MS_EID_SHIFTJIS
actual inline val STBTT_MS_EID_UNICODE_FULL get() = STBTruetype.STBTT_MS_EID_UNICODE_FULL
actual inline val STBTT_MAC_EID_ROMAN get() = STBTruetype.STBTT_MAC_EID_ROMAN
actual inline val STBTT_MAC_EID_JAPANESE get() = STBTruetype.STBTT_MAC_EID_JAPANESE
actual inline val STBTT_MAC_EID_CHINESE_TRAD get() = STBTruetype.STBTT_MAC_EID_CHINESE_TRAD
actual inline val STBTT_MAC_EID_KOREAN get() = STBTruetype.STBTT_MAC_EID_KOREAN
actual inline val STBTT_MAC_EID_ARABIC get() = STBTruetype.STBTT_MAC_EID_ARABIC
actual inline val STBTT_MAC_EID_HEBREW get() = STBTruetype.STBTT_MAC_EID_HEBREW
actual inline val STBTT_MAC_EID_GREEK get() = STBTruetype.STBTT_MAC_EID_GREEK
actual inline val STBTT_MAC_EID_RUSSIAN get() = STBTruetype.STBTT_MAC_EID_RUSSIAN
actual inline val STBTT_MS_LANG_ENGLISH get() = STBTruetype.STBTT_MS_LANG_ENGLISH
actual inline val STBTT_MS_LANG_CHINESE get() = STBTruetype.STBTT_MS_LANG_CHINESE
actual inline val STBTT_MS_LANG_DUTCH get() = STBTruetype.STBTT_MS_LANG_DUTCH
actual inline val STBTT_MS_LANG_FRENCH get() = STBTruetype.STBTT_MS_LANG_FRENCH
actual inline val STBTT_MS_LANG_GERMAN get() = STBTruetype.STBTT_MS_LANG_GERMAN
actual inline val STBTT_MS_LANG_HEBREW get() = STBTruetype.STBTT_MS_LANG_HEBREW
actual inline val STBTT_MS_LANG_ITALIAN get() = STBTruetype.STBTT_MS_LANG_ITALIAN
actual inline val STBTT_MS_LANG_JAPANESE get() = STBTruetype.STBTT_MS_LANG_JAPANESE
actual inline val STBTT_MS_LANG_KOREAN get() = STBTruetype.STBTT_MS_LANG_KOREAN
actual inline val STBTT_MS_LANG_RUSSIAN get() = STBTruetype.STBTT_MS_LANG_RUSSIAN
actual inline val STBTT_MS_LANG_SPANISH get() = STBTruetype.STBTT_MS_LANG_SPANISH
actual inline val STBTT_MS_LANG_SWEDISH get() = STBTruetype.STBTT_MS_LANG_SWEDISH
actual inline val STBTT_MAC_LANG_ENGLISH get() = STBTruetype.STBTT_MAC_LANG_ENGLISH
actual inline val STBTT_MAC_LANG_ARABIC get() = STBTruetype.STBTT_MAC_LANG_ARABIC
actual inline val STBTT_MAC_LANG_DUTCH get() = STBTruetype.STBTT_MAC_LANG_DUTCH
actual inline val STBTT_MAC_LANG_FRENCH get() = STBTruetype.STBTT_MAC_LANG_FRENCH
actual inline val STBTT_MAC_LANG_GERMAN get() = STBTruetype.STBTT_MAC_LANG_GERMAN
actual inline val STBTT_MAC_LANG_HEBREW get() = STBTruetype.STBTT_MAC_LANG_HEBREW
actual inline val STBTT_MAC_LANG_ITALIAN get() = STBTruetype.STBTT_MAC_LANG_ITALIAN
actual inline val STBTT_MAC_LANG_JAPANESE get() = STBTruetype.STBTT_MAC_LANG_JAPANESE
actual inline val STBTT_MAC_LANG_KOREAN get() = STBTruetype.STBTT_MAC_LANG_KOREAN
actual inline val STBTT_MAC_LANG_RUSSIAN get() = STBTruetype.STBTT_MAC_LANG_RUSSIAN
actual inline val STBTT_MAC_LANG_SPANISH get() = STBTruetype.STBTT_MAC_LANG_SPANISH
actual inline val STBTT_MAC_LANG_SWEDISH get() = STBTruetype.STBTT_MAC_LANG_SWEDISH
actual inline val STBTT_MAC_LANG_CHINESE_SIMPLIFIED get() = STBTruetype.STBTT_MAC_LANG_CHINESE_SIMPLIFIED
actual inline val STBTT_MAC_LANG_CHINESE_TRAD get() = STBTruetype.STBTT_MAC_LANG_CHINESE_TRAD

actual inline fun stbtt_BakeFontBitmap(
    data: MemoryBuffer,
    offset: Int,
    pixel_height: Float,
    pixels: MemoryBuffer,
    pw: Int,
    ph: Int,
    first_char: Int,
    chardata: STBTTBakedCharBuffer
) = STBTruetype.stbtt_BakeFontBitmap(
    data.slice(offset).byteBuffer,
    pixel_height,
    pixels.byteBuffer,
    pw,
    ph,
    first_char,
    chardata
)

actual inline fun stbtt_GetBakedQuad(
    chardata: STBTTBakedCharBuffer,
    pw: Int,
    ph: Int,
    char_index: Int,
    xpos: FloatArray,
    ypos: FloatArray,
    q: STBTTAlignedQuad,
    opengl_fillrule: Boolean
) = STBTruetype.stbtt_GetBakedQuad(
    chardata,
    pw,
    ph,
    char_index,
    xpos,
    ypos,
    q,
    opengl_fillrule
)

actual inline fun stbtt_PackBegin(
    spc: STBTTPackContext,
    pixels: MemoryBuffer?,
    width: Int,
    height: Int,
    stride_in_bytes: Int,
    padding: Int,
    alloc_context: Long
) = STBTruetype.stbtt_PackBegin(
    spc,
    pixels?.byteBuffer,
    width,
    height,
    stride_in_bytes,
    padding,
    alloc_context
)

actual inline fun stbtt_PackBegin(
    spc: STBTTPackContext,
    pixels: MemoryBuffer?,
    width: Int,
    height: Int,
    stride_in_bytes: Int,
    padding: Int
) = STBTruetype.stbtt_PackBegin(
    spc,
    pixels?.byteBuffer,
    width,
    height,
    stride_in_bytes,
    padding
)

actual inline fun stbtt_PackEnd(
    spc: STBTTPackContext
) = STBTruetype.stbtt_PackEnd(
    spc
)

actual inline fun stbtt_point_size(
    font_size: Int
) = STBTruetype.STBTT_POINT_SIZE(
    font_size
)

actual inline fun stbtt_PackFontRange(
    spc: STBTTPackContext,
    fontdata: MemoryBuffer,
    font_index: Int,
    font_size: Float,
    first_unicode_char_in_range: Int,
    chardata_for_range: STBTTPackedcharBuffer
) = STBTruetype.stbtt_PackFontRange(
    spc,
    fontdata.byteBuffer,
    font_index,
    font_size,
    first_unicode_char_in_range,
    chardata_for_range
)

actual inline fun stbtt_PackFontRanges(
    spc: STBTTPackContext,
    fontdata: MemoryBuffer,
    font_index: Int,
    ranges: STBTTPackRangeBuffer
) = STBTruetype.stbtt_PackFontRanges(
    spc,
    fontdata.byteBuffer,
    font_index,
    ranges
)

actual inline fun stbtt_PackSetOversampling(
    spc: STBTTPackContext,
    h_oversample: Int,
    v_oversample: Int
) = STBTruetype.stbtt_PackSetOversampling(
    spc,
    h_oversample,
    v_oversample
)

actual inline fun stbtt_GetPackedQuad(
    chardata: STBTTPackedcharBuffer,
    pw: Int,
    ph: Int,
    char_index: Int,
    xpos: FloatArray,
    ypos: FloatArray,
    q: STBTTAlignedQuad,
    align_to_integer: Boolean
) = STBTruetype.stbtt_GetPackedQuad(
    chardata,
    pw,
    ph,
    char_index,
    xpos,
    ypos,
    q,
    align_to_integer
)

actual inline fun stbtt_PackFontRangesGatherRects(
    spc: STBTTPackContext,
    info: STBTTFontinfo,
    ranges: STBTTPackRangeBuffer,
    rects: STBRPRectBuffer
) = STBTruetype.stbtt_PackFontRangesGatherRects(
    spc,
    info,
    ranges,
    rects
)

actual inline fun stbtt_PackFontRangesPackRects(
    spc: STBTTPackContext,
    rects: STBRPRectBuffer
) = STBTruetype.stbtt_PackFontRangesPackRects(
    spc,
    rects
)

actual inline fun stbtt_PackFontRangesRenderIntoRects(
    spc: STBTTPackContext,
    info: STBTTFontinfo,
    ranges: STBTTPackRangeBuffer,
    rects: STBRPRectBuffer
) = STBTruetype.stbtt_PackFontRangesRenderIntoRects(
    spc,
    info,
    ranges,
    rects
)

actual inline fun stbtt_GetNumberOfFonts(
    data: MemoryBuffer
) = STBTruetype.stbtt_GetNumberOfFonts(
    data.byteBuffer
)

actual inline fun stbtt_GetFontOffsetForIndex(
    data: MemoryBuffer,
    index: Int
) = STBTruetype.stbtt_GetFontOffsetForIndex(
    data.byteBuffer,
    index
)

actual inline fun stbtt_InitFont(
    info: STBTTFontinfo,
    data: MemoryBuffer,
    offset: Int
) = STBTruetype.stbtt_InitFont(
    info,
    data.byteBuffer,
    offset
)

actual inline fun stbtt_InitFont(
    info: STBTTFontinfo,
    data: MemoryBuffer
) = STBTruetype.stbtt_InitFont(
    info,
    data.byteBuffer
)

actual inline fun stbtt_FindGlyphIndex(
    info: STBTTFontinfo,
    unicode_codepoint: Int
) = STBTruetype.stbtt_FindGlyphIndex(
    info,
    unicode_codepoint
)

actual inline fun stbtt_ScaleForPixelHeight(
    info: STBTTFontinfo,
    pixels: Float
) = STBTruetype.stbtt_ScaleForPixelHeight(
    info,
    pixels
)

actual inline fun stbtt_ScaleForMappingEmToPixels(
    info: STBTTFontinfo,
    pixels: Float
) = STBTruetype.stbtt_ScaleForMappingEmToPixels(
    info,
    pixels
)

actual inline fun stbtt_GetFontVMetrics(
    info: STBTTFontinfo,
    ascent: IntArray?,
    descent: IntArray?,
    lineGap: IntArray?
) = STBTruetype.stbtt_GetFontVMetrics(
    info,
    ascent,
    descent,
    lineGap
)

actual inline fun stbtt_GetFontVMetricsOS2(
    info: STBTTFontinfo,
    typoAscent: IntArray?,
    typoDescent: IntArray?,
    typoLineGap: IntArray?
) = STBTruetype.stbtt_GetFontVMetricsOS2(
    info,
    typoAscent,
    typoDescent,
    typoLineGap
)

actual inline fun stbtt_GetFontBoundingBox(
    info: STBTTFontinfo,
    x0: IntArray,
    y0: IntArray,
    x1: IntArray,
    y1: IntArray
) = STBTruetype.stbtt_GetFontBoundingBox(
    info,
    x0,
    y0,
    x1,
    y1
)

actual inline fun stbtt_GetCodepointHMetrics(
    info: STBTTFontinfo,
    codepoint: Int,
    advanceWidth: IntArray?,
    leftSideBearing: IntArray?
) = STBTruetype.stbtt_GetCodepointHMetrics(
    info,
    codepoint,
    advanceWidth,
    leftSideBearing
)

actual inline fun stbtt_GetCodepointKernAdvance(
    info: STBTTFontinfo,
    ch1: Int,
    ch2: Int
) = STBTruetype.stbtt_GetCodepointKernAdvance(
    info,
    ch1,
    ch2
)

actual inline fun stbtt_GetCodepointBox(
    info: STBTTFontinfo,
    codepoint: Int,
    x0: IntArray?,
    y0: IntArray?,
    x1: IntArray?,
    y1: IntArray?
) = STBTruetype.stbtt_GetCodepointBox(
    info,
    codepoint,
    x0,
    y0,
    x1,
    y1
)

actual inline fun stbtt_GetGlyphHMetrics(
    info: STBTTFontinfo,
    glyph_index: Int,
    advanceWidth: IntArray?,
    leftSideBearing: IntArray?
) = STBTruetype.stbtt_GetGlyphHMetrics(
    info,
    glyph_index,
    advanceWidth,
    leftSideBearing
)

actual inline fun stbtt_GetGlyphKernAdvance(
    info: STBTTFontinfo,
    glyph1: Int,
    glyph2: Int
) = STBTruetype.stbtt_GetGlyphKernAdvance(
    info,
    glyph1,
    glyph2
)

actual inline fun stbtt_GetGlyphBox(
    info: STBTTFontinfo,
    glyph_index: Int,
    x0: IntArray?,
    y0: IntArray?,
    x1: IntArray?,
    y1: IntArray?
) = STBTruetype.stbtt_GetGlyphBox(
    info,
    glyph_index,
    x0,
    y0,
    x1,
    y1
)

actual inline fun stbtt_IsGlyphEmpty(
    info: STBTTFontinfo,
    glyph_index: Int
) = STBTruetype.stbtt_IsGlyphEmpty(
    info,
    glyph_index
)

actual inline fun stbtt_GetCodepointShape(
    info: STBTTFontinfo,
    unicode_codepoint: Int
) = STBTruetype.stbtt_GetCodepointShape(
    info,
    unicode_codepoint
)

actual inline fun stbtt_GetGlyphShape(
    info: STBTTFontinfo,
    glyph_index: Int
) = STBTruetype.stbtt_GetGlyphShape(
    info,
    glyph_index
)

actual inline fun stbtt_FreeShape(
    info: STBTTFontinfo,
    vertices: STBTTVertexBuffer
) = STBTruetype.stbtt_FreeShape(
    info,
    vertices
)

actual inline fun stbtt_FreeBitmap(
    bitmap: MemoryBuffer,
    userdata: Long
) = STBTruetype.stbtt_FreeBitmap(
    bitmap.byteBuffer,
    userdata
)

actual inline fun stbtt_FreeBitmap(
    bitmap: MemoryBuffer
) = STBTruetype.stbtt_FreeBitmap(
    bitmap.byteBuffer
)

actual inline fun stbtt_GetCodepointBitmap(
    info: STBTTFontinfo,
    scale_x: Float,
    scale_y: Float,
    codepoint: Int,
    width: IntArray,
    height: IntArray,
    xoff: IntArray?,
    yoff: IntArray?
) = STBTruetype.stbtt_GetCodepointBitmap(
    info,
    scale_x,
    scale_y,
    codepoint,
    width,
    height,
    xoff,
    yoff
)?.viewBufferE

actual inline fun stbtt_GetCodepointBitmapSubpixel(
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
) = STBTruetype.stbtt_GetCodepointBitmapSubpixel(
    info,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    codepoint,
    width,
    height,
    xoff,
    yoff
)?.viewBufferE

actual inline fun stbtt_MakeCodepointBitmap(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    codepoint: Int
) = STBTruetype.stbtt_MakeCodepointBitmap(
    info,
    output.byteBuffer,
    out_w,
    out_h,
    out_stride,
    scale_x,
    scale_y,
    codepoint
)

actual inline fun stbtt_MakeCodepointBitmapSubpixel(
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
) = STBTruetype.stbtt_MakeCodepointBitmapSubpixel(
    info,
    output.byteBuffer,
    out_w,
    out_h,
    out_stride,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    codepoint
)

actual inline fun stbtt_MakeCodepointBitmapSubpixelPrefilter(
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
) = STBTruetype.stbtt_MakeCodepointBitmapSubpixelPrefilter(
    info,
    output.byteBuffer,
    out_w,
    out_h,
    out_stride,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    oversample_x,
    oversample_y,
    sub_x,
    sub_y,
    codepoint
)

actual inline fun stbtt_GetCodepointBitmapBox(
    font: STBTTFontinfo,
    codepoint: Int,
    scale_x: Float,
    scale_y: Float,
    ix0: IntArray?,
    iy0: IntArray?,
    ix1: IntArray?,
    iy1: IntArray?
) = STBTruetype.stbtt_GetCodepointBitmapBox(
    font,
    codepoint,
    scale_x,
    scale_y,
    ix0,
    iy0,
    ix1,
    iy1
)

actual inline fun stbtt_GetCodepointBitmapBoxSubpixel(
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
) = STBTruetype.stbtt_GetCodepointBitmapBoxSubpixel(
    font,
    codepoint,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    ix0,
    iy0,
    ix1,
    iy1
)

actual inline fun stbtt_GetGlyphBitmap(
    info: STBTTFontinfo,
    scale_x: Float,
    scale_y: Float,
    glyph: Int,
    width: IntArray,
    height: IntArray,
    xoff: IntArray?,
    yoff: IntArray?
) = STBTruetype.stbtt_GetGlyphBitmap(
    info,
    scale_x,
    scale_y,
    glyph,
    width,
    height,
    xoff,
    yoff
)?.viewBufferE

actual inline fun stbtt_GetGlyphBitmapSubpixel(
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
) = STBTruetype.stbtt_GetGlyphBitmapSubpixel(
    info,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    glyph,
    width,
    height,
    xoff,
    yoff
)?.viewBufferE

actual inline fun stbtt_MakeGlyphBitmap(
    info: STBTTFontinfo,
    output: MemoryBuffer,
    out_w: Int,
    out_h: Int,
    out_stride: Int,
    scale_x: Float,
    scale_y: Float,
    glyph: Int
) = STBTruetype.stbtt_MakeGlyphBitmap(
    info,
    output.byteBuffer,
    out_w,
    out_h,
    out_stride,
    scale_x,
    scale_y,
    glyph
)

actual inline fun stbtt_MakeGlyphBitmapSubpixel(
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
) = STBTruetype.stbtt_MakeGlyphBitmapSubpixel(
    info,
    output.byteBuffer,
    out_w,
    out_h,
    out_stride,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    glyph
)

actual inline fun stbtt_MakeGlyphBitmapSubpixelPrefilter(
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
) = STBTruetype.stbtt_MakeGlyphBitmapSubpixelPrefilter(
    info,
    output.byteBuffer,
    out_w,
    out_h,
    out_stride,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    oversample_x,
    oversample_y,
    sub_x,
    sub_y,
    glyph
)

actual inline fun stbtt_GetGlyphBitmapBox(
    font: STBTTFontinfo,
    glyph: Int,
    scale_x: Float,
    scale_y: Float,
    ix0: IntArray?,
    iy0: IntArray?,
    ix1: IntArray?,
    iy1: IntArray?
) = STBTruetype.stbtt_GetGlyphBitmapBox(
    font,
    glyph,
    scale_x,
    scale_y,
    ix0,
    iy0,
    ix1,
    iy1
)

actual inline fun stbtt_GetGlyphBitmapBoxSubpixel(
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
) = STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(
    font,
    glyph,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    ix0,
    iy0,
    ix1,
    iy1
)

actual inline fun stbtt_Rasterize(
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
) = STBTruetype.stbtt_Rasterize(
    result,
    flatness_in_pixels,
    vertices,
    scale_x,
    scale_y,
    shift_x,
    shift_y,
    x_off,
    y_off,
    invert
)

actual inline fun stbtt_FreeSDF(
    bitmap: MemoryBuffer,
    userdata: Long
) = STBTruetype.stbtt_FreeSDF(
    bitmap.byteBuffer,
    userdata
)

actual inline fun stbtt_FreeSDF(
    bitmap: MemoryBuffer
) = STBTruetype.stbtt_FreeSDF(
    bitmap.byteBuffer
)

actual inline fun stbtt_GetGlyphSDF(
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
) = STBTruetype.stbtt_GetGlyphSDF(
    font,
    scale,
    glyph,
    padding,
    onedge_value,
    pixel_dist_scale,
    width,
    height,
    xoff,
    yoff
)?.viewBufferE

actual inline fun stbtt_GetCodepointSDF(
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
) = STBTruetype.stbtt_GetCodepointSDF(
    font,
    scale,
    codepoint,
    padding,
    onedge_value,
    pixel_dist_scale,
    width,
    height,
    xoff,
    yoff
)?.viewBufferE

actual inline fun stbtt_FindMatchingFont(
    fontdata: MemoryBuffer,
    name: String,
    flags: Int
) = STBTruetype.stbtt_FindMatchingFont(
    fontdata.byteBuffer,
    name,
    flags
)

actual inline fun stbtt_CompareUTF8toUTF16_bigendian(
    s1: MemoryBuffer,
    s2: MemoryBuffer
) = STBTruetype.stbtt_CompareUTF8toUTF16_bigendian(
    s1.byteBuffer,
    s2.byteBuffer
)

actual inline fun stbtt_GetFontNameString(
    font: STBTTFontinfo,
    platformID: Int,
    encodingID: Int,
    languageID: Int,
    nameID: Int
) = STBTruetype.stbtt_GetFontNameString(
    font,
    platformID,
    encodingID,
    languageID,
    nameID
)?.viewBufferE
