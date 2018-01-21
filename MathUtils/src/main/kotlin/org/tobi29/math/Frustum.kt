/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.math

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.tan

class Frustum() {
    val pl = Array(6) { Plane() }
    private var posx = 0.0
    private var posy = 0.0
    private var posz = 0.0
    private var farD = 0.0
    private var fh = 0.0
    private var fw = 0.0
    private var init = false
    private var nearD = 0.0
    private var nh = 0.0
    private var nw = 0.0
    private var range = 0.0

    constructor(angle: Double,
                ratio: Double,
                nearD: Double,
                farD: Double) : this() {
        setPerspective(angle, ratio, nearD, farD)
    }

    fun setPerspective(angle: Double,
                       ratio: Double,
                       nearD: Double,
                       farD: Double) {
        this.nearD = nearD
        this.farD = farD
        val ang2grad = 0.0174532925199433
        val tang = tan(ang2grad * angle * 0.5)
        nh = this.nearD * tang
        nw = nh * ratio
        fh = this.farD * tang
        fw = fh * ratio
        val size = max(fw, fh)
        range = sqrt(farD * farD + size * size)
    }

    fun x(): Double {
        return posx
    }

    fun y(): Double {
        return posy
    }

    fun z(): Double {
        return posz
    }

    fun range(): Double {
        return range
    }

    fun inView(aabb: AABB): Int {
        if (init) {
            var out = 2
            for (i in 0..5) {
                if (pl[i].distance(aabb.getVertexPX(pl[i].normalx),
                        aabb.getVertexPY(pl[i].normaly),
                        aabb.getVertexPZ(pl[i].normalz)) < 0) {
                    return 0
                } else if (pl[i].distance(aabb.getVertexNX(pl[i].normalx),
                        aabb.getVertexNY(pl[i].normaly),
                        aabb.getVertexNZ(pl[i].normalz)) < 0) {
                    out = 1
                }
            }
            return out
        }
        return 0
    }

    fun setView(posx: Double,
                posy: Double,
                posz: Double,
                angx: Double,
                angy: Double,
                angz: Double,
                upx: Double,
                upy: Double,
                upz: Double) {
        this.posx = posx
        this.posy = posy
        this.posz = posz
        var zx = posx - angx
        var zy = posy - angy
        var zz = posz - angz
        var l = sqrt(zx * zx + zy * zy + zz * zz)
        if (abs(l) > 0.0f) {
            zx /= l
            zy /= l
            zz /= l
        }
        var xx = upy * zz - upz * zy
        var xy = upz * zx - upx * zz
        var xz = upx * zy - upy * zx
        l = sqrt(xx * xx + xy * xy + xz * xz)
        if (abs(l) > 0.0f) {
            xx /= l
            xy /= l
            xz /= l
        }
        val yx = zy * xz - zz * xy
        val yy = zz * xx - zx * xz
        val yz = zx * xy - zy * xx
        val ncx = posx - zx * nearD
        val ncy = posy - zy * nearD
        val ncz = posz - zz * nearD
        val fcx = posx - zx * farD
        val fcy = posy - zy * farD
        val fcz = posz - zz * farD
        val nhx = yx * nh
        val nhy = yy * nh
        val nhz = yz * nh
        val nwx = xx * nw
        val nwy = xy * nw
        val nwz = xz * nw
        val fhx = yx * fh
        val fhy = yy * fh
        val fhz = yz * fh
        val fwx = xx * fw
        val fwy = xy * fw
        val fwz = xz * fw
        val ntlx = ncx + nhx - nwx
        val ntly = ncy + nhy - nwy
        val ntlz = ncz + nhz - nwz
        val ntrx = ncx + nhx + nwx
        val ntry = ncy + nhy + nwy
        val ntrz = ncz + nhz + nwz
        val nblx = ncx - nhx - nwx
        val nbly = ncy - nhy - nwy
        val nblz = ncz - nhz - nwz
        val nbrx = ncx - nhx + nwx
        val nbry = ncy - nhy + nwy
        val nbrz = ncz - nhz + nwz
        val ftlx = fcx + fhx - fwx
        val ftly = fcy + fhy - fwy
        val ftlz = fcz + fhz - fwz
        val ftrx = fcx + fhx + fwx
        val ftry = fcy + fhy + fwy
        val ftrz = fcz + fhz + fwz
        val fblx = fcx - fhx - fwx
        val fbly = fcy - fhy - fwy
        val fblz = fcz - fhz - fwz
        val fbrx = fcx - fhx + fwx
        val fbry = fcy - fhy + fwy
        val fbrz = fcz - fhz + fwz
        pl[2].set3Points(ntrx, ntry, ntrz, ntlx, ntly, ntlz, ftlx, ftly, ftlz)
        pl[3].set3Points(nblx, nbly, nblz, nbrx, nbry, nbrz, fbrx, fbry, fbrz)
        pl[4].set3Points(ntlx, ntly, ntlz, nblx, nbly, nblz, fblx, fbly, fblz)
        pl[5].set3Points(nbrx, nbry, nbrz, ntrx, ntry, ntrz, fbrx, fbry, fbrz)
        pl[0].set3Points(ntlx, ntly, ntlz, ntrx, ntry, ntrz, nbrx, nbry, nbrz)
        pl[1].set3Points(ftrx, ftry, ftrz, ftlx, ftly, ftlz, fblx, fbly, fblz)
        init = true
    }
}
