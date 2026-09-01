/*
 * SkyTube
 * Copyright (C) 2025  SkyTube Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package free.rm.skytube.app.stream.spec;

import org.schabi.newpipe.extractor.stream.VideoStream;

import free.rm.skytube.app.stream.Resolutions;
import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoResolution;

/**
 * Accepts only streams whose resolution falls inside the given bounds. A null bound is unbounded.
 */
public final class ResolutionWithin implements StreamSpec<VideoStream> {

    private final VideoResolution minResolution;
    private final VideoResolution maxResolution;

    public ResolutionWithin(VideoResolution minResolution, VideoResolution maxResolution) {
        this.minResolution = minResolution;
        this.maxResolution = maxResolution;
    }

    @Override
    public boolean isSatisfiedBy(VideoStream stream) {
        VideoResolution resolution = Resolutions.of(stream);
        if (minResolution != null && minResolution.isBetterQualityThan(resolution)) {
            return false;
        }
        return maxResolution == null || !resolution.isBetterQualityThan(maxResolution);
    }
}
