/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.utils.math;

public class Frustum {
    public final Plane[] pl = new Plane[6];
    private double posx, posy, posz, angx, angy, angz, upx, upy, upz;
    private double farD;
    private double fh;
    private double fw;
    private boolean init;
    private double nearD;
    private double nh;
    private double nw;

    public Frustum(double angle, double ratio, double nearD, double farD) {
        this();
        setPerspective(angle, ratio, nearD, farD);
    }

    public Frustum() {
        for (int i = 0; i < pl.length; i++) {
            pl[i] = new Plane();
        }
    }

    public void setPerspective(double angle, double ratio, double nearD,
            double farD) {
        this.nearD = nearD;
        this.farD = farD;
        double ang2grad = 0.0174532925199433;
        double tang = FastMath.tan(ang2grad * angle * 0.5);
        nh = this.nearD * tang;
        nw = nh * ratio;
        fh = this.farD * tang;
        fw = fh * ratio;
    }

    public double getAngX() {
        return angx;
    }

    public double getAngY() {
        return angy;
    }

    public double getAngZ() {
        return angz;
    }

    public double getPosX() {
        return posx;
    }

    public double getPosY() {
        return posy;
    }

    public double getPosZ() {
        return posz;
    }

    public double getUpX() {
        return upx;
    }

    public double getUpY() {
        return upy;
    }

    public double getUpZ() {
        return upz;
    }

    public int inView(AABB aabb) {
        if (init) {
            int out = 2;
            for (int i = 0; i < 6; i++) {
                if (pl[i].distance(aabb.getVertexPX(pl[i].normalx),
                        aabb.getVertexPY(pl[i].normaly),
                        aabb.getVertexPZ(pl[i].normalz)) < 0) {
                    return 0;
                } else if (pl[i].distance(aabb.getVertexNX(pl[i].normalx),
                        aabb.getVertexNY(pl[i].normaly),
                        aabb.getVertexNZ(pl[i].normalz)) < 0) {
                    out = 1;
                }
            }
            return out;
        }
        return 0;
    }

    public void setView(double posx, double posy, double posz, double angx,
            double angy, double angz, double upx, double upy, double upz) {
        this.posx = posx;
        this.posy = posy;
        this.posz = posz;
        this.angx = angx;
        this.angy = angy;
        this.angz = angz;
        this.upx = upx;
        this.upy = upy;
        this.upz = upz;
        double zx = posx - angx;
        double zy = posy - angy;
        double zz = posz - angz;
        double l = FastMath.sqrt(zx * zx + zy * zy + zz * zz);
        if (FastMath.abs(l) > Float.MIN_NORMAL) {
            zx /= l;
            zy /= l;
            zz /= l;
        }
        double xx = upy * zz - upz * zy;
        double xy = upz * zx - upx * zz;
        double xz = upx * zy - upy * zx;
        l = FastMath.sqrt(xx * xx + xy * xy + xz * xz);
        if (FastMath.abs(l) > Float.MIN_NORMAL) {
            xx /= l;
            xy /= l;
            xz /= l;
        }
        double yx = zy * xz - zz * xy;
        double yy = zz * xx - zx * xz;
        double yz = zx * xy - zy * xx;
        double ncx = posx - zx * nearD;
        double ncy = posy - zy * nearD;
        double ncz = posz - zz * nearD;
        double fcx = posx - zx * farD;
        double fcy = posy - zy * farD;
        double fcz = posz - zz * farD;
        double nhx = yx * nh;
        double nhy = yy * nh;
        double nhz = yz * nh;
        double nwx = xx * nw;
        double nwy = xy * nw;
        double nwz = xz * nw;
        double fhx = yx * fh;
        double fhy = yy * fh;
        double fhz = yz * fh;
        double fwx = xx * fw;
        double fwy = xy * fw;
        double fwz = xz * fw;
        double ntlx = ncx + nhx - nwx;
        double ntly = ncy + nhy - nwy;
        double ntlz = ncz + nhz - nwz;
        double ntrx = ncx + nhx + nwx;
        double ntry = ncy + nhy + nwy;
        double ntrz = ncz + nhz + nwz;
        double nblx = ncx - nhx - nwx;
        double nbly = ncy - nhy - nwy;
        double nblz = ncz - nhz - nwz;
        double nbrx = ncx - nhx + nwx;
        double nbry = ncy - nhy + nwy;
        double nbrz = ncz - nhz + nwz;
        double ftlx = fcx + fhx - fwx;
        double ftly = fcy + fhy - fwy;
        double ftlz = fcz + fhz - fwz;
        double ftrx = fcx + fhx + fwx;
        double ftry = fcy + fhy + fwy;
        double ftrz = fcz + fhz + fwz;
        double fblx = fcx - fhx - fwx;
        double fbly = fcy - fhy - fwy;
        double fblz = fcz - fhz - fwz;
        double fbrx = fcx - fhx + fwx;
        double fbry = fcy - fhy + fwy;
        double fbrz = fcz - fhz + fwz;
        pl[2].set3Points(ntrx, ntry, ntrz, ntlx, ntly, ntlz, ftlx, ftly, ftlz);
        pl[3].set3Points(nblx, nbly, nblz, nbrx, nbry, nbrz, fbrx, fbry, fbrz);
        pl[4].set3Points(ntlx, ntly, ntlz, nblx, nbly, nblz, fblx, fbly, fblz);
        pl[5].set3Points(nbrx, nbry, nbrz, ntrx, ntry, ntrz, fbrx, fbry, fbrz);
        pl[0].set3Points(ntlx, ntly, ntlz, ntrx, ntry, ntrz, nbrx, nbry, nbrz);
        pl[1].set3Points(ftrx, ftry, ftrz, ftlx, ftly, ftlz, fblx, fbly, fblz);
        init = true;
    }
}
