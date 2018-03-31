/*
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jbox2d.particle

/**
 * The particle type. Can be combined with | operator. Zero means liquid.
 *
 * @author dmurph
 */
object ParticleType {
    const val b2_waterParticle = 0
    /** removed after next step  */
    const val b2_zombieParticle = 1 shl 1
    /** zero velocity  */
    const val b2_wallParticle = 1 shl 2
    /** with restitution from stretching  */
    const val b2_springParticle = 1 shl 3
    /** with restitution from deformation  */
    const val b2_elasticParticle = 1 shl 4
    /** with viscosity  */
    const val b2_viscousParticle = 1 shl 5
    /** without isotropic pressure  */
    const val b2_powderParticle = 1 shl 6
    /** with surface tension  */
    const val b2_tensileParticle = 1 shl 7
    /** mixing color between contacting particles  */
    const val b2_colorMixingParticle = 1 shl 8
    /** call b2DestructionListener on destruction  */
    const val b2_destructionListener = 1 shl 9
}
