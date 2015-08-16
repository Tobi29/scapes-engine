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

import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

public final class FastMath {
    public static final double E = 2.7182818284590452354;
    public static final double HALF_PI = 1.57079632679489661923;
    public static final double PI = 3.14159265358979323846;
    public static final double RAD_2_DEG = 180.0 / PI;
    public static final double DEG_2_RAD = PI / 180.0;
    public static final double TWO_PI = 6.28318530717958647692;
    private static final double[] SIN = new double[1000000];
    private static final double[] ASIN = new double[1000000];

    static {
        for (int i = 0; i < SIN.length; i++) {
            SIN[i] = sin((i - 0.5) / SIN.length * TWO_PI);
        }
        int lowest = 0;
        int highest = 0;
        double lowestValue = 1.0;
        double highestValue = -1.0;
        for (int i = 0; i < SIN.length; i++) {
            double value = SIN[i];
            if (value < lowestValue) {
                lowest = i;
                lowestValue = value;
            } else if (value > highestValue) {
                highest = i;
                highestValue = value;
            }
        }
        SIN[lowest] = -1.0;
        SIN[highest] = 1.0;
        for (int i = 0; i < ASIN.length; i++) {
            ASIN[i] = asin(i * 2.0 / ASIN.length - 1.0);
        }
    }

    private FastMath() {
    }

    public static double abs(double a) {
        return a < 0.0 ? -a : a;
    }

    public static float abs(float a) {
        return a < 0.0f ? -a : a;
    }

    public static int abs(int a) {
        int sign = a >> 31;
        return (a ^ sign) - sign;
    }

    public static long abs(long a) {
        long sign = a >> 63;
        return (a ^ sign) - sign;
    }

    public static int round(float a) {
        return floor(a + 0.5f);
    }

    public static int round(double a) {
        return floor(a + 0.5f);
    }

    public static int floor(float a) {
        int i = (int) a;
        return i > a ? i - 1 : i;
    }

    public static int floor(double a) {
        int i = (int) a;
        return i > a ? i - 1 : i;
    }

    public static byte clamp(byte a, byte b, byte c) {
        return max(b, min(c, a));
    }

    public static byte max(byte a, byte b) {
        return a > b ? a : b;
    }

    public static byte min(byte a, byte b) {
        return a < b ? a : b;
    }

    public static double clamp(double a, double b, double c) {
        return max(b, min(c, a));
    }

    public static double max(double a, double b) {
        return a > b ? a : b;
    }

    public static double min(double a, double b) {
        return a < b ? a : b;
    }

    public static float clamp(float a, float b, float c) {
        return max(b, min(c, a));
    }

    public static float max(float a, float b) {
        return a > b ? a : b;
    }

    public static float min(float a, float b) {
        return a < b ? a : b;
    }

