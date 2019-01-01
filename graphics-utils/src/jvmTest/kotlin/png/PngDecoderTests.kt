/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.graphics.png

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldBe
import org.tobi29.assertions.shouldThrow
import org.tobi29.graphics.height
import org.tobi29.graphics.width
import org.tobi29.io.IOException
import org.tobi29.io.classpath.ClasspathPath

object PngDecoderTests : Spek({
    describe("decoding a png file") {
        data(
            { a -> "decoding $a" },
            data("basi0g01.png", Unit),
            data("basi0g02.png", Unit),
            data("basi0g04.png", Unit),
            data("basi0g08.png", Unit),
            data("basi0g16.png", Unit),
            data("basi2c08.png", Unit),
            data("basi2c16.png", Unit),
            data("basi3p01.png", Unit),
            data("basi3p02.png", Unit),
            data("basi3p04.png", Unit),
            data("basi3p08.png", Unit),
            data("basi4a08.png", Unit),
            data("basi4a16.png", Unit),
            data("basi6a08.png", Unit),
            data("basi6a16.png", Unit),
            data("basn0g01.png", Unit),
            data("basn0g02.png", Unit),
            data("basn0g04.png", Unit),
            data("basn0g08.png", Unit),
            data("basn0g16.png", Unit),
            data("basn2c08.png", Unit),
            data("basn2c16.png", Unit),
            data("basn3p01.png", Unit),
            data("basn3p02.png", Unit),
            data("basn3p04.png", Unit),
            data("basn3p08.png", Unit),
            data("basn4a08.png", Unit),
            data("basn4a16.png", Unit),
            data("basn6a08.png", Unit),
            data("basn6a16.png", Unit),
            data("bgai4a08.png", Unit),
            data("bgai4a16.png", Unit),
            data("bgan6a08.png", Unit),
            data("bgan6a16.png", Unit),
            data("bgbn4a08.png", Unit),
            data("bggn4a16.png", Unit),
            data("bgwn6a08.png", Unit),
            data("bgyn6a16.png", Unit),
            data("ccwn2c08.png", Unit),
            data("ccwn3p08.png", Unit),
            data("cdfn2c08.png", Unit),
            data("cdhn2c08.png", Unit),
            data("cdsn2c08.png", Unit),
            data("cdun2c08.png", Unit),
            data("ch1n3p04.png", Unit),
            data("ch2n3p08.png", Unit),
            data("cm0n0g04.png", Unit),
            data("cm7n0g04.png", Unit),
            data("cm9n0g04.png", Unit),
            data("cs3n2c16.png", Unit),
            data("cs3n3p08.png", Unit),
            data("cs5n2c08.png", Unit),
            data("cs5n3p08.png", Unit),
            data("cs8n2c08.png", Unit),
            data("cs8n3p08.png", Unit),
            data("ct0n0g04.png", Unit),
            data("ct1n0g04.png", Unit),
            data("cten0g04.png", Unit),
            data("ctfn0g04.png", Unit),
            data("ctgn0g04.png", Unit),
            data("cthn0g04.png", Unit),
            data("ctjn0g04.png", Unit),
            data("ctzn0g04.png", Unit),
            data("exif2c08.png", Unit),
            data("f00n0g08.png", Unit),
            data("f00n2c08.png", Unit),
            data("f01n0g08.png", Unit),
            data("f01n2c08.png", Unit),
            data("f02n0g08.png", Unit),
            data("f02n2c08.png", Unit),
            data("f03n0g08.png", Unit),
            data("f03n2c08.png", Unit),
            data("f04n0g08.png", Unit),
            data("f04n2c08.png", Unit),
            data("f99n0g04.png", Unit),
            data("g03n0g16.png", Unit),
            data("g03n2c08.png", Unit),
            data("g03n3p04.png", Unit),
            data("g04n0g16.png", Unit),
            data("g04n2c08.png", Unit),
            data("g04n3p04.png", Unit),
            data("g05n0g16.png", Unit),
            data("g05n2c08.png", Unit),
            data("g05n3p04.png", Unit),
            data("g07n0g16.png", Unit),
            data("g07n2c08.png", Unit),
            data("g07n3p04.png", Unit),
            data("g10n0g16.png", Unit),
            data("g10n2c08.png", Unit),
            data("g10n3p04.png", Unit),
            data("g25n0g16.png", Unit),
            data("g25n2c08.png", Unit),
            data("g25n3p04.png", Unit),
            data("oi1n0g16.png", Unit),
            data("oi1n2c16.png", Unit),
            data("oi2n0g16.png", Unit),
            data("oi2n2c16.png", Unit),
            data("oi4n0g16.png", Unit),
            data("oi4n2c16.png", Unit),
            data("oi9n0g16.png", Unit),
            data("oi9n2c16.png", Unit),
            data("PngSuite.png", Unit),
            data("pp0n2c16.png", Unit),
            data("pp0n6a08.png", Unit),
            data("ps1n0g08.png", Unit),
            data("ps1n2c16.png", Unit),
            data("ps2n0g08.png", Unit),
            data("ps2n2c16.png", Unit),
            data("s01i3p01.png", Unit),
            data("s01n3p01.png", Unit),
            data("s02i3p01.png", Unit),
            data("s02n3p01.png", Unit),
            data("s03i3p01.png", Unit),
            data("s03n3p01.png", Unit),
            data("s04i3p01.png", Unit),
            data("s04n3p01.png", Unit),
            data("s05i3p02.png", Unit),
            data("s05n3p02.png", Unit),
            data("s06i3p02.png", Unit),
            data("s06n3p02.png", Unit),
            data("s07i3p02.png", Unit),
            data("s07n3p02.png", Unit),
            data("s08i3p02.png", Unit),
            data("s08n3p02.png", Unit),
            data("s09i3p02.png", Unit),
            data("s09n3p02.png", Unit),
            data("s32i3p04.png", Unit),
            data("s32n3p04.png", Unit),
            data("s33i3p04.png", Unit),
            data("s33n3p04.png", Unit),
            data("s34i3p04.png", Unit),
            data("s34n3p04.png", Unit),
            data("s35i3p04.png", Unit),
            data("s35n3p04.png", Unit),
            data("s36i3p04.png", Unit),
            data("s36n3p04.png", Unit),
            data("s37i3p04.png", Unit),
            data("s37n3p04.png", Unit),
            data("s38i3p04.png", Unit),
            data("s38n3p04.png", Unit),
            data("s39i3p04.png", Unit),
            data("s39n3p04.png", Unit),
            data("s40i3p04.png", Unit),
            data("s40n3p04.png", Unit),
            data("tbbn0g04.png", Unit),
            data("tbbn2c16.png", Unit),
            data("tbbn3p08.png", Unit),
            data("tbgn2c16.png", Unit),
            data("tbgn3p08.png", Unit),
            data("tbrn2c08.png", Unit),
            data("tbwn0g16.png", Unit),
            data("tbwn3p08.png", Unit),
            data("tbyn3p08.png", Unit),
            data("tm3n3p02.png", Unit),
            data("tp0n0g08.png", Unit),
            data("tp0n2c08.png", Unit),
            data("tp0n3p08.png", Unit),
            data("tp1n3p08.png", Unit),
            data("z00n2c08.png", Unit),
            data("z03n2c08.png", Unit),
            data("z06n2c08.png", Unit),
            data("z09n2c08.png", Unit)
        ) { a, _ ->
            val bitmap = ClasspathPath(
                this::class.java.classLoader, a
            ).readNow {
                decodePng(it)
            }
            it("should return a valid bitmap") {
                (bitmap.width in 1..256) shouldBe true
                (bitmap.height in 1..256) shouldBe true
            }
        }
        data(
            { a -> "decoding $a" },
            data("xc1n0g08.png", Unit),
            data("xc9n2c08.png", Unit),
            data("xcrn0g04.png", Unit),
            data("xcsn0g01.png", Unit),
            data("xd0n2c08.png", Unit),
            data("xd3n2c08.png", Unit),
            data("xd9n2c08.png", Unit),
            data("xdtn0g01.png", Unit),
            data("xhdn0g08.png", Unit),
            data("xlfn0g04.png", Unit),
            data("xs1n0g01.png", Unit),
            data("xs2n0g01.png", Unit),
            data("xs4n0g01.png", Unit),
            data("xs7n0g01.png", Unit)
        ) { a, _ ->
            it("should throw an IOException") {
                shouldThrow<IOException> {
                    ClasspathPath(
                        this::class.java.classLoader, a
                    ).readNow {
                        decodePng(it)
                    }
                }
            }
        }
    }
})
