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
package org.tobi29.scapes.engine;

import org.tobi29.scapes.engine.utils.io.tag.TagStructure;

public class ScapesEngineConfig {
    private final TagStructure tagStructure;
    private double fps, resolutionMultiplier;
    private boolean vSync, fullscreen;

    ScapesEngineConfig(TagStructure tagStructure) {
        this.tagStructure = tagStructure;
        vSync = tagStructure.getBoolean("VSync");
        fps = tagStructure.getDouble("Framerate");
        resolutionMultiplier = tagStructure.getDouble("ResolutionMultiplier");
        fullscreen = tagStructure.getBoolean("Fullscreen");
    }

    public boolean vSync() {
        return vSync;
    }

    public void setVSync(boolean vSync) {
        this.vSync = vSync;
        tagStructure.setBoolean("VSync", true);
    }

    public double fps() {
        return fps;
    }

    public void setFPS(double fps) {
        this.fps = fps;
        tagStructure.setDouble("Framerate", fps);
    }

    public double resolutionMultiplier() {
        return resolutionMultiplier;
    }

    public void setResolutionMultiplier(double resolutionMultiplier) {
        this.resolutionMultiplier = resolutionMultiplier;
        tagStructure.setDouble("ResolutionMultiplier", resolutionMultiplier);
    }

    public double volume(String channel) {
        return tagStructure.getStructure("Volumes").getTagEntrySet().stream()
                .filter(entry -> channel.startsWith(entry.getKey()) &&
                        entry.getValue() instanceof Number)
                .sorted((entry1, entry2) -> entry2.getKey().length() -
                        entry1.getKey().length())
                .mapToDouble(entry -> ((Number) entry.getValue()).doubleValue())
                .findFirst().orElse(1.0);
    }

    public void setVolume(String channel, double value) {
        tagStructure.getStructure("Volumes").setDouble(channel, value);
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        tagStructure.setBoolean("Fullscreen", fullscreen);
    }
}