    public static int clamp(int a, int b, int c) {
        return max(b, min(c, a));
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    public static long clamp(long a, long b, long c) {
        return max(b, min(c, a));
    }

    public static long max(long a, long b) {
        return a > b ? a : b;
    }

    public static long min(long a, long b) {
        return a < b ? a : b;
    }

    public static Vector2d clamp(Vector2 a, Vector2 b, Vector2 c) {
        return max(b, min(c, a));
    }

    public static Vector2d max(Vector2 a, Vector2 b) {
        return new Vector2d(max(a.doubleX(), b.doubleX()),
                max(a.doubleY(), b.doubleY()));
    }

    public static Vector2d min(Vector2 a, Vector2 b) {
        return new Vector2d(min(a.doubleX(), b.doubleX()),
                min(a.doubleY(), b.doubleY()));
    }

    public static Vector3d clamp(Vector3 a, Vector3 b, Vector3 c) {
        return max(b, min(c, a));
    }

    public static Vector3d max(Vector3 a, Vector3 b) {
        return new Vector3d(max(a.doubleX(), b.doubleX()),
                max(a.doubleY(), b.doubleY()), max(a.doubleZ(), b.doubleZ()));
    }

    public static Vector3d min(Vector3 a, Vector3 b) {
        return new Vector3d(min(a.doubleX(), b.doubleX()),
                min(a.doubleY(), b.doubleY()), min(a.doubleZ(), b.doubleZ()));
    }

    public static double asin(double a) {
        return StrictMath.asin(a);
    }

    public static double asinTable(double a) {
        int i = round((a * 0.5 + 0.5) * ASIN.length);
        if (i < 0 || i >= ASIN.length) {
            return Double.NaN;
        }
        return ASIN[i];
    }

    public static double acos(double a) {
        return StrictMath.acos(a);
    }

    public static double atan(double a) {
        return StrictMath.atan(a);
    }

    public static double atan2(double y, double x) {
        return StrictMath.atan2(y, x);
    }

    public static double ceil(double a) {
        return StrictMath.ceil(a);
    }

    public static double cos(double a) {
        return StrictMath.cos(a);
    }

    public static double cosh(double x) {
        return StrictMath.cosh(x);
    }

    public static double sin(double a) {
        return StrictMath.sin(a);
    }

    public static double sinh(double x) {
        return StrictMath.sinh(x);
    }

    public static double tan(double a) {
        return StrictMath.tan(a);
    }

    public static double tanh(double x) {
        return StrictMath.tanh(x);
    }

    public static double sinTable(double a) {
        return sinTableLookup((int) (a / TWO_PI * SIN.length));
    }

    private static double sinTableLookup(int a) {
        return a >= 0 ? SIN[a % SIN.length] : -SIN[-a % SIN.length];
    }

    public static double cosTable(double a) {
        return sinTableLookup((int) ((a / TWO_PI + 0.25) * SIN.length));
    }

    public static float sqr(float a) {
        return a * a;
    }

    public static int sqr(int a) {
        return a * a;
    }

    public static long sqr(long a) {
        return a * a;
    }

    public static double sqrNoAbs(double a) {
        return a < 0 ? -a * a : a * a;
    }

    public static float sqrNoAbs(float a) {
        return a < 0 ? -a * a : a * a;
    }

    public static int sqrNoAbs(int a) {
        return a < 0 ? -a * a : a * a;
    }

    public static long sqrNoAbs(long a) {
        return a < 0 ? -a * a : a * a;
    }

    public static double exp(double a) {
        return StrictMath.exp(a);
    }

    public static double log(double a) {
        return StrictMath.log(a);
    }

    public static double log10(double a) {
        return StrictMath.log10(a);
    }

    public static double sqr(double a) {
        return a * a;
    }

    public static double cbr(double a) {
        return a * a * a;
    }

    public static double sqrt(double a) {
        return StrictMath.sqrt(a);
    }

    public static double cbrt(double a) {
        return StrictMath.cbrt(a);
    }

    public static double pow(double a, double b) {
        return StrictMath.pow(a, b);
    }

    public static short convertFloatToHalf(float a) {
        int bits = Float.floatToIntBits(a);
        int sign = bits >>> 16 & 0x8000;
        int value = (bits & 0x7fffffff) + 0x1000;
        if (value >= 0x47800000) {
            if ((bits & 0x7fffffff) >= 0x47800000) {
                if (value < 0x7f800000) {
                    return (short) (sign | 0x7c00);
                }
                return (short) (sign | 0x7c00 |
                        (bits & 0x007fffff) >>> 13);
            }
            return (short) (sign | 0x7bff);
        }
        if (value >= 0x38800000) {
            return (short) (sign | value - 0x38000000 >>> 13);
        }
        if (value < 0x33000000) {
            return (short) sign;
        }
        value = (bits & 0x7fffffff) >>> 23;
        return (short) (sign |
                (bits & 0x7fffff | 0x800000) + (0x800000 >>> value - 102) >>>
                        126 - value);
    }

    public static double angleDiff(double a1, double a2) {
        return diff(a1, a2, 360.0);
    }

    public static double diff(double a1, double a2, double m) {
        double diff = (a2 - a1) % m;
        double h = m * 0.5;
        while (diff > h) {
            diff -= m;
        }
        while (diff <= -h) {
            diff += m;
        }
        return diff;
    }

    public static double max(Vector2 a) {
        return max(a.doubleX(), a.doubleY());
    }

    public static double max(Vector3 a) {
        return max(a.doubleX(), max(a.doubleY(), a.doubleZ()));
    }

    public static double min(Vector2 a) {
        return min(a.doubleX(), a.doubleY());
    }

    public static double min(Vector3 a) {
        return min(a.doubleX(), min(a.doubleY(), a.doubleZ()));
    }

    public static Vector2d abs(Vector2 a) {
        return new Vector2d(abs(a.doubleX()), abs(a.doubleY()));
    }

    public static Vector3d abs(Vector3 a) {
        return new Vector3d(abs(a.doubleX()), abs(a.doubleY()),
                abs(a.doubleZ()));
    }

    public static double length(Vector2 a) {
        return length(a.doubleX(), a.doubleY());
    }

    public static double length(double x, double y) {
        return sqrt(sqr(x) + sqr(y));
    }

    public static double length(Vector3 a) {
        return length(a.doubleX(), a.doubleY(), a.doubleZ());
    }

    public static double length(double x, double y, double z) {
        return sqrt(sqr(x) + sqr(y) + sqr(z));
    }

    public static double lengthSqr(Vector2 a) {
        return lengthSqr(a.doubleX(), a.doubleY());
    }

    public static double lengthSqr(double x, double y) {
        return sqr(x) + sqr(y);
    }

    public static double lengthSqr(Vector3 a) {
        return lengthSqr(a.doubleX(), a.doubleY(), a.doubleZ());
    }

    public static double lengthSqr(double x, double y, double z) {
        return sqr(x) + sqr(y) + sqr(z);
    }

    public static Vector2 normalize(Vector2 a) {
        return a.div(length(a));
    }

    public static Vector2 normalizeSafe(Vector2 a) {
        double length = length(a);
        if (length == 0.0) {
            return Vector2d.ZERO;
        }
        return a.div(length);
    }

    public static Vector3 normalize(Vector3 a) {
        return a.div(length(a));
    }

    public static Vector3 normalizeSafe(Vector3 a) {
        double length = length(a);
        if (length == 0.0) {
            return Vector3d.ZERO;
        }
        return a.div(length);
    }

    public static double dot(Vector2 a, Vector2 b) {
        return a.doubleX() * b.doubleX() + a.doubleY() * b.doubleY();
    }

    public static double dot(Vector3 a, Vector3 b) {
        return a.doubleX() * b.doubleX() + a.doubleY() * b.doubleY() +
                a.doubleZ() * b.doubleZ();
    }

    public static Vector3 cross(Vector3 a, Vector3 b) {
        double x = a.doubleY() * b.doubleZ() - a.doubleZ() * b.doubleY();
        double y = a.doubleZ() * b.doubleX() - a.doubleX() * b.doubleZ();
        double z = a.doubleX() * b.doubleY() - a.doubleY() * b.doubleX();
        return new Vector3d(x, y, z);
    }

    public static double pointDistance(Vector2 a, Vector2 b) {
        return pointDistance(a.doubleX(), a.doubleY(), b.doubleX(),
                b.doubleY());
    }

    public static double pointDistance(double x1, double y1, double x2,
            double y2) {
        return sqrt(pointDistanceSqr(x1, y1, x2, y2));
    }

    public static double pointDistance(Vector3 a, Vector3 b) {
        return pointDistance(a.doubleX(), a.doubleY(), a.doubleZ(), b.doubleX(),
                b.doubleY(), b.doubleZ());
    }

    public static double pointDistance(double x1, double y1, double z1,
            double x2, double y2, double z2) {
        return sqrt(pointDistanceSqr(x1, y1, z1, x2, y2, z2));
    }

    public static double pointDistanceSqr(Vector2 a, Vector2 b) {
        return pointDistanceSqr(a.doubleX(), a.doubleY(), b.doubleX(),
                b.doubleY());
    }

    public static double pointDistanceSqr(double x1, double y1, double x2,
            double y2) {
        return sqr(x2 - x1) + sqr(y2 - y1);
    }

    public static double pointDistanceSqr(Vector3 a, Vector3 b) {
        return pointDistanceSqr(a.doubleX(), a.doubleY(), a.doubleZ(),
                b.doubleX(), b.doubleY(), b.doubleZ());
    }

    public static double pointDistanceSqr(double x1, double y1, double z1,
            double x2, double y2, double z2) {
        return sqr(x2 - x1) + sqr(y2 - y1) + sqr(z2 - z1);
    }

    public static double pointDirection(Vector2 a, Vector2 b) {
        return pointDirection(a.doubleX(), a.doubleY(), b.doubleX(),
                b.doubleY());
    }

    public static double pointDirection(double x1, double y1, double x2,
            double y2) {
        return atan2(y2 - y1, x2 - x1) * RAD_2_DEG;
    }

    public static boolean inside(Vector2 origin, Vector2 size, Vector2 point) {
        return inside(origin.doubleX(), origin.doubleY(), size.doubleX(),
                size.doubleY(), point.doubleX(), point.doubleY());
    }

    public static boolean inside(double x1, double y1, double x2, double y2,
            double x, double y) {
        x -= x1;
        y -= y1;
        return x >= 0.0 && y >= 0.0 && x < x2 && y < y2;
    }

    public static boolean inside(Vector3 origin, Vector3 size, Vector3 point) {
        return inside(origin.doubleX(), origin.doubleY(), origin.doubleZ(),
                size.doubleX(), size.doubleY(), size.doubleZ(), point.doubleX(),
                point.doubleY(), point.doubleZ());
    }

    public static boolean inside(double x1, double y1, double z1, double x2,
            double y2, double z2, double x, double y, double z) {
        x -= x1;
        y -= y1;
        z -= z1;
        return x >= 0.0 && y >= 0.0 && z >= 0.0 && x < x2 && y < y2 && z < z2;
    }

    public static int nextPowerOfTwo(int value) {
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    public static double dot(int[] g, double x, double y) {
        return g[0] * x + g[1] * y;
    }

    public static double dot(int[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    public static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static float mix(float a, float b, float t) {
        return (1 - t) * a + t * b;
    }

    public static double mix(double a, double b, double t) {
        return (1 - t) * a + t * b;
    }
}
